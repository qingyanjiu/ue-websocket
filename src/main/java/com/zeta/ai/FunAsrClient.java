package com.zeta.ai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ShortBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class FunAsrClient {
    private volatile boolean isRecording;
    private BlockingQueue<byte[]> voices;

    private String streamUrl;
    private String output;

    private WebSocketClient websocket;

    public FunAsrClient(String streamUrl, String output) {
        this.output = output;
        this.streamUrl = streamUrl;
        this.isRecording = true;
        this.voices = new LinkedBlockingQueue<>();
    }
    public FunAsrClient(String streamUrl, String output, WebSocketClient websocket) {
        this.output = output;
        this.streamUrl = streamUrl;
        this.isRecording = true;
        this.websocket = websocket;
        this.voices = new LinkedBlockingQueue<>();
    }


    public void recStream() {
        //抓取资源
        new Thread(()->{
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(streamUrl);
            Frame frame = null;
            FFmpegFrameRecorder recorder = null;
            // 数据块大小 对应funasr[5,10,5]
            int targetSamplesCount = 960;
            // 初始化缓存音频数据相关变量
            // 缓存音频数据的字节数组
            byte[] cachedAudioData = new byte[targetSamplesCount * 2];
            // 缓存的样本数量
            int cachedSamplesCount = 0;

            // 剩余需要缓存的样本数量
            int remainingSamplesCount = targetSamplesCount - cachedSamplesCount;

            try {
                frameGrabber.setFormat("rtsp");
                // 使用TCP传输
                frameGrabber.setOption("rtsp_transport", "tcp");
//                // 设置读取的最大数据，单位字节
//                frameGrabber.setOption("probesize", "100000");
//                // 设置分析的最长时间，单位微秒
//                frameGrabber.setOption("analyzeduration", "100000");

                // 音频比特率
                frameGrabber.setAudioBitrate(16000);
                frameGrabber.setSampleRate(16000);
                frameGrabber.start();
                //转录为单轨, 16K采样率, wav格式
                recorder = new FFmpegFrameRecorder(output, 1);//frameGrabber.getAudioChannels()
                recorder.setFormat("wav");
                recorder.setSampleRate(16000);
                // 音频比特率
                recorder.setAudioBitrate(16000);
                recorder.setTimestamp(frameGrabber.getTimestamp());
                recorder.start();
                int index = 0;
                sendJson("2pass-online", "5,10,5", 10, "rtsp", "wav");
                while (isRecording) {
                    frame = frameGrabber.grabSamples();
                    if (frame == null){
                        isRecording = false;
                        break;
                    }
                    if (frame.samples != null) {
                        recorder.recordSamples(frame.sampleRate, frame.audioChannels, frame.samples);
                        recorder.setTimestamp(frameGrabber.getTimestamp());
                        ShortBuffer samplesBuffer = (ShortBuffer) frame.samples[0];
                        int samplesCount = samplesBuffer.remaining();
                        remainingSamplesCount = targetSamplesCount - cachedSamplesCount;
                        if (samplesCount >= remainingSamplesCount) {
                            // 当前接收到的音频数据足够填满缓存
                            // 将音频数据填充到缓存中
                            for (int i = 0; i < remainingSamplesCount; i++) {
                                short sample = samplesBuffer.get(i);
                                int cachedIndex = (cachedSamplesCount + i) * 2;
                                cachedAudioData[cachedIndex] = (byte) (sample & 0xff);
                                cachedAudioData[cachedIndex + 1] = (byte) ((sample >> 8) & 0xff);
                            }
                            //将cachedAudioData 转换为ByteString
                            if(websocket!=null){
                                websocket.send(cachedAudioData); // 将音频数据发送给 WebSocket
                            }else {
                                // 发送缓存的音频数据
                                voices.put(cachedAudioData);
                            }
                            // 清空缓存
                            cachedSamplesCount = 0;
                        } else {
                            // 当前接收到的音频数据无法填满缓存，继续缓存
                            // 将音频数据填充到缓存中
                            for (int i = 0; i < samplesCount; i++) {
                                short sample = samplesBuffer.get(i);
                                int cachedIndex = (cachedSamplesCount + i) * 2;
                                cachedAudioData[cachedIndex] = (byte) (sample & 0xff);
                                cachedAudioData[cachedIndex + 1] = (byte) ((sample >> 8) & 0xff);
                            }
                            // 更新缓存的样本数量
                            cachedSamplesCount += samplesCount;
                        }
                    }
                    index++;
                }
                sendEof();
                recorder.stop();
                recorder.release();
                frameGrabber.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendJson(
            String mode, String strChunkSize, int chunkInterval, String wavName, String suffix) {
        try {

            JSONObject obj = new JSONObject();
            obj.put("mode", mode);
            JSONArray array = new JSONArray();
            String[] chunkList = strChunkSize.split(",");
            for (int i = 0; i < chunkList.length; i++) {
                array.add(Integer.valueOf(chunkList[i].trim()));
            }

            obj.put("chunk_size", array);
            obj.put("chunk_interval", new Integer(chunkInterval));
            obj.put("wav_name", wavName);
//            if(FunasrWsClient.hotwords.trim().length()>0)
//            {
//                obj.put("hotwords", FunasrWsClient.hotwords.trim());
//            }
            if (suffix.equals("wav")) {
                suffix = "pcm";
            }
            obj.put("wav_format", suffix);
            obj.put("is_speaking", new Boolean(true));
            log.info("sendJson: " + obj);
            // return;

            websocket.send(obj.toString());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send json at end of wav
    public void sendEof() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("is_speaking", new Boolean(false));

            log.info("sendEof: " + obj);

            websocket.send(obj.toString());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String streamUrl = "rtsp://49.7.210.27:8554/0b4f4cfbb7f5d8424d6b7f4a0975cd41/b89d5ddc33c5575be3a32c84a000acb7";
        String wsUrl = "ws://49.7.210.27:10095";
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI(wsUrl), new Draft_6455()) {
                //连接服务端时触发
                @Override
                public void onOpen(ServerHandshake handshakedata) {

                    log.info("websocket客户端和服务器连接成功");
                }
                //收到服务端消息时触发
                @Override
                public void onMessage(String message) {
                    JSONObject jsonObject = (JSONObject) JSONObject.parse(message);
                    String mode = jsonObject.getString("mode");
                    if ("2pass-offline".equals(mode)) {
                        log.info("识别结果={}", message);
                    }
                }
                //和服务端断开连接时触发
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("websocket客户端退出连接");
                }
                //连接异常时触发
                @Override
                public void onError(Exception ex) {
                    log.info("websocket客户端和服务器连接发生错误={}", ex.getMessage());
                }
            };
            webSocketClient.connect();
            FunAsrClient funAsrClient = new FunAsrClient(streamUrl, "d:\\test.wav", webSocketClient);
            funAsrClient.recStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

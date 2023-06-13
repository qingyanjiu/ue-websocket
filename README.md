#### 关于ai目标识别yolov7模块的使用
> 机器需要先安装python环境，安装pytorch相关包依赖，如果使用GPU，需要安装cuda和cudnn以及显卡的驱动程序

> 安装yolov7执行代码模块

> 机器需要安装ffmpeg

> 通过java调用python命令来执行java脚本，目标识别支持流媒体、视频、图片识别

#### 关于算法配置（目前放在ai包内，后期可能会单独做一个project）
> 配置如下
```yaml
ai:
  yolov7:
    #      推流的默认服务器
    pushUrlBase: rtmp://43.142.85.176:1935/detect/
    algs:
      # 交通流量识别
      trafficFlow:
        # conda env name
        candaEnv: test
        # 脚本文件路径
        pyPath: C:\Users\louis\IdeaProjects\AI-visual\zeta-object-detect\yolov7\detect_frame_for_java_call.py
        # 权重文件路径
        weights: D:\yolov7.pt
        # 使用的设备 0 1 2 GPU | cpu CPU
        device: 0
```

> 目前通过接口去启动一个目标检测算法，例如yolov7的trafficFlow算法:
```shell
/ai/yolov7/检测算法名?source=http://180.101.237.17:4119/30.m3u8&requestId=test-stream
```
> requestId是一个唯一的请求编号，请求成功后会将requestId存到redis中，用于记录当前正在执行检测的线程，在应用关闭后，
> 会自动关闭所有python检测进程并删除所有redis中的request记录

> 目标检测每一时刻的检测数据会写入到redis中，类型是zset，key是 ${requestId}:data, value是json字符串，转换成json对象后是检测结果对象，score是写入数据的unix时间（毫秒）。前端需要展示的时候，会去拿最新插入的数据，然后根据配置将写入时间早于某个时间段的数据全部删除，防止保存数据过多。

> 具体调用接口如下
```shell
/ai/yolov7/qryDetectionData?requestId=detect-car
```

##### 实时开启/关闭画框
```shell
开启
curl --location --request POST 'localhost:18080/ai/yolov7/startDrawRect?requestId=test-stream' \
--header 'Authorization: 44dc9d0a-85a9-41cc-9e14-193c4ba1c480'

关闭
curl --location --request POST 'localhost:18080/ai/yolov7/stopDrawRect?requestId=test-stream' \
--header 'Authorization: 44dc9d0a-85a9-41cc-9e14-193c4ba1c480'
```

##### 开启视频目标识别调用
```shell
curl --location --request POST 'localhost:18080/ai/yolov7/traffic-flow' \
--header 'Authorization: 44dc9d0a-85a9-41cc-9e14-193c4ba1c480' \
--header 'Content-Type: application/json' \
--data-raw '{
    "source": "http://180.101.237.17:4119/30.m3u8",
    "requestId": "test-stream",
    "calDensity": true,
    "detectAreas": "[]"
}'

其中traffic-flow是配置文件中yolov7下的每一个算法配置名称
```

##### 查询正在运行的python进程
```shell
curl --location --request POST 'localhost:18080/ai/pythonProcess/list' \
--header 'Authorization: 44dc9d0a-85a9-41cc-9e14-193c4ba1c480'
```

##### 关闭视频目标识别进程
```shell
curl --location --request POST 'localhost:18080/ai/pythonProcess/stop?requestId=test-stream' \
--header 'Authorization: 44dc9d0a-85a9-41cc-9e14-193c4ba1c480'

其中requestId是启动进程时候的唯一编号
```


> 后期可能会考虑将所有算法配置好，启动时直接启动所有检测进程，新封装一个project专门用于执行实时目标检测任务

#### 关于需要多种环境的算法
> 例如先用yolov7进行图像识别再通过ocr进行文字提取，那么可能yolov7需要torch环境而ocr需要tensorflow，两者又不兼容

> 这时候就需要conda切换环境，但linux下用 java process 去做 conda activate 有问题

> 可以考虑将conda的两个环境的路径分别都放到PATH中，并将python命令文件给重命名一下，配置不同的python文件名来启动进程即可

```shell
ln -s /root/miniconda3/envs/chineseocr/bin/python /usr/bin/python-ocr
ln -s /root/miniconda3/envs/yolov7/bin/python /usr/bin/python-yolov7 
```
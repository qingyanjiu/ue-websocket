//package com.zeta.ai.utils.redis;
//
//import com.alibaba.fastjson.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.*;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
///**
// * @description:Redis工具类
// * @author: swwheihei
// * @date:   2020年5月6日 下午8:27:29
// */
//@Component
//@SuppressWarnings(value = {"rawtypes", "unchecked"})
//public class RedisUtil {
//
//	@Autowired
//    private RedisTemplate redisTemplate;
//
//	/**
//     * 指定缓存失效时间
//     * @param key 键
//     * @param time 时间（秒）
//     * @return true / false
//     */
//    public boolean expire(String key, long time) {
//        try {
//            if (time > 0) {
//                redisTemplate.expire(key, time, TimeUnit.SECONDS);
//            }
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * 根据 key 获取过期时间
//     * @param key 键
//     * @return
//     */
//    public long getExpire(String key) {
//        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 判断 key 是否存在
//     * @param key 键
//     * @return true / false
//     */
//    public boolean hasKey(String key) {
//        try {
//            return redisTemplate.hasKey(key);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * 删除缓存
//     * @SuppressWarnings("unchecked") 忽略类型转换警告
//     * @param key 键（一个或者多个）
//     */
//    public boolean del(String... key) {
//    	try {
//    		if (key != null && key.length > 0) {
//                if (key.length == 1) {
//                    redisTemplate.delete(key[0]);
//                } else {
////                    传入一个 Collection<String> 集合
//                    redisTemplate.delete(CollectionUtils.arrayToList(key));
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
////    ============================== String ==============================
//
//    /**
//     * 普通缓存获取
//     * @param key 键
//     * @return 值
//     */
//    public Object get(String key) {
//        return key == null ? null : redisTemplate.opsForValue().get(key);
//    }
//
//    /**
//     * 普通缓存放入
//     * @param key 键
//     * @param value 值
//     * @return true / false
//     */
//    public boolean set(String key, Object value) {
//        try {
//            redisTemplate.opsForValue().set(key, value);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * 普通缓存放入并设置时间
//     * @param key 键
//     * @param value 值
//     * @param time 时间（秒），如果 time < 0 则设置无限时间
//     * @return true / false
//     */
//    public boolean set(String key, Object value, long time) {
//        try {
//            if (time > 0) {
//                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
//            } else {
//                set(key, value);
//            }
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    /**
//     * 递增
//     * @param key 键
//     * @param delta 递增大小
//     * @return
//     */
//    public long incr(String key, long delta) {
//        if (delta < 0) {
//            throw new RuntimeException("递增因子必须大于 0");
//        }
//        return redisTemplate.opsForValue().increment(key, delta);
//    }
//
//    /**
//     * 递减
//     * @param key 键
//     * @param delta 递减大小
//     * @return
//     */
//    public long decr(String key, long delta) {
//        if (delta < 0) {
//            throw new RuntimeException("递减因子必须大于 0");
//        }
//        return redisTemplate.opsForValue().increment(key, delta);
//    }
//
////    ============================== Map ==============================
//
//    /**
//     * HashGet
//     * @param key 键（no null）
//     * @param item 项（no null）
//     * @return 值
//     */
//    public Object hget(String key, String item) {
//        return redisTemplate.opsForHash().get(key, item);
//    }
//
//    /**
//     * 获取 key 对应的 map
//     * @param key 键（no null）
//     * @return 对应的多个键值
//     */
//    public Map<Object, Object> hmget(String key) {
//        return redisTemplate.opsForHash().entries(key);
//    }
//
//    /**
//     * HashSet
//     * @param key 键
//     * @param map 值
//     * @return true / false
//     */
//    public boolean hmset(String key, Map<Object, Object> map) {
//        try {
//            Cursor<Map.Entry<Object,Object>> cursor = redisTemplate.opsForHash().scan("field",
//                    ScanOptions.scanOptions().match(query).count(1000).build());
//            while (cursor.hasNext()) {
//                Map.Entry<Object,Object> entry = cursor.next();
//                result.add(entry.getKey());
//                Object key = entry.getKey();
//                Object valueSet = entry.getValue();
//            }
//            //关闭cursor
//            cursor.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    /**
//     * 模糊查询
//     * @param query 查询参数
//     * @return
//     */
//    public List<Object> scan(String query) {
//        Set<String> keys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
//            Set<String> keysTmp = new HashSet<>();
//            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(query).count(1000).build());
//            while (cursor.hasNext()) {
//                keysTmp.add(new String(cursor.next()));
//            }
//            return keysTmp;
//        });
////        Set<String> keys = (Set<String>) redisTemplate.execute(new RedisCallback<Set<String>>(){
////
////            @Override
////            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
////                Set<String> keysTmp = new HashSet<>();
////                Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(query).count(1000).build());
////                while (cursor.hasNext()) {
////                    keysTmp.add(new String(cursor.next()));
////                }
////            return keysTmp;
////            }
////        });
//
//        return new ArrayList<>(keys);
//    }
//
//    //    ============================== 消息发送与订阅 ==============================
//    public void convertAndSend(String channel, JSONObject msg) {
////        redisTemplate.convertAndSend(channel, msg);
//        redisTemplate.convertAndSend(channel, msg);
//
//    }
//
//    /**
//     * 移除score 从min到max的数据
//     * @param key
//     * @param min
//     * @param max
//     * @return
//     */
//    public Long zSetRemoveRangeByScore(String key, double min, double max) {
//        try {
//            Long value = redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
//            return value;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//}















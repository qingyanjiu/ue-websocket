package com.zeta.ai.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.zeta.ai.utils.redis.FastJsonRedisSerializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @description:Redis中间件配置类，使用spring-data-redis集成，自动从application.yml中加载redis配置
 * @author: swwheihei
 * @date: 2019年5月30日 上午10:58:25
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.database}")
    private int database;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.timeout}")
    private int timeout;
    @Value("${spring.redis.poolMaxTotal:1000}")
    private int poolMaxTotal;
    @Value("${spring.redis.poolMaxIdle:500}")
    private int poolMaxIdle;
    @Value("${spring.redis.poolMaxWait:5}")
    private int poolMaxWait;

    @Bean
    public JedisPool jedisPool() {
        if (StringUtils.isBlank(password)) {
            password = null;
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(poolMaxIdle);
        poolConfig.setMaxTotal(poolMaxTotal);
        // 秒转毫秒
        poolConfig.setMaxWaitMillis(poolMaxWait * 1000L);
        JedisPool jp = new JedisPool(poolConfig, host, port, timeout * 1000, password, database);
        return jp;
    }

    @Bean("redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        // 使用fastjson进行序列化处理，提高解析效率
        FastJsonRedisSerializer<Object> serializer = new FastJsonRedisSerializer<Object>(Object.class);
        // value值的序列化采用fastJsonRedisSerializer
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        // key的序列化采用StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(lettuceConnectionFactory);
        // 使用fastjson时需设置此项，否则会报异常not support type
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        return template;
    }

}

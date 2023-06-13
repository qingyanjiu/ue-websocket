package com.zeta.ai.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.zeta.ai.utils.redis.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class UERedisConfig {

    @Bean
    public RedisTemplate<Object, Object> ueRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
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

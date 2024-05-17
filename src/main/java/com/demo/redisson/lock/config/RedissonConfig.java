package com.demo.redisson.lock.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: huangzh
 * @Date: 2024/5/15 14:51
 **/
@Configuration
@Slf4j
public class RedissonConfig {
    @Value("${redis-ip}")
    private String redisIp;
    @Value("${redis-port}")
    private String redisPort;
    @Value("${redis-pwd}")
    private String redisPwd;

    @Bean
    public RedissonClient redissonClient() {
        RedissonClient redissonClient = null;
        try {
            Config config = new Config();
            config.setCodec(new StringCodec());
//            config.useSingleServer()// 单机模式
//                    .setAddress("redis://" + redisIp + ":" + redisPort)// 连接地址端口
//                    .setPassword(redisPwd);// 密码
            // 集群
            String[] nodes = {"redis://10.15.18.88:7001",
                    "redis://10.15.18.88:7002",
                    "redis://10.15.18.88:7003",
                    "redis://10.15.18.88:8001",
                    "redis://10.15.18.88:8002",
                    "redis://10.15.18.88:8003"
            };
            config.useClusterServers().addNodeAddress(nodes)
                    .setPassword("password");
//                    .setCheckSlotsCoverage(false);// 关闭集群完整性检查，集群nodes.conf文件中，可能会出现内网ip导致 Not all slots covered!，关闭该项检查或修改nodes.conf文件中内网ip为公网ip即可


            redissonClient = Redisson.create(config);
        } catch (Exception e) {
            log.error("redisson初始化失败！");
            e.printStackTrace();
        }
        return redissonClient;
    }
}

package com.demo.redisson.lock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedissonDistributedLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedissonDistributedLockApplication.class, args);
    }

}

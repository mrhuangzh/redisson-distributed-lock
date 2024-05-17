package com.demo.redisson.lock.controller;

import com.demo.redisson.lock.annotation.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: huangzh
 * @Date: 2024/5/15 18:20
 **/
@Slf4j
@RestController
@RequestMapping("/redissonLock")
public class RedissonLockController {

    @Autowired
    RedissonClient redissonClient;

    @DistributedLock(lockName = "rLockTest", leaseTime = -1, waitTime = 17000)// 加锁，且30s自动延时
    @RequestMapping("/test")
    public String test() throws Exception {
        Long threadId = Thread.currentThread().getId();
        log.info("线程id={}进入方法={}", threadId, "test");
        testReentrantLock();
//        Thread.sleep(16000);
        RAtomicDouble moneyBucket = redissonClient.getAtomicDouble("money");
        Double money = moneyBucket.get();
        if (money == null) {
            log.info("线程id={},redis中money不存在，放入money=1", threadId);
            moneyBucket.set(1d);
        } else {
            log.info("线程id={}redis中money={}", threadId, money);
            double moneyNew = money + 1d;
//            moneyNew = moneyBucket.incrementAndGet();// 使用incr无法体现出线程间的争抢覆盖
            moneyBucket.set(moneyNew);
            log.info("线程id={}向redis中放入累加的money={}", threadId, moneyNew);
        }
        log.info("线程id={}方法退出前，最新的money={}", threadId, moneyBucket.get());
        log.info("\n");

        return "money:" + moneyBucket.get();
    }


    @DistributedLock(lockName = "rLockTest", leaseTime = -1, waitTime = 17000)// 加锁，且30s自动延时
    @RequestMapping("/testReentrantLock")
    public void testReentrantLock() throws Exception {

        Long threadId = Thread.currentThread().getId();
        log.info("线程id={}进入方法={}", threadId, "testReentrantLock");
//        Thread.sleep(16000);
    }
}


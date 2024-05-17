package com.demo.redisson.lock.aspect;

import com.demo.redisson.lock.annotation.DistributedLock;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @Author: huangzh
 * @Date: 2024/5/15 18:18
 **/
@Slf4j
@Aspect
@Component
public class DistributedLockAspect {
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.demo.redisson.lock.annotation.DistributedLock)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object object = null;
        // 获取key
        String lockKey = getLockKey1(joinPoint);
//        String lockName = getLockKey2(joinPoint);
        // 获取rLock对象
        RLock rLock = redissonClient.getFairLock(lockKey);// 公平锁
//        RLock rLock = redissonClient.getLock(lockKey);// 可重入锁
        DistributedLock distributedLockInfo = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(DistributedLock.class);
        try {
            // leaseTime=-1 时，redisson默认锁的持有时间为 30s ，
            // 每过 30/3 的时间就检测持有锁的方法是否结束，若未结束，则重新延长持有时间为 30 s
            final boolean status = rLock.tryLock(distributedLockInfo.waitTime(), distributedLockInfo.leaseTime(), TimeUnit.MILLISECONDS);
//            redis分布式锁（公平锁redissonClient.getFairLock）：
//            公平锁，且仅1个线程在获取锁，无竞争线程：使用redisson加锁后，redis中实际会存在三个key，具体加锁过程可以参考：【分布式锁】02-使用Redisson实现公平锁原理    https://cloud.tencent.com/developer/article/1602467?from=article.detail.1602109
//            1) "redisson_lock_queue:{.rLockTest}"
//            2) ".rLockTest"
//            3) "redisson_lock_timeout:{.rLockTest}"

//            可重入锁，或公平锁，存在多个竞争线程获取同一个锁：使用redisson加锁后，redis中实际仅存在一个key
//            1) ".rLockTest"
//                    这个hash类型的key中存在两个值：
//                    分别是当前锁持有线程的唯一标识，和锁持有次数
//                    1) "b32edf2c-daea-4a2b-aa20-64bc0773a21a:91"
//                    2) "1"

//            "redisson_lock_queue:{.rLockTest}"：是list类型，其中存储的数据为争抢锁的线程列表，如5个线程争抢一个锁，其中1线程获取后，queue中会存入其他4个线程
//            ".rLockTest"：分布式锁的key，即各个线程争抢的对象
//            "redisson_lock_timeout:{.rLockTest}"：是zset类型，测试得，其member存储数据和queue内相同，但是根据时间进行了score排序
            if (status) {
                log.info("线程id={}成功获取到锁", Thread.currentThread().getId());
                object = joinPoint.proceed();
            } else {
                log.info("线程id={}未获取到锁", Thread.currentThread().getId());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            // 确保是当前线程仍持有锁，再释放
            if (rLock != null && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                // 若leaseTime=-1，则释放锁时，会关闭自动续期的定时任务cancelExpirationRenewal
            }
        }
        return object;
    }

    public String getLockKey1(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        // key
        String lockKey = "";
        // 切点所在的类
        Class targetClass = joinPoint.getTarget().getClass();
        // 使用了注解的方法
        String methodName = joinPoint.getSignature().getName();
        // 参数类型列表
        Class[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
        // 被注解的方法
        Method method = targetClass.getMethod(methodName, parameterTypes);
        // 被注解方法的参数列表
        Object[] arguments = joinPoint.getArgs();
        // 注解信息
        DistributedLock distributedLockInfo = method.getAnnotation(DistributedLock.class);
        // 获取注解中指定的锁名
        String lockName = distributedLockInfo.lockName();
        if (StringUtil.isNullOrEmpty(lockName)) {
            // 未指定锁名，则获取被注解方法的指定位置参数当做锁名
            int argNum = distributedLockInfo.argNum();
            if (argNum > 0) {
                lockName = arguments[argNum - 1].toString();
            }
        }
        if (!StringUtil.isNullOrEmpty(lockName)) {
            // 进行前后缀拼接，规范完整锁名
            String lockNamePre = distributedLockInfo.lockNamePre();
            String lockNamePost = distributedLockInfo.lockNamePost();
            String separator = distributedLockInfo.sepatator();
            StringBuilder sb = new StringBuilder();
            sb.append(lockNamePre)
                    .append(separator)
                    .append(lockName)
                    .append(lockNamePost);
            lockKey = sb.toString();
            return lockKey;
        }
        // 获取不到则报错
        throw new IllegalArgumentException("无法获取锁名！");
    }

    public String getLockKey2(ProceedingJoinPoint joinPoint) {
        String lockKey = "";
        // 获取切点方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 获取切点方法上的注解信息
        DistributedLock distributedLockInfo = methodSignature.getMethod().getAnnotation(DistributedLock.class);
        // 获取切点方法的参数列表
        Object[] arguments = joinPoint.getArgs();
        // 获取切点方法上注解中指定的lockName
        String lockName = distributedLockInfo.lockName();
        if (StringUtil.isNullOrEmpty(lockName)) {
            // 未指定锁名，则获取被注解方法的指定位置参数当做锁名
            int argNum = distributedLockInfo.argNum();
            if (argNum > 0) {
                lockName = arguments[argNum - 1].toString();
            }
        }
        if (!StringUtil.isNullOrEmpty(lockName)) {
            // 进行前后缀拼接，规范完整锁名
            String lockNamePre = distributedLockInfo.lockNamePre();
            String lockNamePost = distributedLockInfo.lockNamePost();
            String separator = distributedLockInfo.sepatator();
            StringBuilder sb = new StringBuilder();
            sb.append(lockNamePre)
                    .append(separator)
                    .append(lockName)
                    .append(lockNamePost);
            lockKey = sb.toString();
            return lockKey;
        }
        // 获取不到则报错
        throw new IllegalArgumentException("无法获取锁名！");
    }
}
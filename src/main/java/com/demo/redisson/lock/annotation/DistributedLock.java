package com.demo.redisson.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: huangzh
 * @Date: 2024/5/15 15:05
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 锁名前缀
     *
     * @return
     */
    String lockNamePre() default "";

    /**
     * 锁名
     *
     * @return
     */
    String lockName() default "";

    /**
     * 锁名后缀
     *
     * @return
     */
    String lockNamePost() default "";

    /**
     * 无法直接指定锁名时，可以通过获取被注解方法的参数列表的第argNum个参数作为锁名
     *
     * @return
     */
    int argNum() default 0;

    /**
     * 分隔符
     *
     * @return
     */
    String sepatator() default ".";

    /**
     * 最长等待时间
     *
     * @return
     */
    long waitTime() default 3000L;

    /**
     * 锁的持有时间，超过该事件自动释放锁，因此需要大于被锁方法的执行时间
     *
     * @return
     */
    long leaseTime() default 20000L;
}
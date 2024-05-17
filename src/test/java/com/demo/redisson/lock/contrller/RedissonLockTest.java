package com.demo.redisson.lock.contrller;

import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @Author: huangzh
 * @Date: 2024/5/15 18:21
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedissonLockTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();


    @Test
    @PerfTest(invocations = 4, threads = 5)// invocations：执行次数、threads：执行线程数
    // 也可以在linux上使用 ab 工具进行并发模拟测试
    public void test() {
        log.info("test测试方法");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("", httpHeaders);

        List<Charset> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(UTF_8);
        httpHeaders.setAcceptCharset(mediaTypeList);
        testRestTemplate.postForEntity("/redissonLock/test", request, String.class);
    }
}

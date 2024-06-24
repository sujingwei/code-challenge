package org.example.code_challenge.assisttest.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class SendService {
    private final static Logger log = LoggerFactory.getLogger(SendService.class);

    @Autowired
    private OkHttpClient okHttpClient;

    /**
     * 发送地址
     */
    @Value("${ping.addresses}")
    private String addresses;

    /**
     * 连续发送时间
     */
    @Value("${send.seconds}")
    private Integer seconds;

    /**
     * 每秒发出的请求数
     */
    @Value("${send.requests}")
    private Integer requests;

    /**
     * 迟延队列
     */
    private final static BlockingQueue<SendDelayed> queue = new DelayQueue<>();

    private final static ExecutorService executorService =  Executors.newCachedThreadPool();

    public void send() throws InterruptedException {
        String[] addressArray = addresses.split(",");
        log.info("send addresses：{}, seconds:{}, requests:{}", addressArray, seconds, requests);
        for (int i = 0; i < seconds; i++) {
            for (int j = 0; j < requests; j++) {
                String url = addressArray[j % addressArray.length] + "?p=" + UUID.randomUUID().toString().substring(0, 8);
                long random = (long) (Math.random() * 300) + (3 + i) * 1000L; //
                queue.add(new SendDelayed(random, url, this));
            }
        }

        CountDownLatch latch = new CountDownLatch(seconds*requests);

        while (!queue.isEmpty()) {
            SendDelayed task = queue.take();
            // 异步执行
            executorService.execute(()->{
                try {
                    task.execute();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    }

    /**
     * 发送get请求
     *
     * @param url
     * @return
     */
    public String sendGetRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        String body = "";
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.body() != null)
                body = response.body().string();
            return body;
        } catch (Exception e) {
            // log.error(e.getMessage());
            log.error("请求的URL:{}, 响应状态码为：{}, body:{}", url, response!=null ? response.code() : 404, body);
            return null;
        } finally {
            if (response != null) {
                log.info("请求的URL:{}, 响应状态码为：{}, body:{}", url, response.code(), body);
                response.close();
            }
        }
    }
}

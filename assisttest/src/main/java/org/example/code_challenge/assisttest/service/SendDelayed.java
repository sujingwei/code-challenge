package org.example.code_challenge.assisttest.service;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class SendDelayed implements Delayed {
    private long expireTime;

    private String url;

    private SendService sendService;

    public SendDelayed(long delayTime, String url, SendService sendService) {
        this.expireTime = System.currentTimeMillis() + delayTime;
        this.url = url;
        this.sendService = sendService;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        long diff = expireTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    public void execute() {
        // 执行操作
        this.sendService.sendGetRequest(this.url);
    }

}

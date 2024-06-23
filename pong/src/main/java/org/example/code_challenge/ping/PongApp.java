package org.example.code_challenge.ping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Pong Server
 */
@SpringBootApplication
@RestController
public class PongApp {

    // 记录上次请求时间
    private long lastRequestTime = 0;
    // 请求加锁
    private final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        SpringApplication.run(PongApp.class, args);
    }

    /**
     * say world
     */
    @GetMapping("/")
    public Mono<String> index() {
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastRequestTime;
            if (elapsedTime >= 1000) {
                // 当前时间大于等于上次成功时间1秒时，表示放行
                lastRequestTime = now;
            } else {
                throw new TooManyRequestsException(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
            }
            return Mono.just(",World");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 429 异常处理
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Void> handleTooManyRequest(TooManyRequestsException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    /**
     * 自定义异常处理类
     */
    static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException(String message) {
            super(message);
        }
    }

}

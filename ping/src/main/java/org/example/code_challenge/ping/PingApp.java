package org.example.code_challenge.ping;

import org.example.code_challenge.ping.component.FileLockComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.nio.channels.FileLock;
import java.util.Objects;


/**
 * Pong Server
 */
@SpringBootApplication
@RestController
public class PingApp {
    private final static Logger log = LoggerFactory.getLogger(PingApp.class);
    @Value("${pong.server.address}")
    private String pongServerAddress;

    @Lazy
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileLockComponent fileLockComponent;

    public static void main(String[] args) {
        SpringApplication.run(PingApp.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @GetMapping("/")
    public Mono<String> index() {
        String pong = pong();
        // 执行业务逻辑
        return Mono.just("Hello" + pong);
    }

    /**
     * 调pong服务，获取 world
     *
     * @return
     */
    public String pong() {
        FileLock lock = fileLockComponent.getLock();
        try {
            if (lock == null || !lock.isValid()) {
                // 获取文件锁失败
                throw new ForbiddenException(HttpStatus.FORBIDDEN.getReasonPhrase());
            }
            log.info("request pong server, url:{}", pongServerAddress);
            ResponseEntity<String> response = restTemplate.getForEntity(pongServerAddress, String.class);
            log.info("请求pong服务, response is Null:{}, code:{}", false, response.getStatusCode().value());
            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests ex) {
            throw ex;
        } catch (Exception e) {
            // 记录其它异常，但不处理
            log.error(e.getMessage());
            throw e;
        } finally {
            fileLockComponent.releaseLock(lock);
        }
    }

    /**
     * 429 异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<Void> handleTooManyRequest(HttpClientErrorException.TooManyRequests e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    /**
     * 获取文件锁失败时，服务器拒绝请求
     *
     * @param e
     * @return
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Void> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * 自定义ForbiddenException
     */
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }
}

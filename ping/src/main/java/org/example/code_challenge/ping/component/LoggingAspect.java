package org.example.code_challenge.ping.component;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.code_challenge.ping.PingApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Log Aspect
 */
@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(Logger)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 日志输出格式
        String logFormat = "ping send:%s, pong respond:[%d,%s]";
        int code = 200; // 请求状态码
        String params = "null";
        String rsStr = null; // 返回结果
        try {
            if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
                params = (String) joinPoint.getArgs()[0];
            }
            Object rs = joinPoint.proceed(joinPoint.getArgs());

            rsStr = ((Mono<String>) rs).block();

            return rs;
        } catch (HttpClientErrorException.TooManyRequests e) {
            // request rate limited
            code = 429;
            logFormat += String.format(", TooManyRequests:%s", "respond rate limited");
            throw e;
        } catch (PingApp.ForbiddenException e) {
            // 获取文件锁失败，会抛出(PingApp.ForbiddenException异常，响应结果为403状态
            code = 403;
            logFormat += String.format(", Forbidden:%s", "get file lock Fail！");
            throw e;
        } finally {
            String log = String.format(logFormat, params, code, Objects.nonNull(rsStr) && !"".equals(rsStr) ? rsStr : "null");
            if (code >= 400)
                logger.error(log);
            else
                logger.info(log);
        }
    }
}

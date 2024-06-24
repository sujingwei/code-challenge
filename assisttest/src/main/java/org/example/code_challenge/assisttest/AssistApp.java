package org.example.code_challenge.assisttest;

import okhttp3.OkHttpClient;
import org.example.code_challenge.assisttest.service.SendService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AssistApp implements CommandLineRunner, ApplicationContextAware {
    private ApplicationContext context = null;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext ctx) throws BeansException {
        context = ctx;
    }

    public static void main(String[] args) {
        SpringApplication.run(AssistApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        context.getBean(SendService.class).send();
    }


}

package org.unlogged.demo;

import io.unlogged.Unlogged;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
//@EnableScheduling
public class UnloggedDemoApplication {
    @Unlogged(port=12100)
    public static void main(String[] args) {
        SpringApplication.run(UnloggedDemoApplication.class, args);
    }
}

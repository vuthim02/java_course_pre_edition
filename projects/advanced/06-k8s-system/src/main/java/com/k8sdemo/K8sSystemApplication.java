package com.k8sdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class K8sSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(K8sSystemApplication.class, args);
    }
}

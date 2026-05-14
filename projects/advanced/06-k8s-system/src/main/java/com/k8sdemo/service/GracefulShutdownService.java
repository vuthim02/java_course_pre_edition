package com.k8sdemo.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GracefulShutdownService implements ApplicationListener<ContextClosedEvent> {

    @Getter
    private volatile boolean shuttingDown = false;

    private volatile Connector connector;

    @PostConstruct
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown hook triggered");
            shuttingDown = true;
        }, "jvm-shutdown-hook"));
    }

    @Bean
    public TomcatConnectorCustomizer connectorCustomizer() {
        return c -> GracefulShutdownService.this.connector = c;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        shuttingDown = true;
        log.info("Context closed, draining requests...");
        if (connector != null) {
            connector.pause();
            var ph = connector.getProtocolHandler();
            if (ph != null) {
                var executor = ph.getExecutor();
                if (executor instanceof ThreadPoolExecutor threadPool) {
                    threadPool.shutdown();
                    try {
                        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                            log.warn("Executor did not terminate within 30s");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        log.info("Request drain complete");
    }

    public void initiateShutdown() {
        shuttingDown = true;
        log.info("Graceful shutdown initiated via API");
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.exit(0);
        }, "initiate-shutdown").start();
    }
}

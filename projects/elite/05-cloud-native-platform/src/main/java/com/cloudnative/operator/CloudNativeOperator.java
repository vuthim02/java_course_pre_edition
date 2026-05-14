package com.cloudnative.operator;

import io.javaoperatorsdk.operator.Operator;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudNativeOperator {

    private static final Logger log = LoggerFactory.getLogger(CloudNativeOperator.class);

    public static void main(String[] args) {
        log.info("Starting CloudNative Operator...");

        KubernetesClient client = new KubernetesClientBuilder()
            .withConfig(new ConfigBuilder().build())
            .build();

        Operator operator = new Operator(overrider -> overrider.withKubernetesClient(client));

        operator.register(new AppDeploymentReconciler(client));
        operator.register(new ServiceMeshConfigReconciler(client));
        operator.register(new CanaryDeploymentReconciler(client));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            operator.stop();
            client.close();
        }));
        operator.start();
    }
}

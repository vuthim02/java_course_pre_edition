package com.cloudnative.operator;

import com.cloudnative.operator.model.ServiceMeshConfig;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.util.Map;

@ControllerConfiguration
public class ServiceMeshConfigReconciler implements Reconciler<ServiceMeshConfig> {
    private static final Logger log = LoggerFactory.getLogger(ServiceMeshConfigReconciler.class);
    private final KubernetesClient client;

    public ServiceMeshConfigReconciler(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public UpdateControl<ServiceMeshConfig> reconcile(ServiceMeshConfig resource, Context<ServiceMeshConfig> context) {
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        ServiceMeshConfig.ServiceMeshSpec spec = resource.getSpec();

        log.info("Reconciling ServiceMeshConfig: {}/{}", namespace, name);

        String virtualServiceName = name + "-virtual-service";
        String destinationRuleName = name + "-destination-rule";

        createVirtualService(virtualServiceName, namespace, spec);
        createDestinationRule(destinationRuleName, namespace, spec);

        ServiceMeshConfig.ServiceMeshStatus status = new ServiceMeshConfig.ServiceMeshStatus();
        status.setPhase("CONFIGURED");
        status.setVirtualServiceName(virtualServiceName);
        status.setDestinationRuleName(destinationRuleName);
        resource.setStatus(status);

        return UpdateControl.updateStatus(resource);
    }

    private void createVirtualService(String name, String namespace, ServiceMeshConfig.ServiceMeshSpec spec) {
        String yaml = """
            apiVersion: networking.istio.io/v1beta1
            kind: VirtualService
            metadata:
              name: %s
              namespace: %s
            spec:
              hosts:
              - %s
              http:
              - route:
                - destination:
                    host: %s
                    subset: stable
                  weight: %d
                - destination:
                    host: %s
                    subset: canary
                  weight: %d
            """.formatted(name, namespace, spec.getServiceName(), spec.getServiceName(),
                100 - spec.getWeight(), spec.getServiceName(), spec.getWeight());

        try {
            var resourceList = client.load(new ByteArrayInputStream(yaml.getBytes())).items();
            if (resourceList.isEmpty()) return;
            var resource = resourceList.get(0);
            var existing = client.resources(resource.getClass())
                .inNamespace(namespace).withName(name).get();
            if (existing == null) {
                client.resource(resource).inNamespace(namespace).create();
            } else {
                client.resource(resource).inNamespace(namespace).update();
            }
            log.info("VirtualService {}/{} configured", namespace, name);
        } catch (Exception e) {
            log.error("Failed to create VirtualService: {}", e.getMessage(), e);
        }
    }

    private void createDestinationRule(String name, String namespace, ServiceMeshConfig.ServiceMeshSpec spec) {
        String yaml = """
            apiVersion: networking.istio.io/v1beta1
            kind: DestinationRule
            metadata:
              name: %s
              namespace: %s
            spec:
              host: %s
              trafficPolicy:
                connectionPool:
                  tcp:
                    maxConnections: %d
                  http:
                    http1MaxPendingRequests: %d
                    maxRequestsPerConnection: %d
                outlierDetection:
                  consecutive5xxErrors: %d
                  interval: 30s
                  baseEjectionTime: 30s
              subsets:
              - name: stable
                labels:
                  version: stable
              - name: canary
                labels:
                  version: canary
            """.formatted(name, namespace, spec.getServiceName(),
                spec.getCircuitBreaker() != null ? spec.getCircuitBreaker().getMaxConnections() : 100,
                spec.getCircuitBreaker() != null ? spec.getCircuitBreaker().getMaxPendingRequests() : 1000,
                spec.getCircuitBreaker() != null ? spec.getCircuitBreaker().getMaxRequestsPerConnection() : 10,
                spec.getCircuitBreaker() != null ? spec.getCircuitBreaker().getMaxRetries() : 3);

        try {
            var resourceList = client.load(new ByteArrayInputStream(yaml.getBytes())).items();
            if (resourceList.isEmpty()) return;
            var resource = resourceList.get(0);
            var existing = client.resources(resource.getClass())
                .inNamespace(namespace).withName(name).get();
            if (existing == null) {
                client.resource(resource).inNamespace(namespace).create();
            } else {
                client.resource(resource).inNamespace(namespace).update();
            }
            log.info("DestinationRule {}/{} configured", namespace, name);
        } catch (Exception e) {
            log.error("Failed to create DestinationRule: {}", e.getMessage(), e);
        }
    }
}

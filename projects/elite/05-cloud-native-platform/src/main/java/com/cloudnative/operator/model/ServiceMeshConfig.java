package com.cloudnative.operator.model;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("cloudnative.elite.com")
@Version("v1")
@Kind("ServiceMeshConfig")
@Plural("servicemeshconfigs")
public class ServiceMeshConfig extends CustomResource<ServiceMeshConfig.ServiceMeshSpec, ServiceMeshConfig.ServiceMeshStatus> implements Namespaced {

    public static class ServiceMeshSpec {
        private String serviceName;
        private int weight;
        private RetryPolicy retryPolicy;
        private CircuitBreakerPolicy circuitBreaker;
        private String[] timeouts;

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public int getWeight() { return weight; }
        public void setWeight(int weight) { this.weight = weight; }
        public RetryPolicy getRetryPolicy() { return retryPolicy; }
        public void setRetryPolicy(RetryPolicy retryPolicy) { this.retryPolicy = retryPolicy; }
        public CircuitBreakerPolicy getCircuitBreaker() { return circuitBreaker; }
        public void setCircuitBreaker(CircuitBreakerPolicy circuitBreaker) { this.circuitBreaker = circuitBreaker; }
        public String[] getTimeouts() { return timeouts; }
        public void setTimeouts(String[] timeouts) { this.timeouts = timeouts; }
    }

    public static class ServiceMeshStatus {
        private String phase;
        private String virtualServiceName;
        private String destinationRuleName;

        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }
        public String getVirtualServiceName() { return virtualServiceName; }
        public void setVirtualServiceName(String virtualServiceName) { this.virtualServiceName = virtualServiceName; }
        public String getDestinationRuleName() { return destinationRuleName; }
        public void setDestinationRuleName(String destinationRuleName) { this.destinationRuleName = destinationRuleName; }
    }

    public static class RetryPolicy {
        private int attempts;
        private String perTryTimeout;
        private String retryOn;

        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }
        public String getPerTryTimeout() { return perTryTimeout; }
        public void setPerTryTimeout(String perTryTimeout) { this.perTryTimeout = perTryTimeout; }
        public String getRetryOn() { return retryOn; }
        public void setRetryOn(String retryOn) { this.retryOn = retryOn; }
    }

    public static class CircuitBreakerPolicy {
        private int maxConnections;
        private int maxPendingRequests;
        private int maxRequestsPerConnection;
        private int maxRetries;

        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public int getMaxPendingRequests() { return maxPendingRequests; }
        public void setMaxPendingRequests(int maxPendingRequests) { this.maxPendingRequests = maxPendingRequests; }
        public int getMaxRequestsPerConnection() { return maxRequestsPerConnection; }
        public void setMaxRequestsPerConnection(int maxRequestsPerConnection) { this.maxRequestsPerConnection = maxRequestsPerConnection; }
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    }
}

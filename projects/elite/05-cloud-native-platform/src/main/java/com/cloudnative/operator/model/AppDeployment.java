package com.cloudnative.operator.model;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("cloudnative.elite.com")
@Version("v1")
@Kind("AppDeployment")
@Plural("appdeployments")
public class AppDeployment extends CustomResource<AppDeployment.AppDeploymentSpec, AppDeployment.AppDeploymentStatus> implements Namespaced {

    public static class AppDeploymentSpec {
        private String applicationName;
        private String image;
        private int replicas;
        private int port;
        private String healthCheckPath;
        private ResourceRequirements resources;
        private EnvVar[] env;

        public String getApplicationName() { return applicationName; }
        public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public int getReplicas() { return replicas; }
        public void setReplicas(int replicas) { this.replicas = replicas; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getHealthCheckPath() { return healthCheckPath; }
        public void setHealthCheckPath(String healthCheckPath) { this.healthCheckPath = healthCheckPath; }
        public ResourceRequirements getResources() { return resources; }
        public void setResources(ResourceRequirements resources) { this.resources = resources; }
        public EnvVar[] getEnv() { return env; }
        public void setEnv(EnvVar[] env) { this.env = env; }
    }

    public static class AppDeploymentStatus {
        private String phase;
        private int availableReplicas;
        private String message;

        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }
        public int getAvailableReplicas() { return availableReplicas; }
        public void setAvailableReplicas(int availableReplicas) { this.availableReplicas = availableReplicas; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ResourceRequirements {
        private String cpu;
        private String memory;
        private String cpuLimit;
        private String memoryLimit;

        public String getCpu() { return cpu; }
        public void setCpu(String cpu) { this.cpu = cpu; }
        public String getMemory() { return memory; }
        public void setMemory(String memory) { this.memory = memory; }
        public String getCpuLimit() { return cpuLimit; }
        public void setCpuLimit(String cpuLimit) { this.cpuLimit = cpuLimit; }
        public String getMemoryLimit() { return memoryLimit; }
        public void setMemoryLimit(String memoryLimit) { this.memoryLimit = memoryLimit; }
    }

    public static class EnvVar {
        private String name;
        private String value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}

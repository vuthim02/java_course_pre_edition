package com.cloudnative.operator.model;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("cloudnative.elite.com")
@Version("v1")
@Kind("CanaryDeployment")
@Plural("canarydeployments")
public class CanaryDeployment extends CustomResource<CanaryDeployment.CanarySpec, CanaryDeployment.CanaryStatus> implements Namespaced {

    public static class CanarySpec {
        private String applicationName;
        private String stableImage;
        private String canaryImage;
        private int stableReplicas;
        private int canaryReplicas;
        private int trafficWeight;
        private String[] analysisMetrics;
        private String rollbackCriteria;

        public String getApplicationName() { return applicationName; }
        public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
        public String getStableImage() { return stableImage; }
        public void setStableImage(String stableImage) { this.stableImage = stableImage; }
        public String getCanaryImage() { return canaryImage; }
        public void setCanaryImage(String canaryImage) { this.canaryImage = canaryImage; }
        public int getStableReplicas() { return stableReplicas; }
        public void setStableReplicas(int stableReplicas) { this.stableReplicas = stableReplicas; }
        public int getCanaryReplicas() { return canaryReplicas; }
        public void setCanaryReplicas(int canaryReplicas) { this.canaryReplicas = canaryReplicas; }
        public int getTrafficWeight() { return trafficWeight; }
        public void setTrafficWeight(int trafficWeight) { this.trafficWeight = trafficWeight; }
        public String[] getAnalysisMetrics() { return analysisMetrics; }
        public void setAnalysisMetrics(String[] analysisMetrics) { this.analysisMetrics = analysisMetrics; }
        public String getRollbackCriteria() { return rollbackCriteria; }
        public void setRollbackCriteria(String rollbackCriteria) { this.rollbackCriteria = rollbackCriteria; }
    }

    public static class CanaryStatus {
        private String phase;
        private String stableDeploymentName;
        private String canaryDeploymentName;
        private String message;

        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }
        public String getStableDeploymentName() { return stableDeploymentName; }
        public void setStableDeploymentName(String stableDeploymentName) { this.stableDeploymentName = stableDeploymentName; }
        public String getCanaryDeploymentName() { return canaryDeploymentName; }
        public void setCanaryDeploymentName(String canaryDeploymentName) { this.canaryDeploymentName = canaryDeploymentName; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

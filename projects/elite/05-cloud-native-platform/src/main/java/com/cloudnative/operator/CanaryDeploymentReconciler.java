package com.cloudnative.operator;

import com.cloudnative.operator.model.CanaryDeployment;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerConfiguration
public class CanaryDeploymentReconciler implements Reconciler<CanaryDeployment> {
    private static final Logger log = LoggerFactory.getLogger(CanaryDeploymentReconciler.class);
    private final KubernetesClient client;

    public CanaryDeploymentReconciler(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public UpdateControl<CanaryDeployment> reconcile(CanaryDeployment resource, Context<CanaryDeployment> context) {
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        CanaryDeployment.CanarySpec spec = resource.getSpec();

        log.info("Reconciling CanaryDeployment: {}/{}", namespace, name);

        String stableName = name + "-stable";
        String canaryName = name + "-canary";

        Deployment stable = createDeployment(stableName, namespace, spec.getApplicationName(),
            spec.getStableImage(), spec.getStableReplicas(), "stable");
        Deployment canary = createDeployment(canaryName, namespace, spec.getApplicationName(),
            spec.getCanaryImage(), spec.getCanaryReplicas(), "canary");

        CanaryDeployment.CanaryStatus status = new CanaryDeployment.CanaryStatus();
        status.setPhase("RUNNING");
        status.setStableDeploymentName(stableName);
        status.setCanaryDeploymentName(canaryName);
        status.setMessage(String.format("Canary at %d%% traffic", spec.getTrafficWeight()));
        resource.setStatus(status);

        return UpdateControl.updateStatus(resource);
    }

    private Deployment createDeployment(String name, String namespace, String appName,
                                         String image, int replicas, String version) {
        Deployment existing = client.apps().deployments().inNamespace(namespace).withName(name).get();
        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata().withName(name).withNamespace(namespace)
            .addToLabels("app", appName)
            .addToLabels("version", version)
            .endMetadata()
            .withNewSpec()
            .withReplicas(replicas)
            .withNewSelector()
            .addToMatchLabels("app", appName)
            .addToMatchLabels("version", version)
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels("app", appName)
            .addToLabels("version", version)
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(appName)
            .withImage(image)
            .endContainer()
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

        if (existing == null) {
            log.info("Creating deployment {}/{}", namespace, name);
            return client.apps().deployments().inNamespace(namespace).resource(deployment).create();
        } else {
            log.info("Updating deployment {}/{}", namespace, name);
            return client.apps().deployments().inNamespace(namespace).resource(deployment).update();
        }
    }
}

package com.cloudnative.operator;

import com.cloudnative.operator.model.AppDeployment;
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
public class AppDeploymentReconciler implements Reconciler<AppDeployment> {
    private static final Logger log = LoggerFactory.getLogger(AppDeploymentReconciler.class);
    private final KubernetesClient client;

    public AppDeploymentReconciler(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public UpdateControl<AppDeployment> reconcile(AppDeployment resource, Context<AppDeployment> context) {
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        AppDeployment.AppDeploymentSpec spec = resource.getSpec();

        log.info("Reconciling AppDeployment: {}/{}", namespace, name);

        Deployment deployment = createOrUpdateDeployment(name, namespace, spec);
        Service service = createOrUpdateService(name, namespace, spec);

        AppDeployment.AppDeploymentStatus status = new AppDeployment.AppDeploymentStatus();
        status.setPhase("DEPLOYED");
        status.setAvailableReplicas(deployment.getStatus() != null ? deployment.getStatus().getAvailableReplicas() : 0);
        status.setMessage("Deployment and Service created successfully");
        resource.setStatus(status);

        return UpdateControl.updateStatus(resource);
    }

    private Deployment createOrUpdateDeployment(String name, String namespace, AppDeployment.AppDeploymentSpec spec) {
        Deployment existing = client.apps().deployments().inNamespace(namespace).withName(name).get();
        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata().withName(name).withNamespace(namespace)
            .addToLabels("app", spec.getApplicationName()).endMetadata()
            .withNewSpec()
            .withReplicas(spec.getReplicas())
            .withNewSelector().addToMatchLabels("app", spec.getApplicationName()).endSelector()
            .withNewTemplate()
            .withNewMetadata().addToLabels("app", spec.getApplicationName()).endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(spec.getApplicationName())
            .withImage(spec.getImage())
            .withPorts(new ContainerPortBuilder().withContainerPort(spec.getPort()).build())
            .withReadinessProbe(new ProbeBuilder()
                .withHttpGet(new HTTPGetActionBuilder()
                    .withPath(spec.getHealthCheckPath() != null ? spec.getHealthCheckPath() : "/actuator/health")
                    .withNewPort(spec.getPort()).build())
                .withInitialDelaySeconds(10).withPeriodSeconds(5).build())
            .withLivenessProbe(new ProbeBuilder()
                .withHttpGet(new HTTPGetActionBuilder()
                    .withPath(spec.getHealthCheckPath() != null ? spec.getHealthCheckPath() : "/actuator/health")
                    .withNewPort(spec.getPort()).build())
                .withInitialDelaySeconds(30).withPeriodSeconds(10).build())
            .endContainer()
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

        if (existing == null) {
            log.info("Creating Deployment: {}/{}", namespace, name);
            return client.apps().deployments().inNamespace(namespace).resource(deployment).create();
        } else {
            log.info("Updating Deployment: {}/{}", namespace, name);
            return client.apps().deployments().inNamespace(namespace).resource(deployment).update();
        }
    }

    private Service createOrUpdateService(String name, String namespace, AppDeployment.AppDeploymentSpec spec) {
        Service existing = client.services().inNamespace(namespace).withName(name).get();
        Service service = new ServiceBuilder()
            .withNewMetadata().withName(name).withNamespace(namespace)
            .addToLabels("app", spec.getApplicationName()).endMetadata()
            .withNewSpec()
            .withSelector(java.util.Map.of("app", spec.getApplicationName()))
            .addNewPort().withPort(spec.getPort()).withTargetPort(new IntOrString(spec.getPort())).endPort()
            .endSpec()
            .build();

        if (existing == null) {
            log.info("Creating Service: {}/{}", namespace, name);
            return client.services().inNamespace(namespace).resource(service).create();
        } else {
            log.info("Updating Service: {}/{}", namespace, name);
            return client.services().inNamespace(namespace).resource(service).update();
        }
    }
}

package org.miaixz.bus.gitlab.hooks.web;

import org.miaixz.bus.gitlab.support.JacksonJson;

public class EventEnvironment {

    private String name;
    private String action;
    private String deploymentTier;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDeploymentTier() {
        return deploymentTier;
    }

    public void setDeploymentTier(String deploymentTier) {
        this.deploymentTier = deploymentTier;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}

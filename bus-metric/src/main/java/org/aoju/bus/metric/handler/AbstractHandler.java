package org.aoju.bus.metric.handler;

import org.aoju.bus.metric.ApiConfig;

public class AbstractHandler {

    protected ApiConfig apiConfig;

    public AbstractHandler(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

}

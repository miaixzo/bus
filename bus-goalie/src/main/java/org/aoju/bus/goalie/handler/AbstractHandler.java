package org.aoju.bus.goalie.handler;

import org.aoju.bus.goalie.ApiConfig;

public class AbstractHandler {

    protected ApiConfig apiConfig;

    public AbstractHandler(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

}

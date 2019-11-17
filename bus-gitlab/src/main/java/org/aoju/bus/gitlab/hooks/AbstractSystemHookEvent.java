package org.aoju.bus.gitlab.hooks;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractSystemHookEvent implements SystemHookEvent {

    private String requestUrl;
    private String requestQueryString;
    private String requestSecretToken;

    @Override
    @JsonIgnore
    public String getRequestUrl() {
        return (requestUrl);
    }

    @Override
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    @Override
    @JsonIgnore
    public String getRequestQueryString() {
        return (requestQueryString);
    }

    @Override
    public void setRequestQueryString(String requestQueryString) {
        this.requestQueryString = requestQueryString;
    }

    @Override
    @JsonIgnore
    public String getRequestSecretToken() {
        return (requestSecretToken);
    }

    @Override
    public void setRequestSecretToken(String requestSecretToken) {
        this.requestSecretToken = requestSecretToken;
    }
}

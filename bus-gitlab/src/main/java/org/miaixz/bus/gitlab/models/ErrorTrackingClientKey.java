package org.miaixz.bus.gitlab.models;

import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorTrackingClientKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Boolean active;

    @JsonProperty("public_key")
    private String publicKey;

    @JsonProperty("sentry_dsn")
    private String sentryDsn;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSentryDsn() {
        return sentryDsn;
    }

    public void setSentryDsn(String sentryDsn) {
        this.sentryDsn = sentryDsn;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}

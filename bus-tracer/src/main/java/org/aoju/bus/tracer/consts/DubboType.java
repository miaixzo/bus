package org.aoju.bus.tracer.consts;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public enum DubboType {

    PROVIDER("Provider"),
    CONSUMER("Consumer");

    private String type;

    DubboType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}

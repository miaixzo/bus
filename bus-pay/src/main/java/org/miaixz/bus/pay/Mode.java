package org.miaixz.bus.pay;

/**
 * 平台模式
 */
public enum Mode {

    /**
     * 商户模式
     */
    BUSINESS_MODEL("BUSINESS_MODEL"),
    /**
     * 服务商模式
     */
    SERVICE_MODE("SERVICE_MODE");

    /**
     * 模式
     */
    private final String value;

    Mode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

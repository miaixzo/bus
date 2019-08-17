package org.aoju.bus.storage;

/**
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
public enum Provider {

    /**
     * 阿里云
     */
    ALI_OSS("阿里云oss", "aliyun"),
    /**
     * 七牛云
     */
    QINIU_OSS("七牛云oss", "qiniu");

    String name;
    String value;

    Provider(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}

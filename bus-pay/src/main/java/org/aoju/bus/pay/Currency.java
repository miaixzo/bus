package org.aoju.bus.pay;

/**
 * 基础货币
 */
public enum Currency {

    CNY("人民币"),
    USD("美元"),
    HKD("港币"),
    MOP("澳门元"),
    EUR("欧元"),
    TWD("新台币"),
    KRW("韩元"),
    JPY("日元"),
    SGD("新加坡元"),
    AUD("澳大利亚元");

    private String name;

    /**
     * 构造函数
     *
     * @param name
     */
    Currency(String name) {
        this.name = name;
    }

    /**
     * 货币名称
     *
     * @return 货币名称
     */
    public String getName() {
        return name;
    }

}

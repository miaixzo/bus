package org.aoju.bus.notify;

/**
 * 服务商标识,通常使用枚举实现此接口
 */
public interface Provider {

    /**
     * @return 唯一标识
     */
    String getId();

    /**
     * @return 名称
     */
    String getName();

}

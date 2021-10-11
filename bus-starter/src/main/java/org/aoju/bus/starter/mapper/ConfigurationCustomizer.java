package org.aoju.bus.starter.mapper;

import org.apache.ibatis.session.Configuration;

/**
 * 自定义自动配置生成的{@link Configuration}对象的回调接口
 */
public interface ConfigurationCustomizer {

    /**
     * 自定义给定的{@link Configuration}对象
     *
     * @param configuration 要自定义的配置对象
     */
    void customize(Configuration configuration);

}

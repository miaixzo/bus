package org.aoju.bus.spring.mapper;

import org.apache.ibatis.session.Configuration;


/**
 * Callback interface that can be customized a {@link Configuration} object generated on auto-configuration.
 *
 * @author Kazuki Shimizu
 * @since 1.2.1
 */
public interface PluginConfiguration {

    /**
     * Customize the given a {@link Configuration} object.
     *
     * @param configuration the configuration object to customize
     */
    void plugin(Configuration configuration);

}

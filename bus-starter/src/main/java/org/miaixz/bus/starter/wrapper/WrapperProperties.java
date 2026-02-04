/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.wrapper;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration properties for the request/response wrapper filter.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.WRAPPER)
public class WrapperProperties {

    /**
     * The name of this registration. If not specified, the bean name will be used.
     */
    private String name = "_wrapper";

    /**
     * The order of the registered filter bean. Default is 100.
     */
    private int order = 100;

    /**
     * An access prefix to be applied.
     */
    private String prefix = Normal.EMPTY;

    /**
     * Flag to indicate if this filter registration is enabled. Default is true.
     */
    private boolean enabled = true;

    /**
     * Base packages to scan for controllers. Ant-style path patterns can be used. The main purpose is to apply a
     * specific prefix to these controllers.
     */
    private String[] basePackages;

    /**
     * Whether to store the API addresses found after scanning the packages. Used in conjunction with
     * {@code basePackages}.
     */
    private boolean inStorage;

    /**
     * Specifies the auto-type handling for JSON serialization/deserialization (e.g., for Fastjson or Jackson). This
     * feature includes type information in the JSON string, allowing for automatic type recognition during
     * deserialization without needing to pass the type explicitly.
     */
    private String autoType;

    /**
     * Initialization parameters for this registration. Calling this will replace any existing parameters.
     */
    private Map<String, String> initParameters = new LinkedHashMap<>();

    /**
     * The servlet names that the filter will be registered against. This will replace any previously specified servlet
     * names.
     */
    private Set<String> servletNames = new LinkedHashSet<>();

    /**
     * The ServletRegistrationBeans that the filter will be registered against.
     */
    private Set<ServletRegistrationBean<?>> servletRegistrationBeans = new LinkedHashSet<>();

}

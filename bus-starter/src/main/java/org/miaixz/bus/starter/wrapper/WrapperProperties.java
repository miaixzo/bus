/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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

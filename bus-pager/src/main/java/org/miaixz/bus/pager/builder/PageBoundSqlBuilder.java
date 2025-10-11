/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.pager.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Builder;

/**
 * Configurator for the {@link BoundSqlBuilder} interceptor chain, responsible for initializing and managing the SQL
 * binding processor chain.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageBoundSqlBuilder {

    /**
     * The {@link BoundSqlBuilder.Chain} instance that processes BoundSql.
     */
    private BoundSqlBuilder.Chain chain;

    /**
     * Configures the {@link BoundSqlBuilder} interceptor chain based on the provided properties. It initializes a list
     * of handlers from the "boundSqlInterceptors" property and constructs a chain.
     *
     * @param properties the configuration properties, typically containing "boundSqlInterceptors"
     */
    public void setProperties(Properties properties) {
        // Initialize boundSqlInterceptorChain
        String boundSqlInterceptors = properties.getProperty("boundSqlInterceptors");
        if (StringKit.isEmpty(boundSqlInterceptors)) {
            return;
        }

        List<BoundSqlBuilder> handlers = Arrays.stream(boundSqlInterceptors.split("[;|,]"))
                .map(className -> (BoundSqlBuilder) Builder.newInstance(className.trim(), properties)).toList();

        if (!handlers.isEmpty()) {
            chain = new BoundSqlChainBuilder(null, handlers);
        }
    }

    /**
     * Retrieves the configured {@link BoundSqlBuilder.Chain}.
     *
     * @return the BoundSql processor chain instance
     */
    public BoundSqlBuilder.Chain getChain() {
        return chain;
    }

}

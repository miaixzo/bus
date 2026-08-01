/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.http;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.miaixz.bus.spring.options.WrapperRuntimeOptions;

/**
 * Applies repeatable-read request and response wrappers without managing any runtime context.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CachedBodyFilter implements Filter {

    private final WrapperRuntimeOptions options;

    /** Creates a filter using the current wrapper options. */
    public CachedBodyFilter() {
        this(WrapperRuntimeOptions.of());
    }

    /**
     * Creates a filter using an explicit immutable options snapshot.
     *
     * @param options wrapper options; {@code null} uses the current shared options
     */
    public CachedBodyFilter(WrapperRuntimeOptions options) {
        this.options = options == null ? WrapperRuntimeOptions.of() : options;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            throw new ServletException("CachedBodyFilter only supports HTTP requests");
        }

        HttpServletRequest wrappedRequest = httpRequest;
        HttpServletResponse wrappedResponse = httpResponse;
        if (this.options.shouldWrap(httpRequest) && !(httpRequest instanceof CachedBodyRequestWrapper)) {
            wrappedRequest = new CachedBodyRequestWrapper(httpRequest);
        }
        if (!(httpResponse instanceof CachedBodyResponseWrapper)) {
            wrappedResponse = new CachedBodyResponseWrapper(httpResponse);
        }
        filterChain.doFilter(wrappedRequest, wrappedResponse);
    }

}

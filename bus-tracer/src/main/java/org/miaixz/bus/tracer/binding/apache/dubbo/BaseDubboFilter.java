/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.tracer.binding.apache.dubbo;

import org.apache.dubbo.rpc.Filter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 基础dubbo过滤器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class BaseDubboFilter implements Filter, Filter.Listener {

    private static final String[] DUBBO_INNER_SERVICE_NAMES = new String[]{
            "com.alibaba.cloud.dubbo.service.DubboMetadataService",
            "org.apache.dubbo.rpc.service.GenericService"
    };

    boolean isDubboInnerService(String serviceName) {
        for (String dubboInnerServiceName : DUBBO_INNER_SERVICE_NAMES) {
            if (dubboInnerServiceName.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    String withUnknown(String text) {
        return StringKit.isBlank(text) ? "unknown" : text;
    }

    String getAsyncIdTrace(String asyncId) {
        return null != asyncId ? "-[异步ID: " + asyncId + "]" : Normal.EMPTY;
    }

}

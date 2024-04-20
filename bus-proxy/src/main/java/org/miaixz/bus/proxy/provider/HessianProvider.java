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
package org.miaixz.bus.proxy.provider;

import com.caucho.hessian.client.HessianProxyFactory;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.proxy.Provider;

import java.net.MalformedURLException;

/**
 * Hessian 服务提供
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HessianProvider implements Provider {

    private Class serviceInterface;
    private String url;

    public HessianProvider() {

    }

    public HessianProvider(Class serviceInterface, String url) {
        this.serviceInterface = serviceInterface;
        this.url = url;
    }

    public Object getObject() {
        try {
            return new HessianProxyFactory().create(serviceInterface, url);
        } catch (MalformedURLException e) {
            throw new InternalException("Invalid url given.", e);
        }
    }

    public void setServiceInterface(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}


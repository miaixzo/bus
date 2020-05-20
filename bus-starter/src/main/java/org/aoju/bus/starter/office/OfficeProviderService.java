/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.starter.office;

import lombok.RequiredArgsConstructor;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Provider;
import org.aoju.bus.office.Registry;
import org.aoju.bus.office.provider.LocalOfficeProvider;
import org.aoju.bus.office.provider.OnlineOfficeProvider;
import org.springframework.stereotype.Component;

/**
 * 文档在线预览服务提供
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
@Component
@RequiredArgsConstructor
public class OfficeProviderService {

    public OfficeProviderService(Provider localProvider,
                                 Provider onlineProvider) {
        Registry.getInstance().register(Registry.LOCAL, localProvider);
        Registry.getInstance().register(Registry.ONLINE, onlineProvider);
    }

    public Provider get(String type) {
        if (Registry.getInstance().contains(type)) {
            if (Registry.LOCAL.equals(type)) {
                return (LocalOfficeProvider) Registry.getInstance().require(Registry.LOCAL);
            }
            if (Registry.ONLINE.equals(type)) {
                return (OnlineOfficeProvider) Registry.getInstance().require(Registry.ONLINE);
            }
        }
        throw new InstrumentException(Builder.FAILURE);
    }

}

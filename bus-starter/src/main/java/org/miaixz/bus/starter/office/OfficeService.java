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
package org.miaixz.bus.starter.office;

import lombok.RequiredArgsConstructor;
import org.miaixz.bus.office.Provider;
import org.miaixz.bus.office.Registry;
import org.springframework.stereotype.Component;

/**
 * Service for providing online document viewing capabilities.
 * <p>
 * This service is responsible for registering different document conversion providers (e.g., for local and online
 * conversions) into a central registry upon application startup.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@RequiredArgsConstructor
public class OfficeService {

    /**
     * Constructs the OfficeService and registers the local and online providers.
     * <p>
     * This constructor is automatically called by Spring, which injects the configured local and online provider beans.
     * These providers are then registered with the global {@link Registry}.
     * </p>
     *
     * @param localProvider  The provider for local document conversions.
     * @param onlineProvider The provider for online or remote document conversions.
     */
    public OfficeService(Provider localProvider, Provider onlineProvider) {
        Registry.getInstance().register(Registry.LOCAL, localProvider);
        Registry.getInstance().register(Registry.ONLINE, onlineProvider);
    }

}

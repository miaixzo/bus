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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.logger.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Timeout implements Runnable {

    private final Association as;
    private final String expiredMsg;
    private final String cancelMsg;
    private final ScheduledFuture<?> future;

    private Timeout(Association as,
                    String expiredMsg,
                    String cancelMsg,
                    int timeout) {
        this.as = as;
        this.expiredMsg = expiredMsg;
        this.cancelMsg = cancelMsg;
        this.future = as.getDevice()
                .schedule(this, timeout, TimeUnit.MILLISECONDS);
    }

    public static Timeout start(Association as,
                                String startMsg,
                                String expiredMsg,
                                String cancelMsg,
                                int timeout) {
        Logger.debug(startMsg, as, timeout);
        return new Timeout(as, expiredMsg, cancelMsg, timeout);
    }

    public void stop() {
        Logger.debug(cancelMsg, as);
        future.cancel(false);
    }

    @Override
    public void run() {
        Logger.info(expiredMsg, as);
        as.abort();
    }

}

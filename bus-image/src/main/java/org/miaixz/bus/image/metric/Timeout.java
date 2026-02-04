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
package org.miaixz.bus.image.metric;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Timeout implements Runnable {

    private final Association as;
    private final String expiredMsg;
    private final String cancelMsg;
    private final ScheduledFuture<?> future;

    private Timeout(Association as, String expiredMsg, String cancelMsg, int timeout) {
        this.as = as;
        this.expiredMsg = expiredMsg;
        this.cancelMsg = cancelMsg;
        this.future = as.getDevice().schedule(this, timeout, TimeUnit.MILLISECONDS);
    }

    public static Timeout start(Association as, String startMsg, String expiredMsg, String cancelMsg, int timeout) {
        Logger.debug(startMsg, as, timeout);
        return new Timeout(as, expiredMsg, cancelMsg, timeout);
    }

    public void stop() {
        Logger.debug(cancelMsg, as);
        future.cancel(false);
    }

    @Override
    public void run() {
        synchronized (as) {
            Logger.info(expiredMsg, as);
            as.abort();
        }
    }

}

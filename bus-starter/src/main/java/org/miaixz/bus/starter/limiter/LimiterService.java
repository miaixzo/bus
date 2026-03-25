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
package org.miaixz.bus.starter.limiter;

import org.miaixz.bus.limiter.Context;
import org.miaixz.bus.limiter.Holder;
import org.springframework.beans.factory.InitializingBean;

/**
 * A service that initializes the global context for the rate limiting and circuit breaking framework.
 * <p>
 * This class implements {@link InitializingBean} to ensure that the limiter {@link Context} is set in a static
 * {@link Holder} as soon as the Spring bean is initialized. This makes the context globally accessible to the
 * framework's components, such as proxies and strategy providers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LimiterService implements InitializingBean {

    /**
     * The limiter context, containing configuration and strategy information.
     */
    public final Context context;

    /**
     * Constructs a new LimiterService with the given context.
     *
     * @param context The limiter context to be set globally.
     */
    public LimiterService(Context context) {
        this.context = context;
    }

    /**
     * Called by the Spring container after the bean has been constructed and all properties have been set.
     * <p>
     * This method sets the provided {@link Context} into the static {@link Holder}, making it available throughout the
     * application for the limiter framework.
     * </p>
     */
    @Override
    public void afterPropertiesSet() {
        Holder.set(context);
    }

}

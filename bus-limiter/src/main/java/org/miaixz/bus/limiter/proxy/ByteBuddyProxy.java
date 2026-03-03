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
package org.miaixz.bus.limiter.proxy;

import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.attribute.MethodAttributeAppender;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * A utility class for creating ByteBuddy proxies to intercept method calls. This class generates a subclass of a given
 * original class and intercepts all its methods using a {@link ByteBuddyHandler} to apply limiting rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteBuddyProxy {

    /**
     * The target object (bean) for which the proxy is being created. Method calls on the proxy will be delegated to
     * this bean.
     */
    public final Object bean;

    /**
     * The original class of the target bean. The proxy will be a subclass of this class.
     */
    private final Class<?> originalClazz;

    /**
     * Constructs a new {@code ByteBuddyProxy} with the specified target bean and its original class.
     *
     * @param bean          The target object to be proxied.
     * @param originalClazz The original class of the target object.
     */
    public ByteBuddyProxy(Object bean, Class<?> originalClazz) {
        this.bean = bean;
        this.originalClazz = originalClazz;
    }

    /**
     * Creates and returns a ByteBuddy proxy instance for the {@link #bean}. The proxy intercepts all method calls and
     * delegates them to a {@link ByteBuddyHandler} for applying limiting logic.
     *
     * @return A proxied instance of the original class.
     * @throws Exception if an error occurs during proxy creation.
     */
    public Object proxy() throws Exception {
        Logger.debug("proxy {}.", originalClazz.getSimpleName());
        return new ByteBuddy().subclass(originalClazz)
                .name(StringKit.format("{}$ByteBuddy${}", originalClazz.getName(), DateKit.current()))
                .method(ElementMatchers.any()).intercept(InvocationHandlerAdapter.of(new ByteBuddyHandler(this)))
                .attribute(MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER)
                .annotateType(bean.getClass().getAnnotations()).make()
                .load(ByteBuddyProxy.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded()
                .getConstructor().newInstance();
    }

}

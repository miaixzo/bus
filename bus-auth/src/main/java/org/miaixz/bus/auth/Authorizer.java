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
package org.miaixz.bus.auth;

import java.util.Arrays;
import java.util.function.Function;

import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.nimble.AbstractProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Authorization module builder, used to quickly construct authentication providers. It uses the builder pattern to
 * configure the authentication source, context, cache, and protocol configurations, dynamically creating corresponding
 * authentication provider instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Authorizer {

    /**
     * Authentication source (e.g., TWITTER, SAML).
     */
    private String source;
    /**
     * Context configuration, containing protocol-specific parameters.
     */
    private Context context;
    /**
     * Cache implementation, used to store temporary data such as state.
     */
    private CacheX cache;
    /**
     * Array of custom protocol configurations.
     */
    private Complex[] complex;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Authorizer() {

    }

    /**
     * Creates an Authorizer builder instance.
     *
     * @return a new Authorizer instance
     */
    public static Authorizer builder() {
        return new Authorizer();
    }

    /**
     * Sets the authentication source.
     *
     * @param source the authentication source (e.g., TWITTER, SAML_EXAMPLE)
     * @return the current Authorizer instance
     */
    public Authorizer source(String source) {
        this.source = source;
        return this;
    }

    /**
     * Sets the context configuration.
     *
     * @param context the context configuration object
     * @return the current Authorizer instance
     */
    public Authorizer context(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Dynamically sets the context configuration using a function.
     *
     * @param context a function that generates the context configuration based on the source
     * @return the current Authorizer instance
     */
    public Authorizer context(Function<String, Context> context) {
        this.context = context.apply(this.source);
        return this;
    }

    /**
     * Sets the cache implementation.
     *
     * @param cache the cache object
     * @return the current Authorizer instance
     */
    public Authorizer cache(CacheX cache) {
        this.cache = cache;
        return this;
    }

    /**
     * Sets custom protocol configurations.
     *
     * @param complex an array of protocol configurations
     * @return the current Authorizer instance
     */
    public Authorizer complex(Complex... complex) {
        this.complex = complex;
        return this;
    }

    /**
     * Builds an authentication provider instance. It finds the matching {@link Complex} based on the configured source
     * and dynamically creates the corresponding provider instance.
     *
     * @return an authentication provider instance
     * @throws AuthorizedException if the source or context is not set, or if no matching {@link Complex} is found
     */
    public Provider build() {
        // Validate if source and context are set
        if (StringKit.isEmpty(this.source) || null == this.context) {
            throw new AuthorizedException(ErrorCode._110001.getKey());
        }

        // Merge default Registry and custom Complex configurations
        Complex[] complexes = this.concat(Registry.values(), this.complex);

        // Filter for the Complex that matches the source
        Complex complex = Arrays.stream(complexes).distinct()
                .filter(authSource -> authSource.getName().equalsIgnoreCase(this.source)).findAny()
                .orElseThrow(() -> new AuthorizedException(ErrorCode._110001.getKey()));

        // Get the provider class
        Class<? extends AbstractProvider> targetClass = complex.getTargetClass();
        if (null == targetClass) {
            throw new AuthorizedException(ErrorCode._110001.getKey());
        }

        // Dynamically create the provider instance
        try {
            if (this.cache == null) {
                return targetClass.getDeclaredConstructor(Context.class).newInstance(this.context);
            } else {
                return targetClass.getDeclaredConstructor(Context.class, CacheX.class)
                        .newInstance(this.context, this.cache);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthorizedException(ErrorCode._110001.getKey());
        }
    }

    /**
     * Concatenates two arrays of {@link Complex} objects.
     *
     * @param first  the first array of Complex objects (usually default configurations)
     * @param second the second array of Complex objects (custom configurations)
     * @return the concatenated array of Complex objects
     */
    private Complex[] concat(Complex[] first, Complex[] second) {
        if (null == second || second.length == 0) {
            return first;
        }
        Complex[] result = new Complex[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}

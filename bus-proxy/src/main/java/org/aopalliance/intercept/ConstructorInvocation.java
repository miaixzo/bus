package org.aopalliance.intercept;

import java.lang.reflect.Constructor;

/**
 * Description of an invocation to a constructor, given to an
 * interceptor upon constructor-call.
 * A constructor invocation is a joinpoint and can be intercepted
 * by a constructor interceptor.
 *
 * @see ConstructorInterceptor
 */
public interface ConstructorInvocation extends Invocation {

    /**
     * Get the constructor being called.
     * This method is a friendly implementation of the
     * {@link Joinpoint#getStaticPart()} method (same result).
     *
     * @return the constructor being called
     */
    Constructor<?> getConstructor();

}

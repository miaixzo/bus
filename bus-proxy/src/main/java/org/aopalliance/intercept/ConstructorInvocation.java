package org.aopalliance.intercept;


import java.lang.reflect.Constructor;

/**
 * Description of an invocation to a constuctor, given to an
 * interceptor upon construtor-call.
 *
 * <p>A constructor invocation is a joinpoint and can be intercepted
 * by a constructor interceptor.
 *
 * @see ConstructorInterceptor
 */
public interface ConstructorInvocation extends Invocation {

    /**
     * Gets the constructor being called.
     * <p>
     * This method is a frienly implementation
     * of the {@link Joinpoint#getStaticPart()}
     * method (same result).
     *
     * @return the constructor being called.
     */
    Constructor getConstructor();

}

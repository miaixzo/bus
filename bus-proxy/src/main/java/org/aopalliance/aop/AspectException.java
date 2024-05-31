package org.aopalliance.aop;

/**
 * Superclass for all AOP infrastructure exceptions.
 * Unchecked, as such exceptions are fatal and end user
 * code shouldn't be forced to catch them.
 */
public class AspectException extends RuntimeException {

    /**
     * Constructor for AspectException.
     *
     * @param message the exception message
     */
    public AspectException(String message) {
        super(message);
    }

    /**
     * Constructor for AspectException.
     *
     * @param message the exception message
     * @param cause   the root cause, if any
     */
    public AspectException(String message, Throwable cause) {
        super(message, cause);
    }

}

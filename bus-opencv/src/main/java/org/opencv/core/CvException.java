package org.opencv.core;

/**
 * The cv exception class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CvException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CvException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        return "CvException [" + super.toString() + "]";
    }

}

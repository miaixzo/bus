package com.ukettle.www.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class uKettleException extends RuntimeException {

	static final long serialVersionUID = 1662900257135756746L;

	public uKettleException() {
		super();
	}

	public uKettleException(String message) {
		super(message);
	}

	public uKettleException(String message, Throwable cause) {
		super(message, cause);
	}

	public uKettleException(Throwable cause) {
		super(cause);
	}

	/**
	 * 将CheckedException转换为UncheckedException.
	 */
	public static RuntimeException unchecked(Exception e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	/**
	 * 将ErrorStack转化为String.
	 */
	public static String getStackTraceAsString(Exception e) {
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

	/**
	 * 判断异常是否由某些底层的异常引起.
	 */
	public static boolean isCausedBy(Exception ex,
			Class<? extends Exception>... causeExceptionClasses) {
		Throwable cause = ex.getCause();
		while (cause != null) {
			for (Class<? extends Exception> causeClass : causeExceptionClasses) {
				if (causeClass.isInstance(cause)) {
					return true;
				}
			}
			cause = cause.getCause();
		}
		return false;
	}
}
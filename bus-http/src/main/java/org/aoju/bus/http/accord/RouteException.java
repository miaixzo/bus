/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.accord;

import org.aoju.bus.http.Internal;

import java.io.IOException;

/**
 * An exception thrown to indicate a problem connecting via a single Route. Multiple attempts may
 * have been made with alternative protocols, none of which were successful.
 *
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public final class RouteException extends RuntimeException {

    private IOException firstException;
    private IOException lastException;

    public RouteException(IOException cause) {
        super(cause);
        firstException = cause;
        lastException = cause;
    }

    public IOException getFirstConnectException() {
        return firstException;
    }

    public IOException getLastConnectException() {
        return lastException;
    }

    public void addConnectException(IOException e) {
        Internal.addSuppressedIfPossible(firstException, e);
        lastException = e;
    }

}

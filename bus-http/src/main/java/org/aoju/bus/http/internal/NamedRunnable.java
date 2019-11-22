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
package org.aoju.bus.http.internal;

import org.aoju.bus.core.utils.StringUtils;

/**
 * Runnable implementation which always sets its thread name.
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public abstract class NamedRunnable implements Runnable {

    protected final String name;

    public NamedRunnable(String format, Object... args) {
        this.name = StringUtils.format(format, args);
    }

    @Override
    public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    protected abstract void execute();

}

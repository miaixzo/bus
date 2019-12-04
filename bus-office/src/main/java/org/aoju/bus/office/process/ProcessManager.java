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
package org.aoju.bus.office.process;

import org.aoju.bus.office.support.ProcessQuery;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
public interface ProcessManager {

    long PID_NOT_FOUND = -2;
    long PID_UNKNOWN = -1;

    void kill(Process process, long pid) throws IOException;

    /**
     * @param query the object
     * @return the pid if found, {@link #PID_NOT_FOUND} if not,
     * or {@link #PID_UNKNOWN} if this implementation is unable to find out
     * @throws IOException exception
     */
    long findPid(ProcessQuery query) throws IOException;

}

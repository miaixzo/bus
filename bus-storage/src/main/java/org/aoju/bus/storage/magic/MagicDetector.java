/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.storage.magic;

import java.io.File;
import java.util.Map;


/**
 * 探测器
 *
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public interface MagicDetector {

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getName();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getDisplayName();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getVersion();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String[] getHandledTypes();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String[] getHandledExtensions();

    /**
     * DOCUMENT ME!
     *
     * @param data       DOCUMENT ME!
     * @param offset     DOCUMENT ME!
     * @param length     DOCUMENT ME!
     * @param bitmask    DOCUMENT ME!
     * @param comparator DOCUMENT ME!
     * @param mimeType   DOCUMENT ME!
     * @param params     DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    String[] process(byte[] data, int offset, int length, long bitmask, char comparator,
                     String mimeType, Map<String, String> params);

    /**
     * DOCUMENT ME!
     *
     * @param file       DOCUMENT ME!
     * @param offset     DOCUMENT ME!
     * @param length     DOCUMENT ME!
     * @param bitmask    DOCUMENT ME!
     * @param comparator DOCUMENT ME!
     * @param mimeType   DOCUMENT ME!
     * @param params     DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    String[] process(File file, int offset, int length, long bitmask, char comparator,
                     String mimeType, Map<String, String> params);

}

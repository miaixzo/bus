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
package org.aoju.bus.forest;

import org.aoju.bus.core.consts.Symbol;

/**
 * 常量表
 *
 * @author Kimi Liu
 * @version 3.2.0
 * @since JDK 1.8
 */
public interface Consts {

    String BOOT_INF_CLASSES = "BOOT-INF/classes/";
    String BOOT_INF_LIB = "BOOT-INF/lib/";

    String WEB_INF_CLASSES = "WEB-INF/classes/";
    String WEB_INF_LIB = "WEB-INF/lib/";

    String META_INF_MANIFEST = "META-INF/MANIFEST.MF";
    String XJAR_SRC_DIR = Consts.class.getPackage().getName().replace(Symbol.C_DOT, Symbol.C_SLASH) + Symbol.SLASH;
    String XJAR_INF_DIR = "META-INF/";
    String XJAR_INF_IDX = "FOREST.MF";
    String CRLF = System.getProperty("line.separator");

    String XJAR_ALGORITHM = "--xjar.algorithm=";
    String XJAR_KEYSIZE = "--xjar.keysize=";
    String XJAR_IVSIZE = "--xjar.ivsize=";
    String XJAR_PASSWORD = "--xjar.password=";
    String XJAR_KEYFILE = "--xjar.keyfile=";

    String XJAR_ALGORITHM_KEY = "Jar-Algorithm";
    String XJAR_KEYSIZE_KEY = "Jar-Keysize";
    String XJAR_IVSIZE_KEY = "Jar-Ivsize";
    String XJAR_PASSWORD_KEY = "Jar-Password";

    String XJAR_KEY_ALGORITHM = "algorithm";
    String XJAR_KEY_KEYSIZE = "keysize";
    String XJAR_KEY_IVSIZE = "ivsize";
    String XJAR_KEY_PASSWORD = "password";
    String XJAR_KEY_HOLD = "hold";

    String DEFAULT_ALGORITHM = "AES";

    int DEFAULT_KEYSIZE = 128;
    int DEFAULT_IVSIZE = 128;

    // 保留密钥在 META-INF/MANIFEST.MF 中，启动时无需输入密钥。
    int FLAG_DANGER = 1;
    // 危险模式：保留密钥
    int MODE_DANGER = FLAG_DANGER;
    // 普通模式
    int MODE_NORMAL = 0;

}

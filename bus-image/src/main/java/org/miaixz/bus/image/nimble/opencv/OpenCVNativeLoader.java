/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble.opencv;

import java.io.IOException;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.loader.Loaders;
import org.miaixz.bus.health.Platform;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.Core;

/**
 * OpenCV动态库加载 1. 默认加载运行环境下的opencv动态库 2. 加载失败会重试加载jar中的opencv动态库
 *
 * @author Kimi Liu
 * @since Java 17+
 *
 */
public class OpenCVNativeLoader extends org.opencv.osgi.OpenCVNativeLoader {

    public static void loader() {
        new OpenCVNativeLoader().init();
    }

    public void init() {
        try {
            super.init();
        } catch (UnsatisfiedLinkError e) {
            try {
                Loaders.nat()
                        .load(Symbol.SLASH + Normal.LIB_PROTOCOL_JAR + Symbol.SLASH
                                + Platform.getNativeLibraryResourcePrefix() + Symbol.SLASH
                                + System.mapLibraryName(Core.NATIVE_LIBRARY_NAME),
                                org.opencv.osgi.OpenCVNativeLoader.class);
            } catch (IOException ie) {
                Logger.error("Failed to load the native OpenCV library.");
            }
            Logger.info("Successfully loaded OpenCV native library.");
        }
    }

}

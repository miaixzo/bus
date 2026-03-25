/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
                Loaders.nat().load(
                        Symbol.SLASH + Normal.LIB_PROTOCOL_JAR + Symbol.SLASH
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

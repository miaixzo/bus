package org.aoju.bus.image.nimble.opencv;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.loader.Loaders;
import org.aoju.bus.health.Platform;
import org.aoju.bus.logger.Logger;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * OpenCV动态库加载
 * 1. 默认加载运行环境下的opencv动态库
 * 2. 加载失败会重试加载jar中的opencv动态库
 */
public class OpenCVNativeLoader extends org.opencv.osgi.OpenCVNativeLoader {

    public void init() {
        try {
            super.init();
        } catch (UnsatisfiedLinkError e) {
            try {
                Loaders.nat().load(Symbol.SLASH + Normal.LIB_PROTOCOL_JAR
                                + Symbol.SLASH + Platform.getNativeLibraryResourcePrefix() + Symbol.SLASH
                                + System.mapLibraryName(Core.NATIVE_LIBRARY_NAME).replace(".dylib", ".jnilib"),
                        org.opencv.osgi.OpenCVNativeLoader.class);
            } catch (IOException ie) {
                Logger.error("Failed to load the native OpenCV library.");
            }
            Logger.info("Successfully loaded OpenCV native library.");
        }
    }

}

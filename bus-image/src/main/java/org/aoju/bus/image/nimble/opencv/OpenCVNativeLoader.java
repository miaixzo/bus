package org.aoju.bus.image.nimble.opencv;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.loader.Loaders;
import org.aoju.bus.health.Platform;
import org.aoju.bus.logger.Logger;
import org.opencv.core.Core;

import java.io.IOException;

public class OpenCVNativeLoader extends org.opencv.osgi.OpenCVNativeLoader {

    @Override
    public void init() {
        try {
            org.opencv.osgi.OpenCVNativeLoader loader = new org.opencv.osgi.OpenCVNativeLoader();
            loader.init();
        } catch (UnsatisfiedLinkError e) {
            try {
                Loaders.nat().load(Symbol.SLASH + Normal.LIB_PROTOCOL_JAR
                                + Symbol.SLASH + Platform.getNativeLibraryResourcePrefix()
                                + Symbol.SLASH + System.mapLibraryName(Core.NATIVE_LIBRARY_NAME),
                        org.opencv.osgi.OpenCVNativeLoader.class);
            } catch (IOException ie) {
                throw new InstrumentException(ie);
            }
        }
        Logger.info("Successfully loaded OpenCV native library.");
    }

}

package org.opencv.osgi;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.loader.Loaders;
import org.aoju.bus.health.Platform;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is intended to provide a convenient way to load OpenCV's native
 * library from the Java bundle. If Blueprint is enabled in the OSGi container
 * this class will be instantiated automatically and the init() method called
 * loading the native library.
 */
public class OpenCVNativeLoader implements OpenCVInterface {

    public void init() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            try {
                Loaders.nat().load(Symbol.SLASH + Normal.LIB_PROTOCOL_JAR
                                + Symbol.SLASH + Platform.getNativeLibraryResourcePrefix()
                                + Symbol.SLASH + System.mapLibraryName(Core.NATIVE_LIBRARY_NAME),
                        org.opencv.osgi.OpenCVNativeLoader.class);
            } catch (IOException ie) {
                Logger.getLogger("org.opencv.osgi").log(Level.INFO, "Failed to load the native OpenCV library.");
            }
        }
        Logger.getLogger("org.opencv.osgi").log(Level.INFO, "Successfully loaded OpenCV native library.");
    }

}

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
/**
 * Module: {@code bus.opencv}
 *
 * <p>
 * Provides Java bindings and native libraries for OpenCV image processing and computer vision.
 *
 * <p>
 * Includes matrix and geometry operations, image codecs, image transformations, perceptual hashing, native library
 * loading, and OSGi integration.
 *
 * @author Kimi Liu
 */
module bus.opencv {

    exports org.miaixz.bus.opencv.core;
    exports org.miaixz.bus.opencv.geometry;
    exports org.miaixz.bus.opencv.img_hash;
    exports org.miaixz.bus.opencv.imgcodecs;
    exports org.miaixz.bus.opencv.imgproc;
    exports org.miaixz.bus.opencv.osgi;
    exports org.miaixz.bus.opencv.utils;

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.starter.image;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for image processing and analysis, particularly for DICOM.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.IMAGE)
public class ImageProperties {

    /**
     * Configuration for the DICOM node/server.
     */
    private Node node = new Node();

    /**
     * Whether to enable OpenCV for advanced image processing tasks.
     */
    private boolean opencv;

    /**
     * Whether to enable the DICOM server functionality.
     */
    private boolean server;

    /**
     * The directory path for saving original DICOM (.dcm) files.
     */
    private String dcmDir;

    /**
     * The directory path for saving converted image files (e.g., JPEG, PNG).
     */
    private String imgDir;

    /**
     * Nested class for DICOM server (node) information.
     */
    @Getter
    @Setter
    public class Node {

        /**
         * The hostname or IP address of the DICOM server.
         */
        private String host;

        /**
         * The port number of the DICOM server.
         */
        private String port;

        /**
         * The Application Entity (AE) title of the DICOM server.
         */
        private String aeTitle;

        /**
         * Whether to enable negotiation of transfer syntax by UID or name.
         */
        private boolean negociation;

        /**
         * Specifies the SOP classes and transfer syntaxes by their UID or name. Corresponds to the
         * {@code sop-classes.properties} file.
         */
        private String sopClasses;

        /**
         * Defines related general-purpose SOP classes according to DICOM Part 4, B.3.1.4. Corresponds to the
         * {@code sop-classes-uid.properties} file.
         */
        private String sopClassesUID;

        /**
         * Extended storage transfer capabilities for SOP classes and transfer syntaxes. Corresponds to the
         * {@code sop-classes-tcs.properties} file.
         */
        private String sopClassesTCS;

    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

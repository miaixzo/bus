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
package org.miaixz.bus.starter.image;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable image processing and DICOM server properties.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.IMAGE)
public final class ImageProperties {

    /**
     * Whether the image integration is enabled.
     */
    private final boolean enabled;
    /**
     * Whether native OpenCV integration is enabled.
     */
    private final boolean opencv;
    /**
     * Remote DICOM server connection settings.
     */
    private final boolean server;
    /**
     * Local directory used for DICOM input and intermediate files.
     */
    private final String dcmDir;
    /**
     * Local directory used for rendered image output.
     */
    private final String imgDir;
    /**
     * DICOM peer node definitions keyed by logical name.
     */
    private final Node node;

    /**
     * Creates validated image properties.
     *
     * @param enabled whether image integration is enabled
     * @param opencv  whether OpenCV processing is enabled
     * @param server  whether the DICOM server is enabled
     * @param dcmDir  DICOM storage directory
     * @param imgDir  converted image storage directory
     * @param node    DICOM node options
     */
    public ImageProperties(@DefaultValue("false") boolean enabled, @DefaultValue("false") boolean opencv,
            @DefaultValue("false") boolean server, String dcmDir, String imgDir, @DefaultValue Node node) {
        Node effectiveNode = node == null ? new Node() : node;
        if (effectiveNode.port() != 0 && (effectiveNode.port() < 1 || effectiveNode.port() > 65535)) {
            throw new IllegalArgumentException("bus.image.node.port must be in 1..65535");
        }
        if (opencv && blank(imgDir)) {
            throw new IllegalArgumentException("bus.image.img-dir is required when OpenCV is enabled");
        }
        if (server && (blank(dcmDir) || blank(effectiveNode.host()) || effectiveNode.port() < 1
                || blank(effectiveNode.aeTitle()) || blank(effectiveNode.sopClasses())
                || blank(effectiveNode.sopClassesUID()) || blank(effectiveNode.sopClassesTCS()))) {
            throw new IllegalArgumentException(
                    "DICOM server requires dcm-dir, host, port, ae-title and all three SOP resource paths");
        }
        this.enabled = enabled;
        this.opencv = opencv;
        this.server = server;
        this.dcmDir = dcmDir;
        this.imgDir = imgDir;
        this.node = effectiveNode;
    }

    /**
     * Returns whether a configuration string is absent or blank.
     *
     * @param value configuration string
     * @return {@code true} when the string is absent or blank
     */
    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * DICOM node configuration.
     *
     * @param host          network host
     * @param port          network port
     * @param aeTitle       ae title
     * @param negociation   content-negotiation settings
     * @param sopClasses    sop classes
     * @param sopClassesUID sop classes uid
     * @param sopClassesTCS sop classes tcs
     */
    public record Node(String host, int port, String aeTitle, boolean negociation, String sopClasses,
            String sopClassesUID, String sopClassesTCS) {

        /**
         * Creates empty disabled-server defaults.
         */
        public Node() {
            this(null, 0, null, false, null, null, null);
        }

        /**
         * Exposes the remote DICOM peer host name or address.
         *
         * @return node host
         */
        public String getHost() {
            return host;
        }

        /**
         * Exposes the remote DICOM peer listening port.
         *
         * @return node port
         */
        public int getPort() {
            return port;
        }

        /**
         * Exposes the DICOM application entity title used for association negotiation.
         *
         * @return application entity title
         */
        public String getAeTitle() {
            return aeTitle;
        }

        /**
         * Returns the configured DICOM service-object pair class names.
         *
         * @return SOP classes resource
         */
        public String getSopClasses() {
            return sopClasses;
        }

        /**
         * Returns the configured DICOM service-object pair class UIDs.
         *
         * @return SOP class UID resource
         */
        public String getSopClassesUID() {
            return sopClassesUID;
        }

        /**
         * Returns the configured transfer syntaxes for the service-object pair classes.
         *
         * @return SOP transfer capabilities resource
         */
        public String getSopClassesTCS() {
            return sopClassesTCS;
        }
    }

}

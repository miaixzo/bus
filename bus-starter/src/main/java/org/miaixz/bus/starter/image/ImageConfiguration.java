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

import jakarta.annotation.Resource;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.Args;
import org.miaixz.bus.image.Centre;
import org.miaixz.bus.image.Efforts;
import org.miaixz.bus.image.Node;
import org.miaixz.bus.image.nimble.opencv.OpenCVNativeLoader;
import org.miaixz.bus.image.plugin.StoreSCP;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for DICOM image processing.
 * <p>
 * This class sets up the DICOM Store SCP (Service Class Provider) as a Spring bean, based on the settings provided in
 * {@link ImageProperties}. It conditionally initializes OpenCV and the DICOM server itself.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { ImageProperties.class })
public class ImageConfiguration {

    /**
     * Injected image configuration properties.
     */
    @Resource
    ImageProperties properties;

    /**
     * Injected Efforts bean, likely for handling DICOM processing tasks.
     */
    @Resource
    Efforts efforts;

    /**
     * Creates and configures the DICOM Store SCP bean, represented by the {@link Centre} class.
     * <p>
     * The bean's lifecycle is managed by Spring, with its {@code start} and {@code stop} methods called automatically.
     * The server is only created if {@code bus.image.server} is enabled.
     * </p>
     *
     * @return A configured {@link Centre} instance, or {@code null} if the server is disabled.
     * @throws NullPointerException if essential server properties (aeTitle, host, port) are missing.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Centre onStoreSCP() {
        if (this.properties.isOpencv()) {
            new OpenCVNativeLoader().init();
        }
        if (this.properties.isServer()) {
            if (StringKit.isEmpty(this.properties.getNode().getAeTitle())) {
                throw new NullPointerException("The aeTitle cannot be null.");
            }
            if (StringKit.isEmpty(this.properties.getNode().getHost())) {
                throw new NullPointerException("The host cannot be null.");
            }
            if (StringKit.isEmpty(this.properties.getNode().getPort())) {
                throw new NullPointerException("The port cannot be null.");
            }
            Args args = new Args();
            if (StringKit.isNotEmpty(this.properties.getNode().getSopClasses())) {
                args.setSopClasses(
                        ResourceKit
                                .getResourceUrl(this.properties.getNode().getSopClasses(), ImageConfiguration.class));
            }
            if (StringKit.isNotEmpty(this.properties.getNode().getSopClassesTCS())) {
                args.setSopClassesTCS(
                        ResourceKit.getResourceUrl(
                                this.properties.getNode().getSopClassesTCS(),
                                ImageConfiguration.class));
            }
            if (StringKit.isNotEmpty(this.properties.getNode().getSopClassesUID())) {
                args.setSopClassesUID(
                        ResourceKit.getResourceUrl(
                                this.properties.getNode().getSopClassesUID(),
                                ImageConfiguration.class));
            }
            return Centre.builder().args(args).efforts(efforts)
                    .node(
                            new Node(this.properties.getNode().getAeTitle(), this.properties.getNode().getHost(),
                                    Integer.parseInt(this.properties.getNode().getPort())))
                    .storeSCP(new StoreSCP(this.properties.getDcmDir())).build();
        }
        return null;
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

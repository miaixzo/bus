/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.starter.image;

import jakarta.annotation.Resource;
import org.miaixz.bus.core.toolkit.ResourceKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.image.Args;
import org.miaixz.bus.image.Centre;
import org.miaixz.bus.image.Efforts;
import org.miaixz.bus.image.Node;
import org.miaixz.bus.image.nimble.opencv.OpenCVNativeLoader;
import org.miaixz.bus.image.plugin.StoreSCP;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = {ImageProperties.class})
public class ImageConfiguration {

    @Resource
    ImageProperties properties;

    @Resource
    Efforts efforts;

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
            Args args = new Args(true);
            if (StringKit.isNotEmpty(this.properties.getNode().getRelClass())) {
                args.setExtendSopClassesURL(ResourceKit.getResourceUrl(this.properties.getNode().getRelClass(), ImageConfiguration.class));
            }
            if (StringKit.isNotEmpty(this.properties.getNode().getRelClass())) {
                args.setExtendSopClassesURL(ResourceKit.getResourceUrl(this.properties.getNode().getRelClass(), ImageConfiguration.class));
            }
            if (StringKit.isNotEmpty(this.properties.getNode().getSopClass())) {
                args.setTransferCapabilityFile(ResourceKit.getResourceUrl(this.properties.getNode().getSopClass(), ImageConfiguration.class));
            }
            if (StringKit.isNotEmpty(this.properties.getNode().getTcsClass())) {
                args.setExtendStorageSOPClass(ResourceKit.getResourceUrl(this.properties.getNode().getTcsClass(), ImageConfiguration.class));
            }
            return Centre.builder().args(args).efforts(efforts)
                    .node(new Node(this.properties.getNode().getAeTitle(), this.properties.getNode().getHost(), Integer.valueOf(this.properties.getNode().getPort())))
                    .storeSCP(new StoreSCP(this.properties.getDcmPath())).build();
        }
        return null;
    }

}

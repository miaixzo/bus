/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.starter.image;

import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.image.Args;
import org.aoju.bus.image.Centre;
import org.aoju.bus.image.Efforts;
import org.aoju.bus.image.Node;
import org.aoju.bus.image.nimble.opencv.OpenCVNativeLoader;
import org.aoju.bus.image.plugin.StoreSCP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
@EnableConfigurationProperties(value = {ImageProperties.class})
public class ImageConfiguration {

    @Autowired
    ImageProperties properties;

    @Autowired
    Efforts efforts;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Centre onStoreSCP() {
        if (this.properties.opencv) {
            new OpenCVNativeLoader().init();
        }

        if (StringKit.isEmpty(this.properties.aeTitle)) {
            throw new NullPointerException("The aeTitle cannot be null.");
        }
        if (StringKit.isEmpty(this.properties.host)) {
            throw new NullPointerException("The host cannot be null.");
        }
        if (StringKit.isEmpty(this.properties.port)) {
            throw new NullPointerException("The port cannot be null.");
        }
        Args args = new Args(true);
        if (StringKit.isNotEmpty(this.properties.relClass)) {
            args.setExtendSopClassesURL(FileKit.getResource(this.properties.relClass, ImageConfiguration.class));
        }
        if (StringKit.isNotEmpty(this.properties.relClass)) {
            args.setExtendSopClassesURL(FileKit.getResource(this.properties.relClass, ImageConfiguration.class));
        }
        if (StringKit.isNotEmpty(this.properties.sopClass)) {
            args.setTransferCapabilityFile(FileKit.getResource(this.properties.sopClass, ImageConfiguration.class));
        }
        if (StringKit.isNotEmpty(this.properties.tcsClass)) {
            args.setExtendStorageSOPClass(FileKit.getResource(this.properties.tcsClass, ImageConfiguration.class));
        }

        return Centre.builder().args(args).efforts(efforts)
                .node(new Node(this.properties.aeTitle, this.properties.host, Integer.valueOf(this.properties.port)))
                .storeSCP(new StoreSCP(this.properties.dcmPath)).build();
    }

}

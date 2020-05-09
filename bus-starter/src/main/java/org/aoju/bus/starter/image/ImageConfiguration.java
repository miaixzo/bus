/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.starter.image;

import org.aoju.bus.core.utils.ResourceUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.image.Args;
import org.aoju.bus.image.Centre;
import org.aoju.bus.image.Node;
import org.aoju.bus.image.Rollers;
import org.aoju.bus.image.centre.StoreSCPCentre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
@EnableConfigurationProperties(value = {ImageProperties.class})
public class ImageConfiguration {

    @Autowired
    ImageProperties properties;

    @Autowired
    Rollers rollers;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Centre onStoreSCP() {
        StoreSCPCentre store = StoreSCPCentre.Builder();
        Args args = new Args(true);
        if (StringUtils.isNotEmpty(properties.relClass)) {
            args.setExtendSopClassesURL(ResourceUtils.getResource(properties.relClass, ImageConfiguration.class));
        }
        if (StringUtils.isNotEmpty(properties.sopClass)) {
            args.setTransferCapabilityFile(ResourceUtils.getResource(properties.sopClass, ImageConfiguration.class));
        }
        store.setArgs(args);
        store.setNode(new Node(properties.aeTitle, properties.host, Integer.valueOf(properties.port)));
        store.setRollers(rollers);
        store.setStoreSCP(properties.dcmPath);
        store.setDevice(store.getStoreSCP().getDevice());
        return store.build();
    }

}

package org.aoju.bus.starter.image;

import org.aoju.bus.image.Args;
import org.aoju.bus.image.Centre;
import org.aoju.bus.image.Node;
import org.aoju.bus.image.Rollers;
import org.aoju.bus.image.centre.StoreSCPCentre;
import org.aoju.bus.image.nimble.opencv.OpenCVNativeLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(value = {ImageProperties.class})
public class ImageConfiguration {

    @Autowired
    ImageProperties properties;

    @Autowired
    Rollers rollers;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Centre onStoreSCP() {
        if (properties.opencv) {
            new OpenCVNativeLoader().init();
        }
        StoreSCPCentre store = StoreSCPCentre.Builder();
        store.setArgs(new Args(true));
        store.setNode(new Node(properties.aeTitle, properties.host, Integer.valueOf(properties.ports)));
        store.setRollers(rollers);
        store.setStoreSCP(properties.dcmPath);
        store.setDevice(store.getStoreSCP().getDevice());
        return store.build();
    }

}

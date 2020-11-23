package org.aoju.bus.goalie.support;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.XmlKit;
import org.aoju.bus.goalie.Provider;

public class XmlProvider implements Provider {

    @Override
    public String serialize(Object obj) {
        try {
            return XmlKit.beanToXml(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Normal.EMPTY;
    }

}

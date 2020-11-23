package org.aoju.bus.goalie.support;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.goalie.Provider;

public class JsonProvider implements Provider {

    @Override
    public String serialize(Object obj) {
        return JSON.toJSONString(obj);
    }

}

package org.miaixz.bus.notify.metric.aliyun;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.Property;

@Getter
@Setter
public class AliyunProperty extends Property {

    /**
     * 播放次数
     */
    private String playTimes;

    /**
     * API默认请求地址
     * 当 {@link Context} 中 endpoint 为空时使用地址
     */
    @Override
    public String getUrl() {
        return this.url = "https://dysmsapi.aliyuncs.com/";
    }

}

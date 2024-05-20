package org.miaixz.bus.notify.metric.aliyun;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.notify.magic.Property;

@Data
@SuperBuilder
public class AliyunProperty extends Property {

    /**
     * 播放次数
     */
    private String playTimes;

}

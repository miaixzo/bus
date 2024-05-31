package org.miaixz.bus.notify.metric.aliyun;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.notify.magic.Property;

@Getter
@Setter
@SuperBuilder
public class AliyunProperty extends Property {

    /**
     * 播放次数
     */
    private String playTimes;

}

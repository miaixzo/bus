package org.aoju.bus.notify;

import lombok.AllArgsConstructor;
import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.metric.Properties;
import org.aoju.bus.notify.metric.Template;

import java.util.Map;

/**
 * 抽象类
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@AllArgsConstructor
public abstract class AbstractProvider<T extends Template, K extends Properties> implements Provider<T> {

    protected K properties;

    @Override
    public Message send(String templateId, Map<String, String> context) {
        return null;
    }

    @Override
    public Message send(T template, Map<String, String> context) {
        return null;
    }
}

package org.aoju.bus.office.magic.family;

import java.util.HashMap;
import java.util.Map;

/**
 * 包含将在为特定的{@link DocumentFormat}加载(打开)和存储(保存)文档时应用的属性.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class FormatProperties {

    /**
     * 加载(打开)文档时应用的属性.
     */
    private final Map<String, Object> load = new HashMap<>();

    /**
     * 为每个受支持的家庭存储(保存)文档时应用的属性.
     */
    private final Map<FamilyType, Map<String, Object>> store = new HashMap<>();

    public Map<String, Object> getLoad() {
        return load;
    }

    public Map<FamilyType, Map<String, Object>> getStore() {
        return store;
    }

}

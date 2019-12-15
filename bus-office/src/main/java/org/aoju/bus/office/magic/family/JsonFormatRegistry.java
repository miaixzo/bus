package org.aoju.bus.office.magic.family;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 包含office支持的{@code DocumentFormat}集合，该集合已从JSON源加载.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class JsonFormatRegistry extends SimpleFormatRegistry {

    protected JsonFormatRegistry() {
        super();
    }

    /**
     * 从给定源创建JsonDocumentFormatRegistry.
     *
     * @param source 包含DocumentFormat集合的字符串(JSON格式).
     * @return 创建的JsonDocumentFormatRegistry.
     */
    public static JsonFormatRegistry create(final String source) {
        return create(source, null);
    }

    /**
     * 从给定的InputStream创建JsonDocumentFormatRegistry.
     *
     * @param source 包含DocumentFormat集合的InputStream (JSON格式).
     * @return 创建的JsonDocumentFormatRegistry.
     * @throws IOException 如果发生I/O错误.
     */
    public static JsonFormatRegistry create(final InputStream source) throws IOException {
        return create(source, null);
    }

    /**
     * 从给定的InputStream创建JsonDocumentFormatRegistry.
     *
     * @param source           包含DocumentFormat集合的InputStream (JSON格式).
     * @param customProperties 加载或存储文档时应用的自定义属性.
     * @return 创建的JsonDocumentFormatRegistry.
     * @throws IOException 如果发生I/O错误.
     */
    public static JsonFormatRegistry create(
            final InputStream source,
            final Map<String, FormatProperties> customProperties)
            throws IOException {
        return create(IoUtils.toString(source, "UTF-8"), customProperties);
    }

    /**
     * 从给定源创建JsonDocumentFormatRegistry.
     *
     * @param source           包含DocumentFormat集合的InputStream (JSON格式).
     * @param customProperties 加载或存储文档时应用的自定义属性.
     * @return 创建的JsonDocumentFormatRegistry.
     */
    public static JsonFormatRegistry create(
            final String source,
            final Map<String, FormatProperties> customProperties) {
        final JsonFormatRegistry registry = new JsonFormatRegistry();
        registry.readJsonArray(source, customProperties);
        return registry;
    }

    protected void readJsonArray(final String source, final Map<String, FormatProperties> customProperties) {
        final List<DocumentFormat> list = JSONObject.parseArray(source, DocumentFormat.class);
        list.stream().map(fmt -> {
            if (ObjectUtils.isEmpty(customProperties) || !customProperties.containsKey(fmt.getExtension())) {
                return DocumentFormat.unmodifiableCopy(fmt);
            }
            final FormatProperties props = customProperties.get(fmt.getExtension());
            final DocumentFormat.Builder builder = DocumentFormat.builder().from(fmt).unmodifiable(true);

            props.getLoad().forEach(builder::loadProperty);
            props.getStore().forEach((family, storeProps) -> {
                storeProps.forEach((name, value) -> {
                    builder.storeProperty(family, name, value);
                });
            });
            return builder.build();
        }).forEach(this::addFormat);
    }

}

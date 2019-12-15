package org.aoju.bus.office.magic.family;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * 保存默认的{@link FormatRegistry}实例.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public final class RegistryInstanceHolder {

    private static FormatRegistry instance;

    /**
     * 获取默认的{@link FormatRegistry}实例.
     *
     * @return 默认的{@link FormatRegistry}.
     */
    public static FormatRegistry getInstance() {
        synchronized (FormatRegistry.class) {
            if (ObjectUtils.isEmpty(instance)) {
                try (InputStream input = RegistryInstanceHolder.class.getResourceAsStream(
                        "/document-formats.json")) {
                    instance = JsonFormatRegistry.create(input);
                } catch (IOException ex) {
                    throw new InstrumentException(
                            "Unable to load the default document-formats.json configuration file", ex);
                }
            }
            return instance;
        }
    }

    /**
     * 设置默认的{@link FormatRegistry}实例.
     *
     * @param registry 要设置的{@link FormatRegistry}.
     */
    public static void setInstance(final FormatRegistry registry) {
        synchronized (FormatRegistry.class) {
            instance = registry;
        }
    }

}

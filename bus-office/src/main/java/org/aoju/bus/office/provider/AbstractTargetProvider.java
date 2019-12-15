package org.aoju.bus.office.provider;

import java.io.File;

/**
 * 所有目标文档规范实现的基类.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractTargetProvider extends AbstractDocumentProvider
        implements TargetDocumentProvider {

    protected AbstractTargetProvider(final File file) {
        super(file);
    }

}

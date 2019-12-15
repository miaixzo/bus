package org.aoju.bus.office.provider;

import java.io.File;

/**
 * 所有源文档规范实现的基类.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractSourceProvider extends AbstractDocumentProvider
        implements SourceDocumentProvider {

    protected AbstractSourceProvider(final File file) {
        super(file);
    }

}

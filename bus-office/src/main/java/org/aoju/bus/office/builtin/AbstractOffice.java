package org.aoju.bus.office.builtin;

import org.aoju.bus.office.provider.SourceDocumentProvider;

/**
 * 所有office任务实现的基类.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractOffice implements MadeInOffice {

    protected final SourceDocumentProvider source;

    /**
     * 使用指定的源文档创建新任务.
     *
     * @param source 文档的源规范.
     */
    public AbstractOffice(final SourceDocumentProvider source) {
        super();
        this.source = source;
    }

}

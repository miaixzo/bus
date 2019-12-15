package org.aoju.bus.office.provider;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.office.magic.family.DocumentFormat;

import java.io.File;

/**
 * 所有文档规范实现的基类.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractDocumentProvider implements DocumentProvider {

    private final File file;
    private DocumentFormat documentFormat;

    protected AbstractDocumentProvider(final File file) {
        super();

        Assert.notNull(file, "The file is null");

        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public DocumentFormat getFormat() {
        return documentFormat;
    }

    /**
     * @param documentFormat 要设置的文档格式.
     */
    public void setDocumentFormat(final DocumentFormat documentFormat) {
        Assert.notNull(documentFormat, "The document format is null or unsupported");
        this.documentFormat = documentFormat;
    }

}

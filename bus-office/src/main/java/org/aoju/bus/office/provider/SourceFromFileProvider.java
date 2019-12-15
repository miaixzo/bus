package org.aoju.bus.office.provider;

import java.io.File;

/**
 * 当转换过程不再需要源文件时，提供应用行为的接口.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class SourceFromFileProvider extends AbstractSourceProvider
        implements SourceDocumentProvider {

    SourceFromFileProvider(final File file) {
        super(file);
    }

    @Override
    public void onConsumed(final File file) {

    }

}

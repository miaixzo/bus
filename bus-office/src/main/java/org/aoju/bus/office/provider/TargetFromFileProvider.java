package org.aoju.bus.office.provider;


import org.aoju.bus.core.utils.FileUtils;

import java.io.File;

/**
 * 当转换过程不再需要目标文件时，提供应用行为的接口.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class TargetFromFileProvider extends AbstractTargetProvider
        implements TargetDocumentProvider {

    public TargetFromFileProvider(final File file) {
        super(file);
    }

    @Override
    public void onComplete(final File file) {

    }

    @Override
    public void onFailure(final File file, final Exception exception) {
        FileUtils.delete(file);
    }

}

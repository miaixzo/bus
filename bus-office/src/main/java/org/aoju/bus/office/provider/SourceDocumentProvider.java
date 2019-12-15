package org.aoju.bus.office.provider;

import java.io.File;

/**
 * 当转换过程不再需要源文件时，提供应用行为的接口.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface SourceDocumentProvider extends DocumentProvider {

    /**
     * 当文件被使用并且转换器不再需要时调用。在调用此方法之前，不能从文件系统中删除该文件.
     *
     * @param file 所使用的文件.
     */
    void onConsumed(File file);

}

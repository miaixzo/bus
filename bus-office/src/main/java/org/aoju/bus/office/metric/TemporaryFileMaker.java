package org.aoju.bus.office.metric;

import java.io.File;

/**
 * 提供创建临时文件的服务.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface TemporaryFileMaker {

    /**
     * 创建没有扩展名的新临时文件.
     *
     * @return 创建的文件.
     */
    File makeTemporaryFile();

    /**
     * 创建有指定扩展名的新临时文件.
     *
     * @param extension 要创建的文件的扩展名.
     * @return 创建的文件.
     */
    File makeTemporaryFile(String extension);

}

package org.aoju.bus.office.provider;

import org.aoju.bus.office.magic.family.DocumentFormat;

import java.io.File;

/**
 * 为文档提供转换过程所需的物理文件和格式的接口.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface DocumentProvider {

    /**
     * 获取文档所在的文件.
     *
     * @return 一个文件实例.
     */
    File getFile();

    /**
     * @return 文档格式.
     */
    DocumentFormat getFormat();

}

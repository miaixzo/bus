package org.aoju.bus.office.provider;

import java.io.File;

/**
 * 当转换过程不再需要目标文件时，提供应用行为的接口.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface TargetDocumentProvider extends DocumentProvider {

    /**
     * 如果转换成功完成，则调用.
     *
     * @param file 写入转换结果的文件.
     */
    void onComplete(File file);

    /**
     * 如果转换以异常结束则调用.
     *
     * @param file      要将转换结果写入其中的文件.
     * @param exception 表示转换失败原因的异常.
     */
    void onFailure(File file, Exception exception);

}

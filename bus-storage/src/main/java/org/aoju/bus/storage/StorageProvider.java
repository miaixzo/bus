package org.aoju.bus.storage;

import java.io.Closeable;
import java.util.Map;

/**
 * 上传接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface StorageProvider extends Closeable {

    String name();

    /**
     * 文件上传
     *
     * @param object
     * @return
     */
    String upload(UploadObject object);

    /**
     * 获取文件下载地址
     *
     * @param fileKey 文件（全路径或者fileKey）
     * @return
     */
    String getUrl(String fileKey);

    /**
     * 删除图片
     *
     * @return
     */
    boolean delete(String fileKey);

    String downloadAndSaveAs(String fileKey, String localSaveDir);

    Map<String, Object> createUploadToken(UploadToken param);
}

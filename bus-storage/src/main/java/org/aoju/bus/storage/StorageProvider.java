package org.aoju.bus.storage;

import java.io.Closeable;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.0.9
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
     * @param
     * @return
     */
    String getUrl(String fileKey, boolean isInternal);

    /**
     * 删除图片
     *
     * @return
     */
    boolean delete(String fileKey);

    String downloadAndSaveAs(String fileKey, String localSaveDir, boolean isInternal);

    Map<String, Object> createUploadToken(UploadToken param);
}

package org.aoju.bus.storage;

import lombok.Data;
import org.aoju.bus.core.consts.Httpd;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.storage.magic.MimeType;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
@Data
public class UploadObject {

    private String fileName;
    private String mimeType;
    private String catalog;
    private String url;
    private byte[] bytes;
    private File file;
    private InputStream inputStream;

    public UploadObject(String filePath) {
        if (filePath.startsWith(Httpd.HTTP_PREFIX)
                || filePath.startsWith(Httpd.HTTPS_PREFIX)) {
            this.url = filePath;
            this.fileName = parseFileName(this.url);
        } else {
            this.file = new File(filePath);
            this.fileName = file.getName();
        }
    }

    public static String parseFileName(String filePath) {
        filePath = filePath.split("\\?")[0];
        int index = filePath.lastIndexOf("/") + 1;
        if (index > 0) {
            return filePath.substring(index);
        }
        return filePath;
    }

    public UploadObject(File file) {
        this.fileName = file.getName();
        this.file = file;
    }

    public UploadObject(String fileName, File file) {
        this.fileName = fileName;
        this.file = file;
    }

    public UploadObject(String fileName, InputStream inputStream, String mimeType) {
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.mimeType = mimeType;
    }

    public UploadObject(String fileName, byte[] bytes, String mimeType) {
        this.fileName = fileName;
        this.bytes = bytes;
        this.mimeType = mimeType;
    }

    public String getFileName() {
        if (StringUtils.isBlank(fileName)) {
            fileName = UUID.randomUUID().toString().replaceAll("\\-", "");
        }
        if (mimeType != null && !fileName.contains(".")) {
            String fileExtension = MimeType.getFileExtension(mimeType);
            if (fileExtension != null) fileName = fileName + fileExtension;
        }
        return fileName;
    }

}

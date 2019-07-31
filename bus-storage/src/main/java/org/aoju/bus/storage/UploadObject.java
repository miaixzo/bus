package org.aoju.bus.storage;

import org.aoju.bus.core.consts.Httpd;
import org.aoju.bus.core.key.ObjectID;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.storage.magic.Magic;
import org.aoju.bus.storage.magic.MagicMatch;
import org.aoju.bus.storage.magic.MimeTypeFile;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class UploadObject {

    private String fileName;
    private String mimeType;
    private String catalog;
    private String url;
    private byte[] bytes;
    private File file;
    private InputStream inputStream;
    private Map<String, Object> metadata = new HashMap<String, Object>();

    public UploadObject(String filePath) {
        if (filePath.startsWith(Httpd.HTTP_PREFIX) || filePath.startsWith(Httpd.HTTPS_PREFIX)) {
            this.url = filePath;
            this.fileName = parseFileName(this.url);
        } else {
            this.file = new File(filePath);
            this.fileName = file.getName();
        }
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

    public UploadObject(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
        this.mimeType = perseMimeType(bytes);
    }

    private static String perseMimeType(byte[] bytes) {
        try {
            MagicMatch match = Magic.getMagicMatch(bytes);
            String mimeType = match.getMimeType();
            return mimeType;
        } catch (Exception e) {
            return null;
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

    public String getFileName() {
        if (StringUtils.isBlank(fileName)) {
            fileName = ObjectID.id();
        }
        if (mimeType != null && !fileName.contains(".")) {
            String fileExtension = MimeTypeFile.getFileExtension(mimeType);
            if (fileExtension != null) {
                fileName = fileName + fileExtension;
            }
        }
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public File getFile() {
        return file;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void setString(String mimeType) {
        this.mimeType = mimeType;
    }

    public UploadObject addMetaData(String key, Object value) {
        metadata.put(key, value);
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getCatalog() {
        return catalog;
    }

    public UploadObject toCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

}

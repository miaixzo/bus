package org.aoju.bus.storage.provider;

import lombok.Data;
import org.aoju.bus.core.consts.Httpd;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.storage.StorageProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
@Data
public abstract class AbstractProvider implements StorageProvider {

    protected static final String DIR_SPLITER = "/";

    protected String accessKey;
    protected String secretKey;
    protected String bucket;
    protected String prefix;
    protected boolean privated;

    public static String downloadFile(String fileURL, String saveDir) {
        HttpURLConnection httpConn = null;
        FileOutputStream outputStream = null;
        try {
            URL url = new URL(fileURL);
            httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");

                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
                }
                InputStream inputStream = httpConn.getInputStream();
                String saveFilePath = saveDir + File.separator + fileName;

                outputStream = new FileOutputStream(saveFilePath);

                int bytesRead = -1;
                byte[] buffer = new byte[2048];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                return saveFilePath;
            } else {
                throw new CommonException("下载失败");
            }
        } catch (IOException e) {
            throw new CommonException("下载失败", e);
        } finally {
            try {
                if (outputStream != null) outputStream.close();
            } catch (Exception e2) {
            }
            try {
                if (httpConn != null) httpConn.disconnect();
            } catch (Exception e2) {
            }
        }
    }

    protected String getFullPath(String file) {
        if (file.startsWith(Httpd.HTTP_PREFIX) || file.startsWith(Httpd.HTTPS_PREFIX)) {
            return file;
        }
        return prefix + file;
    }

    @Override
    public String downloadAndSaveAs(String file, String localSaveDir, boolean isInternal) {
        return downloadFile(getUrl(file, isInternal), localSaveDir);
    }

}

/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.storage.provider;

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
 * @version 3.0.0
 * @since JDK 1.8
 */
public abstract class AbstractProvider implements StorageProvider {

    protected String prefix;
    protected String bucketName;

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
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                            fileURL.length());
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
    public String downloadAndSaveAs(String file, String localSaveDir) {
        return downloadFile(getUrl(file), localSaveDir);
    }

}

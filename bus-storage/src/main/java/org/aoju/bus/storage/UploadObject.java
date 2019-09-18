/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
 * @version 3.5.0
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
            fileName = UUID.randomUUID().toString().replaceAll("\\-", "");
        }
        if (mimeType != null && !fileName.contains(".")) {
            String fileExtension = MimeType.getFileExtension(mimeType);
            if (fileExtension != null) fileName = fileName + fileExtension;
        }
        return fileName;
    }

}

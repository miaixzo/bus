/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.storage;

/**
 * Enumerates the supported storage platform types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry {

    /**
     * Alibaba Cloud Object Storage Service (OSS).
     */
    ALIYUN,
    /**
     * Amazon Simple Storage Service (S3).
     */
    AMAZON,
    /**
     * Microsoft Azure Blob Storage Service (S3).
     */
    AZURE,
    /**
     * Backblaze B2 Cloud Storage (S3 Compatible).
     */
    BACKBLAZE_B2,
    /**
     * Baidu Object Storage (BOS).
     */
    BAIDU,
    /**
     * Cloudflare R2 Object Storage (S3 Compatible).
     */
    CLOUDFLARE_R2,
    /**
     * File Transfer Protocol (FTP).
     */
    FTP,
    /**
     * generic S3-compatible.
     */
    S3,
    /**
     * Gitlab file storage.
     */
    GITLAB,
    /**
     * Google Cloud Storage (CS).
     */
    GOOGLE,
    /**
     * Huawei Cloud Object Storage Service (OBS).
     */
    HUAWEI,
    /**
     * JD Cloud Object Storage Service (OBS).
     */
    JD,
    /**
     * Local file system storage.
     */
    LOCAL,
    /**
     * MinIO Object Storage Service.
     */
    MINIO,
    /**
     * MinIO Object Storage Service.
     */
    ONEDRIVE,
    /**
     * Qiniu Cloud Object Storage Service (OSS).
     */
    QINIU,
    /**
     * Secure File Transfer Protocol (SFTP).
     */
    SFTP,
    /**
     * Server Message Block (SMB) protocol for file sharing.
     */
    SMB,
    /**
     * Tencent Cloud Object Storage (COS).
     */
    TENCENT,
    /**
     * Upyun Object Storage Service (OSS).
     */
    UPYUN,
    /**
     * Volcengine TOS Storage (S3 Compatible).
     */
    VOLCENGINE,
    /**
     * Wasabi Cloud Storage (S3 Compatible).
     */
    WASABI,
    /**
     * Web Distributed Authoring and Versioning (WebDAV).
     */
    WEBDAV

}

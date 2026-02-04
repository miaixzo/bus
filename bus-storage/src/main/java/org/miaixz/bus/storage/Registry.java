/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
     * Alibaba Cloud International Object Storage Service (OSS).
     */
    ALIYUN_INTL,
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
     * Box Enterprise content management platform.
     */
    BOX,
    /**
     * Cloudflare R2 Object Storage (S3 Compatible).
     */
    CLOUDFLARE_R2,
    /**
     * Contabo Object Storage (S3 Compatible).
     */
    CONTABO,
    /**
     * DigitalOcean Spaces (S3 Compatible).
     */
    DIGITALOCEAN,
    /**
     * Dropbox Business file sync and collaboration.
     */
    DROPBOX,
    /**
     * Exoscale Object Storage (S3 Compatible).
     */
    EXOSCALE,
    /**
     * Filebase Decentralized Storage (S3 Compatible).
     */
    FILEBASE,
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
     * Google Drive and Google Workspace.
     */
    GOOGLE_DRIVE,
    /**
     * Huawei Cloud Object Storage Service (OBS).
     */
    HUAWEI,
    /**
     * IBM Cloud Object Storage (S3 Compatible).
     */
    IBM,
    /**
     * Apple iCloud Drive cloud storage.
     */
    ICLOUD,
    /**
     * JD Cloud Object Storage Service (OBS).
     */
    JD,
    /**
     * Kakao Cloud Object Storage (S3 Compatible).
     */
    KAKAO,
    /**
     * Linode Object Storage (S3 Compatible).
     */
    LINODE,
    /**
     * Local file system storage.
     */
    LOCAL,
    /**
     * Mega encrypted cloud storage.
     */
    MEGA,
    /**
     * MinIO Object Storage Service.
     */
    MINIO,
    /**
     * Naver Cloud Object Storage (S3 Compatible).
     */
    NAVER,
    /**
     * NHN Cloud Object Storage (S3 Compatible).
     */
    NHN,
    /**
     * Oracle Cloud Object Storage (S3 Compatible).
     */
    ORACLE,
    /**
     * OVHcloud Object Storage (S3 Compatible).
     */
    OVHCLOUD,
    /**
     * Qiniu Cloud Object Storage Service (OSS).
     */
    QINIU,
    /**
     * Sakura Cloud Object Storage (S3 Compatible).
     */
    SAKURA,
    /**
     * Scaleway Object Storage (S3 Compatible).
     */
    SCALEWAY,
    /**
     * Secure File Transfer Protocol (SFTP).
     */
    SFTP,
    /**
     * Microsoft SharePoint Online and OneDrive for Business.
     */
    SHAREPOINT,
    /**
     * Server Message Block (SMB) protocol for file sharing.
     */
    SMB,
    /**
     * Storj Decentralized Cloud Storage (S3 Compatible).
     */
    STORJ,
    /**
     * Tencent Cloud Object Storage (COS).
     */
    TENCENT,
    /**
     * Tencent Cloud International Object Storage (COS).
     */
    TENCENT_INTL,
    /**
     * Upyun Object Storage Service (OSS).
     */
    UPYUN,
    /**
     * Volcengine TOS Storage (S3 Compatible).
     */
    VOLCENGINE,
    /**
     * Vultr Object Storage (S3 Compatible).
     */
    VULTR,
    /**
     * Wasabi Cloud Storage (S3 Compatible).
     */
    WASABI,
    /**
     * Web Distributed Authoring and Versioning (WebDAV).
     */
    WEBDAV

}

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
package org.miaixz.bus.storage.metric;

import org.miaixz.bus.storage.Context;

/**
 * Storage service provider for Oracle Cloud Object Storage. This provider integrates with Oracle Cloud Infrastructure
 * (OCI) Object Storage using S3-compatible API.
 * <p>
 * Oracle Cloud Object Storage is a highly scalable, durable, and secure object storage service that is part of Oracle
 * Cloud Infrastructure. It provides S3-compatible API for seamless integration.
 * <p>
 * <strong>Configuration:</strong>
 * <ul>
 * <li>endpoint: Oracle Cloud Object Storage endpoint (e.g.,
 * https://{namespace}.compat.objectstorage.{region}.oraclecloud.com)</li>
 * <li>bucket: Bucket name in Oracle Cloud Object Storage</li>
 * <li>accessKey: Oracle Cloud access key (Customer Secret Key)</li>
 * <li>secretKey: Oracle Cloud secret key (Customer Secret Key)</li>
 * <li>region: Oracle Cloud region (e.g., us-phoenix-1, us-ashburn-1, ap-tokyo-1)</li>
 * </ul>
 * <p>
 * <strong>Endpoint Format:</strong>
 * 
 * <pre>
 * https://{namespace}.compat.objectstorage.{region}.oraclecloud.com
 *
 * Examples:
 * - US Phoenix: https://mycompany.compat.objectstorage.us-phoenix-1.oraclecloud.com
 * - US Ashburn: https://mycompany.compat.objectstorage.us-ashburn-1.oraclecloud.com
 * - AP Tokyo: https://mycompany.compat.objectstorage.ap-tokyo-1.oraclecloud.com
 * - EU Frankfurt: https://mycompany.compat.objectstorage.eu-frankfurt-1.oraclecloud.com
 * </pre>
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * Context context = Context.builder().endpoint("https://mycompany.compat.objectstorage.us-phoenix-1.oraclecloud.com")
 *         .bucket("my-bucket").accessKey("your-access-key").secretKey("your-secret-key").region("us-phoenix-1")
 *         .build();
 *
 * OracleOssProvider provider = new OracleOssProvider(context);
 * provider.upload("file.txt", fileBytes);
 * }</pre>
 * <p>
 * <strong>Supported Regions:</strong>
 * <ul>
 * <li>Americas: us-phoenix-1, us-ashburn-1, ca-toronto-1, ca-montreal-1, sa-saopaulo-1</li>
 * <li>EMEA: eu-frankfurt-1, eu-zurich-1, uk-london-1, eu-amsterdam-1</li>
 * <li>Asia Pacific: ap-tokyo-1, ap-osaka-1, ap-seoul-1, ap-mumbai-1, ap-sydney-1, ap-melbourne-1</li>
 * <li>Middle East: me-jeddah-1, me-dubai-1</li>
 * </ul>
 * <p>
 * <strong>Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy migration</li>
 * <li>High durability (99.999999999% - 11 nines)</li>
 * <li>Strong consistency for read-after-write</li>
 * <li>Automatic encryption at rest</li>
 * <li>Integration with Oracle Cloud services</li>
 * <li>Support for multipart uploads</li>
 * <li>Lifecycle management policies</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OracleOssProvider extends GenericS3Provider {

    /**
     * Constructs an Oracle Cloud Object Storage provider with the given context. Initializes the S3-compatible client
     * using the provided credentials and endpoint configuration.
     * <p>
     * The endpoint should follow the format: {@code https://{namespace}.compat.objectstorage.{region}.oraclecloud.com}
     * <p>
     * Where:
     * <ul>
     * <li>{namespace} is your Oracle Cloud tenancy namespace</li>
     * <li>{region} is the Oracle Cloud region (e.g., us-phoenix-1, ap-tokyo-1)</li>
     * </ul>
     *
     * @param context The storage context, containing endpoint, bucket, access key, secret key, region, and other
     *                configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public OracleOssProvider(Context context) {
        super(context);
    }

}

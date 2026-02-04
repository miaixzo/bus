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
 * Storage service provider for Linode Object Storage. This provider integrates with Linode Object Storage (now part of
 * Akamai) using S3-compatible API.
 * <p>
 * Linode Object Storage is a globally-available, S3-compatible storage service with Akamai CDN integration. It provides
 * high-performance object storage with predictable pricing.
 * <p>
 * <strong>Configuration:</strong>
 * <ul>
 * <li>endpoint: Linode Object Storage endpoint (e.g., https://{region}.linodeobjects.com)</li>
 * <li>bucket: Bucket name in Linode Object Storage</li>
 * <li>accessKey: Linode Object Storage access key</li>
 * <li>secretKey: Linode Object Storage secret key</li>
 * <li>region: Linode region (e.g., us-east-1, eu-central-1, ap-south-1)</li>
 * </ul>
 * <p>
 * <strong>Endpoint Format:</strong>
 * 
 * <pre>
 * Standard Endpoint:
 * https://{cluster-id}.linodeobjects.com
 *
 * Examples:
 * - US East (Newark): https://us-east-1.linodeobjects.com
 * - US Southeast (Atlanta): https://us-southeast-1.linodeobjects.com
 * - EU Central (Frankfurt): https://eu-central-1.linodeobjects.com
 * - AP South (Singapore): https://ap-south-1.linodeobjects.com
 * </pre>
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * Context context = Context.builder().endpoint("https://us-east-1.linodeobjects.com").bucket("my-bucket")
 *         .accessKey("your-access-key").secretKey("your-secret-key").region("us-east-1").build();
 *
 * LinodeOssProvider provider = new LinodeOssProvider(context);
 * provider.upload("file.txt", fileBytes);
 * }</pre>
 * <p>
 * <strong>Supported Regions:</strong>
 * <ul>
 * <li>Americas:
 * <ul>
 * <li>us-east-1 - Newark, NJ</li>
 * <li>us-southeast-1 - Atlanta, GA</li>
 * </ul>
 * </li>
 * <li>Europe:
 * <ul>
 * <li>eu-central-1 - Frankfurt, Germany</li>
 * </ul>
 * </li>
 * <li>Asia Pacific:
 * <ul>
 * <li>ap-south-1 - Singapore</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * <strong>Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy migration</li>
 * <li>Akamai CDN integration for global delivery</li>
 * <li>Simple, predictable pricing ($5/month for 250GB)</li>
 * <li>Free outbound transfer within Linode network</li>
 * <li>Automatic SSL/TLS encryption</li>
 * <li>CORS support for web applications</li>
 * <li>Lifecycle policies for cost optimization</li>
 * <li>Access control with bucket policies and ACLs</li>
 * <li>Versioning support for data protection</li>
 * </ul>
 * <p>
 * <strong>Akamai Integration:</strong>
 * 
 * <pre>
 * Since Linode is now part of Akamai, you get:
 * - Access to Akamai's global CDN network
 * - Enhanced performance and reliability
 * - DDoS protection
 * - Edge computing capabilities
 * </pre>
 * <p>
 * <strong>Pricing (as of 2024):</strong>
 * <ul>
 * <li>Storage: $5/month for 250GB (additional $0.02/GB)</li>
 * <li>Outbound Transfer: $0.005/GB (free within Linode)</li>
 * <li>Requests: Free (no per-request charges)</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Static website hosting</li>
 * <li>Media storage and streaming</li>
 * <li>Backup and archival</li>
 * <li>Application data storage</li>
 * <li>Content distribution with Akamai CDN</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LinodeOssProvider extends GenericS3Provider {

    /**
     * Constructs a Linode Object Storage provider with the given context. Initializes the S3-compatible client using
     * the provided credentials and endpoint configuration.
     * <p>
     * The endpoint should follow the format: {@code https://{cluster-id}.linodeobjects.com}
     * <p>
     * Where {cluster-id} is the Linode region identifier (e.g., us-east-1, eu-central-1, ap-south-1).
     * <p>
     * <strong>Getting Started:</strong>
     * <ol>
     * <li>Create a bucket in Linode Cloud Manager</li>
     * <li>Generate Object Storage access keys</li>
     * <li>Use the access key and secret key in your configuration</li>
     * <li>Optionally configure CORS for web access</li>
     * </ol>
     * <p>
     * <strong>Note:</strong> Linode Object Storage is now part of Akamai Connected Cloud, providing enhanced global
     * performance and reliability.
     *
     * @param context The storage context, containing endpoint, bucket name, access key, secret key, region, and other
     *                configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public LinodeOssProvider(Context context) {
        super(context);
    }

}

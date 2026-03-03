/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.storage.metric;

import org.miaixz.bus.storage.Context;

/**
 * Storage service provider for IBM Cloud Object Storage. This provider integrates with IBM Cloud Object Storage using
 * S3-compatible API.
 * <p>
 * IBM Cloud Object Storage is a highly scalable, secure, and cost-effective object storage service designed for
 * enterprise workloads. It provides S3-compatible API for seamless integration.
 * <p>
 * <strong>Configuration:</strong>
 * <ul>
 * <li>endpoint: IBM Cloud Object Storage endpoint (e.g., https://s3.{region}.cloud-object-storage.appdomain.cloud)</li>
 * <li>bucket: Bucket name in IBM Cloud Object Storage</li>
 * <li>accessKey: IBM Cloud HMAC access key (Service Credential)</li>
 * <li>secretKey: IBM Cloud HMAC secret key (Service Credential)</li>
 * <li>region: IBM Cloud region (e.g., us-south, us-east, eu-gb, eu-de, jp-tok)</li>
 * </ul>
 * <p>
 * <strong>Endpoint Format:</strong>
 * 
 * <pre>
 * Public Endpoints:
 * https://s3.{region}.cloud-object-storage.appdomain.cloud
 *
 * Private Endpoints (within IBM Cloud):
 * https://s3.private.{region}.cloud-object-storage.appdomain.cloud
 *
 * Direct Endpoints (single data center):
 * https://s3.direct.{region}.cloud-object-storage.appdomain.cloud
 *
 * Examples:
 * - US South: https://s3.us-south.cloud-object-storage.appdomain.cloud
 * - US East: https://s3.us-east.cloud-object-storage.appdomain.cloud
 * - EU GB: https://s3.eu-gb.cloud-object-storage.appdomain.cloud
 * - EU DE: https://s3.eu-de.cloud-object-storage.appdomain.cloud
 * - JP Tokyo: https://s3.jp-tok.cloud-object-storage.appdomain.cloud
 * </pre>
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * Context context = Context.builder().endpoint("https://s3.us-south.cloud-object-storage.appdomain.cloud")
 *         .bucket("my-bucket").accessKey("your-hmac-access-key").secretKey("your-hmac-secret-key").region("us-south")
 *         .build();
 *
 * IBMCosProvider provider = new IBMCosProvider(context);
 * provider.upload("file.txt", fileBytes);
 * }</pre>
 * <p>
 * <strong>Supported Regions:</strong>
 * <ul>
 * <li>Cross Region: us, eu, ap (spans multiple data centers)</li>
 * <li>Regional:
 * <ul>
 * <li>Americas: us-south, us-east, ca-tor, br-sao</li>
 * <li>Europe: eu-gb, eu-de, eu-es</li>
 * <li>Asia Pacific: jp-tok, jp-osa, au-syd</li>
 * </ul>
 * </li>
 * <li>Single Data Center: Various locations worldwide</li>
 * </ul>
 * <p>
 * <strong>Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy migration</li>
 * <li>High durability (99.999999999% - 11 nines)</li>
 * <li>Built-in encryption at rest and in transit</li>
 * <li>Immutable Object Storage for compliance</li>
 * <li>Integrated with IBM Cloud services (Watson, Analytics, etc.)</li>
 * <li>Support for Aspera high-speed transfer</li>
 * <li>Archive tier for long-term storage</li>
 * <li>Key Protect integration for key management</li>
 * </ul>
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: Frequently accessed data</li>
 * <li>Vault: Infrequently accessed data (30-day minimum)</li>
 * <li>Cold Vault: Rarely accessed data (90-day minimum)</li>
 * <li>Flex: Automatic tier optimization</li>
 * </ul>
 * <p>
 * <strong>Security &amp; Compliance:</strong>
 * <ul>
 * <li>HIPAA, PCI-DSS, SOC 2, ISO 27001 compliant</li>
 * <li>GDPR ready with EU data residency</li>
 * <li>Activity Tracker for audit logging</li>
 * <li>IAM integration for access control</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IBMCosProvider extends GenericS3Provider {

    /**
     * Constructs an IBM Cloud Object Storage provider with the given context. Initializes the S3-compatible client
     * using the provided HMAC credentials and endpoint configuration.
     * <p>
     * The endpoint should follow the format: {@code https://s3.{region}.cloud-object-storage.appdomain.cloud}
     * <p>
     * Where {region} is the IBM Cloud region (e.g., us-south, eu-gb, jp-tok).
     * <p>
     * <strong>Credential Requirements:</strong>
     * <ul>
     * <li>Access Key: HMAC access key from IBM Cloud Service Credentials</li>
     * <li>Secret Key: HMAC secret key from IBM Cloud Service Credentials</li>
     * </ul>
     * <p>
     * To obtain HMAC credentials:
     * <ol>
     * <li>Create a Service Credential in IBM Cloud Console</li>
     * <li>Enable "Include HMAC Credential" option</li>
     * <li>Use the generated access_key_id and secret_access_key</li>
     * </ol>
     *
     * @param context The storage context, containing endpoint, bucket, access key (HMAC), secret key (HMAC), region,
     *                and other configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public IBMCosProvider(Context context) {
        super(context);
    }

}

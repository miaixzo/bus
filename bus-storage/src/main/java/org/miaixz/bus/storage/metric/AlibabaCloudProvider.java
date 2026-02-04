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
 * Storage service provider for Alibaba Cloud International Object Storage Service (OSS). This provider is specifically
 * designed for Alibaba Cloud's international regions outside of mainland China, offering global cloud storage solutions
 * with S3-compatible API.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>Global presence with 20+ international regions</li>
 * <li>High availability with 99.995% SLA</li>
 * <li>Multi-region replication and CDN integration</li>
 * <li>Advanced security features (encryption, access control)</li>
 * <li>Competitive pricing for international markets</li>
 * </ul>
 * <p>
 * <strong>Available International Regions:</strong>
 * <ul>
 * <li><strong>Asia Pacific:</strong></li>
 * <li>ap-southeast-1 (Singapore)</li>
 * <li>ap-southeast-2 (Sydney, Australia)</li>
 * <li>ap-southeast-3 (Kuala Lumpur, Malaysia)</li>
 * <li>ap-southeast-5 (Jakarta, Indonesia)</li>
 * <li>ap-southeast-6 (Manila, Philippines)</li>
 * <li>ap-southeast-7 (Bangkok, Thailand)</li>
 * <li>ap-northeast-1 (Tokyo, Japan)</li>
 * <li>ap-northeast-2 (Seoul, South Korea)</li>
 * <li>ap-south-1 (Mumbai, India)</li>
 * <li><strong>Europe:</strong></li>
 * <li>eu-central-1 (Frankfurt, Germany)</li>
 * <li>eu-west-1 (London, UK)</li>
 * <li><strong>Middle East:</strong></li>
 * <li>me-east-1 (Dubai, UAE)</li>
 * <li><strong>North America:</strong></li>
 * <li>us-west-1 (Silicon Valley, USA)</li>
 * <li>us-east-1 (Virginia, USA)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://oss-ap-southeast-1.aliyuncs.com") // Singapore
 *         .bucket("my-bucket").accessKey("ALIBABA_INTL_ACCESS_KEY").secretKey("ALIBABA_INTL_SECRET_KEY")
 *         .region("ap-southeast-1").build();
 *
 * AlibabaCloudProvider provider = new AlibabaCloudProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://oss-{region}.aliyuncs.com
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: Frequently accessed data with high performance</li>
 * <li>IA (Infrequent Access): Less frequently accessed data (30-day minimum)</li>
 * <li>Archive: Long-term archival storage (60-day minimum)</li>
 * <li>Cold Archive: Ultra-low-cost archival storage (180-day minimum)</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>International applications and services</li>
 * <li>Southeast Asian market expansion</li>
 * <li>Global content distribution and CDN</li>
 * <li>Cross-border e-commerce platforms</li>
 * <li>International backup and disaster recovery</li>
 * <li>Multi-region data replication</li>
 * </ul>
 * <p>
 * <strong>Differences from Domestic Version:</strong>
 * <ul>
 * <li>International regions only (no mainland China)</li>
 * <li>Different pricing structure</li>
 * <li>International compliance (GDPR, SOC 2, ISO 27001)</li>
 * <li>English-first documentation and support</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlibabaCloudProvider extends GenericS3Provider {

    /**
     * Constructs an Alibaba Cloud International OSS provider with the given context.
     *
     * @param context The storage context, containing Alibaba Cloud International-specific endpoint, bucket, access key,
     *                secret key, and region configurations.
     */
    public AlibabaCloudProvider(Context context) {
        super(context);
    }

}

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
 * Storage service provider for Tencent Cloud International Object Storage (COS). This provider is specifically designed
 * for Tencent Cloud's international regions outside of mainland China, offering global cloud storage solutions with
 * S3-compatible API.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>Global presence with 20+ international regions</li>
 * <li>High availability with 99.95% SLA</li>
 * <li>Integrated with Tencent Cloud CDN for global acceleration</li>
 * <li>Advanced security features (encryption, access control)</li>
 * <li>Competitive pricing for international markets</li>
 * </ul>
 * <p>
 * <strong>Available International Regions:</strong>
 * <ul>
 * <li><strong>Asia Pacific:</strong></li>
 * <li>ap-singapore (Singapore)</li>
 * <li>ap-bangkok (Bangkok, Thailand)</li>
 * <li>ap-mumbai (Mumbai, India)</li>
 * <li>ap-jakarta (Jakarta, Indonesia)</li>
 * <li>ap-seoul (Seoul, South Korea)</li>
 * <li>ap-tokyo (Tokyo, Japan)</li>
 * <li><strong>North America:</strong></li>
 * <li>na-siliconvalley (Silicon Valley, USA)</li>
 * <li>na-ashburn (Virginia, USA)</li>
 * <li>na-toronto (Toronto, Canada)</li>
 * <li><strong>Europe:</strong></li>
 * <li>eu-frankfurt (Frankfurt, Germany)</li>
 * <li>eu-moscow (Moscow, Russia)</li>
 * <li><strong>South America:</strong></li>
 * <li>sa-saopaulo (São Paulo, Brazil)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://cos.ap-singapore.myqcloud.com") // Singapore
 *         .bucket("my-bucket-1234567890") // Bucket name with APPID suffix
 *         .accessKey("TENCENT_INTL_SECRET_ID").secretKey("TENCENT_INTL_SECRET_KEY").region("ap-singapore").build();
 *
 * TencentCloudProvider provider = new TencentCloudProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://cos.{region}.myqcloud.com
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>STANDARD: Frequently accessed data with high performance</li>
 * <li>STANDARD_IA: Infrequently accessed data (30-day minimum)</li>
 * <li>INTELLIGENT_TIERING: Automatic tier optimization based on access patterns</li>
 * <li>ARCHIVE: Long-term archival storage (90-day minimum)</li>
 * <li>DEEP_ARCHIVE: Ultra-low-cost archival storage (180-day minimum)</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>International applications and services</li>
 * <li>Southeast Asian market expansion</li>
 * <li>Global content distribution and CDN</li>
 * <li>Cross-border gaming platforms</li>
 * <li>International backup and disaster recovery</li>
 * <li>Multi-region data replication</li>
 * <li>WeChat mini-programs with global reach</li>
 * </ul>
 * <p>
 * <strong>Differences from Domestic Version:</strong>
 * <ul>
 * <li>International regions only (no mainland China)</li>
 * <li>Different pricing structure</li>
 * <li>International compliance (GDPR, SOC 2, ISO 27001)</li>
 * <li>English-first documentation and support</li>
 * <li>Separate account system from domestic version</li>
 * </ul>
 * <p>
 * <strong>Integration:</strong> Seamlessly integrates with Tencent Cloud International services including CDN, Cloud
 * Infinite (CI) for media processing, and Cloud Log Service (CLS).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TencentCloudProvider extends GenericS3Provider {

    /**
     * Constructs a Tencent Cloud International COS provider with the given context.
     *
     * @param context The storage context, containing Tencent Cloud International-specific endpoint, bucket, secret ID,
     *                secret key, and region configurations.
     */
    public TencentCloudProvider(Context context) {
        super(context);
    }

}

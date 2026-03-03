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
 * Storage service provider for Sakura Cloud Object Storage (S3-compatible). Sakura Internet is one of Japan's leading
 * cloud service providers, offering reliable and cost-effective object storage solutions optimized for the Japanese
 * market.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>Optimized network performance for Japan</li>
 * <li>Data residency in Japanese data centers</li>
 * <li>Competitive pricing for Japanese market</li>
 * <li>High availability with 99.9% SLA</li>
 * <li>Integration with Sakura Cloud ecosystem</li>
 * </ul>
 * <p>
 * <strong>Available Regions:</strong>
 * <ul>
 * <li>is1a (Ishikari Zone 1A) - Primary region</li>
 * <li>is1b (Ishikari Zone 1B) - Secondary region</li>
 * <li>tk1a (Tokyo Zone 1A)</li>
 * <li>tk1b (Tokyo Zone 1B)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://s3.isk01.sakurastorage.jp") // or tk1
 *         .bucket("my-bucket").accessKey("SAKURA_ACCESS_KEY").secretKey("SAKURA_SECRET_KEY").region("is1a").build();
 *
 * SakuraCloudProvider provider = new SakuraCloudProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://s3.{region}.sakurastorage.jp
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: Frequently accessed data with high performance</li>
 * <li>Standard-IA: Infrequently accessed data with lower storage costs</li>
 * </ul>
 * <p>
 * <strong>Pricing Highlights:</strong>
 * <ul>
 * <li>Competitive pricing for Japanese market</li>
 * <li>No data transfer fees within Sakura Cloud</li>
 * <li>Simple and transparent pricing structure</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Japanese market applications and services</li>
 * <li>Web application backend storage</li>
 * <li>Static website hosting</li>
 * <li>Media and content delivery for Japan</li>
 * <li>Backup and disaster recovery</li>
 * <li>Log aggregation and analytics</li>
 * <li>E-commerce platform storage</li>
 * </ul>
 * <p>
 * <strong>Integration:</strong> Seamlessly integrates with other Sakura Cloud services including CDN, Load Balancer,
 * and Database services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SakuraCloudProvider extends GenericS3Provider {

    /**
     * Constructs a Sakura Cloud Object Storage provider with the given context.
     *
     * @param context The storage context, containing Sakura Cloud-specific endpoint, bucket, access key, secret key,
     *                and region configurations.
     */
    public SakuraCloudProvider(Context context) {
        super(context);
    }

}

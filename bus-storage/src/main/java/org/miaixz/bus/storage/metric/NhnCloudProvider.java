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
 * Storage service provider for NHN Cloud Object Storage (S3-compatible). NHN Cloud is a major South Korean cloud
 * service provider, offering enterprise-grade object storage solutions with strong presence in gaming and entertainment
 * industries.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy integration</li>
 * <li>Optimized network performance for South Korea and Asia</li>
 * <li>High availability with 99.9% SLA</li>
 * <li>Strong presence in gaming and entertainment sectors</li>
 * <li>Data sovereignty with Korean data centers</li>
 * <li>Integration with NHN Cloud gaming services</li>
 * </ul>
 * <p>
 * <strong>Available Regions:</strong>
 * <ul>
 * <li>KR1 (Korea Pangyo) - Primary region</li>
 * <li>KR2 (Korea Pyeongchon) - Secondary region</li>
 * <li>JP1 (Japan Tokyo) - For Asia-Pacific expansion</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://kr1-api-object-storage.nhncloudservice.com")
 *         .bucket("my-bucket").accessKey("NHN_ACCESS_KEY").secretKey("NHN_SECRET_KEY").region("KR1").build();
 *
 * NhnCloudProvider provider = new NhnCloudProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://{region}-api-object-storage.nhncloudservice.com
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: Frequently accessed data with high performance</li>
 * <li>IA (Infrequent Access): Less frequently accessed data with lower costs</li>
 * <li>Archive: Long-term archival storage with lowest costs</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Gaming platform asset storage and distribution</li>
 * <li>Mobile game backend storage</li>
 * <li>Entertainment and media content delivery</li>
 * <li>Live streaming and VOD storage</li>
 * <li>E-commerce platform storage</li>
 * <li>Big data analytics and AI/ML workloads</li>
 * <li>Backup and disaster recovery</li>
 * </ul>
 * <p>
 * <strong>Integration:</strong> Seamlessly integrates with NHN Cloud services including GameAnvil (game server
 * platform), Gamebase (game backend), CDN, and AI services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NhnCloudProvider extends GenericS3Provider {

    /**
     * Constructs an NHN Cloud Object Storage provider with the given context.
     *
     * @param context The storage context, containing NHN Cloud-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public NhnCloudProvider(Context context) {
        super(context);
    }

}

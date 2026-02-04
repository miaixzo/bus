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
 * Storage service provider for Naver Cloud Object Storage (S3-compatible). Naver Cloud Platform is South Korea's
 * leading cloud service provider, offering enterprise-grade object storage solutions optimized for the Korean market.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy integration</li>
 * <li>Optimized network performance for South Korea</li>
 * <li>High availability with 99.9% SLA</li>
 * <li>Data sovereignty with Korean data centers</li>
 * <li>Integration with Naver ecosystem services</li>
 * <li>Competitive pricing for Korean market</li>
 * </ul>
 * <p>
 * <strong>Available Regions:</strong>
 * <ul>
 * <li>KR (Korea) - Primary region with multiple availability zones</li>
 * <li>KR-1 (Korea Zone 1)</li>
 * <li>KR-2 (Korea Zone 2)</li>
 * <li>SGN (Singapore) - For global expansion</li>
 * <li>JPN (Japan) - For Asia-Pacific coverage</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://kr.object.ncloudstorage.com") // or sgn, jpn
 *         .bucket("my-bucket").accessKey("NAVER_ACCESS_KEY").secretKey("NAVER_SECRET_KEY").region("kr").build();
 *
 * NaverCloudProvider provider = new NaverCloudProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://{region}.object.ncloudstorage.com
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: Frequently accessed data with high performance</li>
 * <li>IA (Infrequent Access): Less frequently accessed data with lower storage costs</li>
 * <li>Archive: Long-term archival storage with lowest costs</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Korean market applications and services</li>
 * <li>Mobile app backend storage (LINE, Naver apps)</li>
 * <li>Media streaming and content delivery</li>
 * <li>Big data analytics and AI/ML workloads</li>
 * <li>Backup and disaster recovery</li>
 * <li>E-commerce and gaming platforms</li>
 * </ul>
 * <p>
 * <strong>Integration:</strong> Seamlessly integrates with other Naver Cloud Platform services including CDN, Media
 * Services, and AI services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NaverCloudProvider extends GenericS3Provider {

    /**
     * Constructs a Naver Cloud Object Storage provider with the given context.
     *
     * @param context The storage context, containing Naver Cloud-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public NaverCloudProvider(Context context) {
        super(context);
    }

}

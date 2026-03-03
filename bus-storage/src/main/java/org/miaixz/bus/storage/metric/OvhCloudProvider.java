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
 * Storage service provider for OVHcloud Object Storage (S3-compatible). OVHcloud is Europe's largest cloud provider,
 * offering reliable and GDPR-compliant object storage solutions with global data center coverage.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy migration</li>
 * <li>GDPR-compliant with European data sovereignty</li>
 * <li>Global presence with 30+ data centers worldwide</li>
 * <li>High performance with low latency</li>
 * <li>Competitive pricing with transparent costs</li>
 * <li>99.9% SLA guarantee</li>
 * </ul>
 * <p>
 * <strong>Available Regions:</strong>
 * <ul>
 * <li>GRA (Gravelines, France)</li>
 * <li>SBG (Strasbourg, France)</li>
 * <li>UK (London, United Kingdom)</li>
 * <li>DE (Frankfurt, Germany)</li>
 * <li>WAW (Warsaw, Poland)</li>
 * <li>BHS (Beauharnois, Canada)</li>
 * <li>SYD (Sydney, Australia)</li>
 * <li>SGP (Singapore)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://s3.gra.io.cloud.ovh.net") // or sbg, uk, de, waw, bhs, syd, sgp
 *         .bucket("my-bucket").accessKey("OVH_ACCESS_KEY").secretKey("OVH_SECRET_KEY").region("gra") // or sbg, uk, de,
 *                                                                                                    // waw, bhs, syd,
 *                                                                                                    // sgp
 *         .build();
 *
 * OvhCloudProvider provider = new OvhCloudProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://s3.{region}.io.cloud.ovh.net
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: Frequently accessed data with high performance</li>
 * <li>Archive: Long-term storage with lower costs (OpenStack Swift based)</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>European enterprise applications requiring GDPR compliance</li>
 * <li>Multi-region content distribution</li>
 * <li>Backup and disaster recovery</li>
 * <li>Big data and analytics workloads</li>
 * <li>Media streaming and content delivery</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OvhCloudProvider extends GenericS3Provider {

    /**
     * Constructs an OVHcloud Object Storage provider with the given context.
     *
     * @param context The storage context, containing OVHcloud-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public OvhCloudProvider(Context context) {
        super(context);
    }

}

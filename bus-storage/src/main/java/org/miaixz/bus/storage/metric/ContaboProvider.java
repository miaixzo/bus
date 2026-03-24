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
 * Storage service provider for Contabo Object Storage (S3-compatible). Contabo is a German cloud hosting provider
 * offering cost-effective and high-performance object storage solutions with European data centers.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy integration</li>
 * <li>Extremely competitive pricing</li>
 * <li>European data centers (Germany)</li>
 * <li>GDPR compliant with German data residency</li>
 * <li>High-performance SSD storage</li>
 * <li>No hidden fees or egress charges</li>
 * </ul>
 * <p>
 * <strong>Available Regions:</strong>
 * <ul>
 * <li>EU (European Union) - Germany data centers</li>
 * <li>US (United States) - US data centers</li>
 * <li>SG (Singapore) - Asia-Pacific data centers</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://eu2.contabostorage.com") // or usc1, ap1
 *         .bucket("my-bucket").accessKey("CONTABO_ACCESS_KEY").secretKey("CONTABO_SECRET_KEY").region("eu2") // or
 *                                                                                                            // usc1,
 *                                                                                                            // ap1
 *         .build();
 *
 * ContaboProvider provider = new ContaboProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong>
 * <ul>
 * <li>Europe: https://eu2.contabostorage.com</li>
 * <li>US Central: https://usc1.contabostorage.com</li>
 * <li>Asia Pacific: https://ap1.contabostorage.com</li>
 * </ul>
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: High-performance storage for frequently accessed data</li>
 * </ul>
 * <p>
 * <strong>Pricing Highlights:</strong>
 * <ul>
 * <li>Storage: €2.49/TB/month (250GB minimum)</li>
 * <li>Extremely cost-effective compared to major cloud providers</li>
 * <li>No egress/bandwidth charges</li>
 * <li>No API request charges</li>
 * <li>Flat-rate pricing model</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Cost-sensitive applications and startups</li>
 * <li>European market applications requiring GDPR compliance</li>
 * <li>Backup and archival storage</li>
 * <li>Media and content distribution</li>
 * <li>Development and testing environments</li>
 * <li>Large-scale data storage with predictable costs</li>
 * </ul>
 * <p>
 * <strong>Compliance:</strong> GDPR, ISO 27001, German data protection laws
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ContaboProvider extends GenericS3Provider {

    /**
     * Constructs a Contabo Object Storage provider with the given context.
     *
     * @param context The storage context, containing Contabo-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public ContaboProvider(Context context) {
        super(context);
    }

}

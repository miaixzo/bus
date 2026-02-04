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
 * Storage service provider for Scaleway Object Storage (S3-compatible). Scaleway is a leading European cloud provider
 * based in France, offering cost-effective and GDPR-compliant object storage solutions.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>GDPR-compliant with European data residency</li>
 * <li>Multi-region support across Europe</li>
 * <li>Competitive pricing with no egress fees within Scaleway ecosystem</li>
 * <li>High durability (99.999999999% - 11 nines)</li>
 * </ul>
 * <p>
 * <strong>Available Regions:</strong>
 * <ul>
 * <li>fr-par (Paris, France) - Default region</li>
 * <li>nl-ams (Amsterdam, Netherlands)</li>
 * <li>pl-waw (Warsaw, Poland)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://s3.fr-par.scw.cloud") // or nl-ams, pl-waw
 *         .bucket("my-bucket").accessKey("SCW_ACCESS_KEY").secretKey("SCW_SECRET_KEY").region("fr-par") // or nl-ams,
 *                                                                                                       // pl-waw
 *         .build();
 *
 * ScalewayProvider provider = new ScalewayProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://s3.{region}.scw.cloud
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>European market applications requiring GDPR compliance</li>
 * <li>Backup and archival storage</li>
 * <li>Static website hosting</li>
 * <li>Media and content delivery</li>
 * <li>Data lakes and analytics</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ScalewayProvider extends GenericS3Provider {

    /**
     * Constructs a Scaleway Object Storage provider with the given context.
     *
     * @param context The storage context, containing Scaleway-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public ScalewayProvider(Context context) {
        super(context);
    }

}

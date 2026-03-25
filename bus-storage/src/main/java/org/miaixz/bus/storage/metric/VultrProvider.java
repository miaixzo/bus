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
 * Storage service provider for Vultr Object Storage (S3-compatible). Vultr is a global cloud computing platform
 * offering high-performance SSD cloud servers and object storage with competitive pricing.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>Global network with 25+ locations worldwide</li>
 * <li>High-performance NVMe SSD storage</li>
 * <li>Simple and transparent pricing</li>
 * <li>Free bandwidth between Vultr services</li>
 * <li>Developer-friendly with extensive API support</li>
 * </ul>
 * <p>
 * <strong>Available Regions:</strong>
 * <ul>
 * <li>ewr (New Jersey, USA)</li>
 * <li>ord (Chicago, USA)</li>
 * <li>dfw (Dallas, USA)</li>
 * <li>sea (Seattle, USA)</li>
 * <li>lax (Los Angeles, USA)</li>
 * <li>atl (Atlanta, USA)</li>
 * <li>sjc (Silicon Valley, USA)</li>
 * <li>ams (Amsterdam, Netherlands)</li>
 * <li>lhr (London, UK)</li>
 * <li>fra (Frankfurt, Germany)</li>
 * <li>cdg (Paris, France)</li>
 * <li>sgp (Singapore)</li>
 * <li>nrt (Tokyo, Japan)</li>
 * <li>syd (Sydney, Australia)</li>
 * <li>yto (Toronto, Canada)</li>
 * <li>mel (Melbourne, Australia)</li>
 * <li>blr (Bangalore, India)</li>
 * <li>mex (Mexico City, Mexico)</li>
 * <li>waw (Warsaw, Poland)</li>
 * <li>mad (Madrid, Spain)</li>
 * <li>jnb (Johannesburg, South Africa)</li>
 * <li>del (Delhi NCR, India)</li>
 * <li>icn (Seoul, South Korea)</li>
 * <li>sao (São Paulo, Brazil)</li>
 * <li>sto (Stockholm, Sweden)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://ewr1.vultrobjects.com") // Region-specific endpoint
 *         .bucket("my-bucket").accessKey("VULTR_ACCESS_KEY").secretKey("VULTR_SECRET_KEY").region("ewr1").build();
 *
 * VultrProvider provider = new VultrProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://{region}.vultrobjects.com
 * <p>
 * <strong>Pricing Highlights:</strong>
 * <ul>
 * <li>Storage: $5/TB/month</li>
 * <li>Free bandwidth between Vultr services in same region</li>
 * <li>Outbound bandwidth: $0.01/GB</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Application data storage and backups</li>
 * <li>Static website hosting</li>
 * <li>Media and content distribution</li>
 * <li>Development and testing environments</li>
 * <li>Log aggregation and analytics</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VultrProvider extends GenericS3Provider {

    /**
     * Constructs a Vultr Object Storage provider with the given context.
     *
     * @param context The storage context, containing Vultr-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public VultrProvider(Context context) {
        super(context);
    }

}

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
 * Storage service provider for Exoscale Object Storage (S3-compatible). Exoscale is a European cloud computing platform
 * based in Switzerland, offering GDPR-compliant and privacy-focused object storage solutions.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>Swiss data sovereignty and GDPR compliance</li>
 * <li>European data centers (Switzerland, Germany, Austria, Bulgaria)</li>
 * <li>High security and privacy standards</li>
 * <li>Simple and transparent pricing</li>
 * <li>99.95% SLA guarantee</li>
 * </ul>
 * <p>
 * <strong>Available Zones:</strong>
 * <ul>
 * <li>ch-gva-2 (Geneva, Switzerland) - Primary zone</li>
 * <li>ch-dk-2 (Zurich, Switzerland)</li>
 * <li>de-fra-1 (Frankfurt, Germany)</li>
 * <li>de-muc-1 (Munich, Germany)</li>
 * <li>at-vie-1 (Vienna, Austria)</li>
 * <li>bg-sof-1 (Sofia, Bulgaria)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://sos-ch-gva-2.exo.io") // Zone-specific endpoint
 *         .bucket("my-bucket").accessKey("EXO_ACCESS_KEY").secretKey("EXO_SECRET_KEY").region("ch-gva-2").build();
 *
 * ExoscaleProvider provider = new ExoscaleProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint Format:</strong> https://sos-{zone}.exo.io
 * <p>
 * <strong>Storage Classes:</strong>
 * <ul>
 * <li>Standard: High-performance storage for frequently accessed data</li>
 * </ul>
 * <p>
 * <strong>Pricing Highlights:</strong>
 * <ul>
 * <li>Storage: CHF 0.021/GB/month (~€0.022)</li>
 * <li>Outbound transfer: CHF 0.05/GB (~€0.052)</li>
 * <li>No charges for inbound transfer</li>
 * <li>No API request charges</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>European applications requiring GDPR compliance</li>
 * <li>Swiss data residency requirements</li>
 * <li>Privacy-sensitive data storage</li>
 * <li>Backup and disaster recovery</li>
 * <li>Static website hosting</li>
 * <li>Media and content distribution</li>
 * </ul>
 * <p>
 * <strong>Compliance:</strong> GDPR, ISO 27001, Swiss Federal Data Protection Act (FADP)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExoscaleProvider extends GenericS3Provider {

    /**
     * Constructs an Exoscale Object Storage provider with the given context.
     *
     * @param context The storage context, containing Exoscale-specific endpoint, bucket, access key, secret key, and
     *                zone configurations.
     */
    public ExoscaleProvider(Context context) {
        super(context);
    }

}

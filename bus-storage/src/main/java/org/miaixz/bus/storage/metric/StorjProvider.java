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
 * Storage service provider for Storj DCS (Decentralized Cloud Storage) with S3-compatible API. Storj is a decentralized
 * cloud storage network that distributes encrypted data across thousands of independent nodes worldwide.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>Decentralized architecture with no single point of failure</li>
 * <li>End-to-end encryption with client-side key management</li>
 * <li>Automatic erasure coding and redundancy (80/29 scheme)</li>
 * <li>Global distribution across 10,000+ storage nodes</li>
 * <li>Significantly lower costs than traditional cloud storage</li>
 * </ul>
 * <p>
 * <strong>Architecture:</strong>
 * <ul>
 * <li>Files split into 80 pieces, only 29 needed for recovery</li>
 * <li>Each piece stored on different nodes globally</li>
 * <li>Zero-knowledge encryption - Storj cannot access your data</li>
 * <li>Blockchain-based node reputation system</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://gateway.storjshare.io").bucket("my-bucket")
 *         .accessKey("STORJ_ACCESS_KEY").secretKey("STORJ_SECRET_KEY").region("global").build();
 *
 * StorjProvider provider = new StorjProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint:</strong> https://gateway.storjshare.io
 * <p>
 * <strong>Pricing Highlights:</strong>
 * <ul>
 * <li>Storage: $4/TB/month (50% cheaper than AWS S3)</li>
 * <li>Egress: $7/TB (90% cheaper than AWS S3)</li>
 * <li>No API request charges</li>
 * <li>Free tier: 25GB storage + 25GB egress/month</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Cost-effective alternative to centralized cloud storage</li>
 * <li>Privacy-sensitive data storage</li>
 * <li>Backup and disaster recovery</li>
 * <li>Media and content distribution</li>
 * <li>Web3 and blockchain applications</li>
 * <li>Censorship-resistant data storage</li>
 * <li>Compliance with data sovereignty requirements</li>
 * </ul>
 * <p>
 * <strong>Security:</strong>
 * <ul>
 * <li>AES-256-GCM encryption</li>
 * <li>Client-side encryption keys</li>
 * <li>Zero-knowledge architecture</li>
 * <li>No single node has complete file</li>
 * </ul>
 * <p>
 * <strong>Performance:</strong>
 * <ul>
 * <li>Parallel downloads from multiple nodes</li>
 * <li>Automatic node selection for optimal performance</li>
 * <li>Global CDN-like distribution</li>
 * <li>99.95% durability guarantee</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class StorjProvider extends GenericS3Provider {

    /**
     * Constructs a Storj DCS provider with the given context.
     *
     * @param context The storage context, containing Storj-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public StorjProvider(Context context) {
        super(context);
    }

}

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
 * Storage service provider for Filebase decentralized storage (S3-compatible). Filebase is a decentralized storage
 * aggregator that provides S3-compatible access to multiple decentralized networks including IPFS, Storj, and Sia.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>S3-compatible API for seamless integration</li>
 * <li>Decentralized storage across multiple networks (IPFS, Storj, Sia)</li>
 * <li>Automatic geo-replication across 3+ networks</li>
 * <li>Built-in redundancy and data durability</li>
 * <li>Web3-native with blockchain integration</li>
 * <li>Competitive pricing with predictable costs</li>
 * </ul>
 * <p>
 * <strong>Supported Networks:</strong>
 * <ul>
 * <li>IPFS (InterPlanetary File System)</li>
 * <li>Storj (Decentralized cloud storage)</li>
 * <li>Sia (Blockchain-based storage)</li>
 * <li>Skynet (Decentralized CDN)</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://s3.filebase.com").bucket("my-bucket")
 *         .accessKey("FILEBASE_ACCESS_KEY").secretKey("FILEBASE_SECRET_KEY").region("us-east-1").build();
 *
 * FilebaseProvider provider = new FilebaseProvider(context);
 * }</pre>
 * <p>
 * <strong>Endpoint:</strong> https://s3.filebase.com
 * <p>
 * <strong>Pricing Highlights:</strong>
 * <ul>
 * <li>Storage: $5.99/TB/month (includes 3x replication)</li>
 * <li>No egress fees</li>
 * <li>No API request charges</li>
 * <li>Flat-rate pricing model</li>
 * </ul>
 * <p>
 * <strong>Use Cases:</strong>
 * <ul>
 * <li>Web3 and blockchain applications</li>
 * <li>NFT metadata and asset storage</li>
 * <li>Decentralized application (dApp) storage</li>
 * <li>Content distribution with IPFS</li>
 * <li>Censorship-resistant data storage</li>
 * <li>Long-term archival with blockchain verification</li>
 * </ul>
 * <p>
 * <strong>Benefits:</strong>
 * <ul>
 * <li>No vendor lock-in - data stored across multiple networks</li>
 * <li>Automatic failover and redundancy</li>
 * <li>IPFS CID (Content Identifier) for each file</li>
 * <li>Immutable storage with blockchain verification</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FilebaseProvider extends GenericS3Provider {

    /**
     * Constructs a Filebase Object Storage provider with the given context.
     *
     * @param context The storage context, containing Filebase-specific endpoint, bucket, access key, secret key, and
     *                region configurations.
     */
    public FilebaseProvider(Context context) {
        super(context);
    }

}

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
 * Storage service provider for DigitalOcean Spaces. This provider integrates with DigitalOcean Spaces using
 * S3-compatible API.
 * <p>
 * DigitalOcean Spaces is a simple, scalable object storage service designed for developers. It provides S3-compatible
 * API with built-in CDN integration for fast content delivery.
 * <p>
 * <strong>Configuration:</strong>
 * <ul>
 * <li>endpoint: DigitalOcean Spaces endpoint (e.g., https://{region}.digitaloceanspaces.com)</li>
 * <li>bucket: Space name (bucket) in DigitalOcean Spaces</li>
 * <li>accessKey: DigitalOcean Spaces access key</li>
 * <li>secretKey: DigitalOcean Spaces secret key</li>
 * <li>region: DigitalOcean region (e.g., nyc3, sfo3, sgp1, fra1)</li>
 * </ul>
 * <p>
 * <strong>Endpoint Format:</strong>
 * 
 * <pre>
 * Standard Endpoint:
 * https://{region}.digitaloceanspaces.com
 *
 * CDN Endpoint (for public access):
 * https://{space-name}.{region}.cdn.digitaloceanspaces.com
 *
 * Examples:
 * - New York 3: https://nyc3.digitaloceanspaces.com
 * - San Francisco 3: https://sfo3.digitaloceanspaces.com
 * - Singapore 1: https://sgp1.digitaloceanspaces.com
 * - Frankfurt 1: https://fra1.digitaloceanspaces.com
 * - Amsterdam 3: https://ams3.digitaloceanspaces.com
 * </pre>
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * Context context = Context.builder().endpoint("https://nyc3.digitaloceanspaces.com").bucket("my-space")
 *         .accessKey("your-access-key").secretKey("your-secret-key").region("nyc3").build();
 *
 * DigitalOceanProvider provider = new DigitalOceanProvider(context);
 * provider.upload("file.txt", fileBytes);
 * }</pre>
 * <p>
 * <strong>Supported Regions:</strong>
 * <ul>
 * <li>Americas:
 * <ul>
 * <li>nyc3 - New York 3</li>
 * <li>sfo3 - San Francisco 3</li>
 * </ul>
 * </li>
 * <li>Europe:
 * <ul>
 * <li>fra1 - Frankfurt 1</li>
 * <li>ams3 - Amsterdam 3</li>
 * </ul>
 * </li>
 * <li>Asia Pacific:
 * <ul>
 * <li>sgp1 - Singapore 1</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * <strong>Features:</strong>
 * <ul>
 * <li>S3-compatible API for easy migration</li>
 * <li>Built-in CDN with 300+ global edge locations</li>
 * <li>Simple, predictable pricing ($5/month for 250GB)</li>
 * <li>Free outbound transfer to DigitalOcean products</li>
 * <li>Automatic SSL/TLS encryption</li>
 * <li>CORS support for web applications</li>
 * <li>Lifecycle policies for automatic deletion</li>
 * <li>Access control with Spaces ACLs</li>
 * </ul>
 * <p>
 * <strong>CDN Integration:</strong>
 * 
 * <pre>
 * Enable CDN for your Space to get:
 * - Global content delivery
 * - Custom domain support
 * - Free SSL certificates
 * - Cache purging
 * - Access logs
 * </pre>
 * <p>
 * <strong>Pricing (as of 2024):</strong>
 * <ul>
 * <li>Storage: $5/month for 250GB (additional $0.02/GB)</li>
 * <li>Outbound Transfer: $0.01/GB (free to DO products)</li>
 * <li>CDN: Included at no extra cost</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DigitalOceanProvider extends GenericS3Provider {

    /**
     * Constructs a DigitalOcean Spaces provider with the given context. Initializes the S3-compatible client using the
     * provided credentials and endpoint configuration.
     * <p>
     * The endpoint should follow the format: {@code https://{region}.digitaloceanspaces.com}
     * <p>
     * Where {region} is the DigitalOcean region (e.g., nyc3, sfo3, sgp1, fra1, ams3).
     * <p>
     * <strong>Getting Started:</strong>
     * <ol>
     * <li>Create a Space in DigitalOcean Control Panel</li>
     * <li>Generate Spaces access keys (API → Spaces Keys)</li>
     * <li>Use the access key and secret key in your configuration</li>
     * <li>Optionally enable CDN for public content delivery</li>
     * </ol>
     *
     * @param context The storage context, containing endpoint, bucket (space name), access key, secret key, region, and
     *                other configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public DigitalOceanProvider(Context context) {
        super(context);
    }

}

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
package org.miaixz.bus.cache;

/**
 * Core cache backend options shared by cache consumers and starter-side property binding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Options {

    /**
     * Cache storage backend type.
     */
    private String type;

    /**
     * Maximum number of entries. Applies to {@code memory}, {@code caffeine}, {@code guava}.
     */
    private long maxSize = 10_000;

    /**
     * Default TTL in milliseconds. Applies to {@code memory}, {@code caffeine}, {@code guava}.
     */
    private long expire = 3_600_000;

    /**
     * Comma-separated {@code host:port} server list for {@code memcached}.
     */
    private String nodes;

    /**
     * Redis connection configuration. Applies to {@code redis} and {@code redis-cluster}.
     */
    private Redis redis = new Redis();

    /**
     * Creates an options holder with the module defaults.
     */
    public Options() {
    }

    /**
     * Returns the configured backend type.
     *
     * @return backend type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the backend type.
     *
     * @param type backend type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the configured maximum cache size.
     *
     * @return maximum number of entries
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum cache size.
     *
     * @param maxSize maximum number of entries
     */
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Returns the default expiration in milliseconds.
     *
     * @return expiration in milliseconds
     */
    public long getExpire() {
        return expire;
    }

    /**
     * Sets the default expiration in milliseconds.
     *
     * @param expire expiration in milliseconds
     */
    public void setExpire(long expire) {
        this.expire = expire;
    }

    /**
     * Returns the configured server list for node-based backends.
     *
     * @return comma-separated host list
     */
    public String getNodes() {
        return nodes;
    }

    /**
     * Sets the configured server list for node-based backends.
     *
     * @param nodes comma-separated host list
     */
    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    /**
     * Returns the Redis-specific options block.
     *
     * @return Redis options
     */
    public Redis getRedis() {
        return redis;
    }

    /**
     * Sets the Redis-specific options block.
     *
     * @param redis Redis options
     */
    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    /**
     * Redis connection and pool settings.
     */
    public static class Redis {

        /**
         * Redis server hostname. Applies to single-node ({@code redis}) mode.
         */
        private String host = "localhost";

        /**
         * Redis server port. Applies to single-node ({@code redis}) mode.
         */
        private int port = 6379;

        /**
         * Redis authentication password. Applies to both {@code redis} and {@code redis-cluster}.
         */
        private String password;

        /**
         * Connection and read timeout in milliseconds.
         */
        private int timeout = 2000;

        /**
         * Maximum total connections in the pool.
         */
        private int maxActive = 8;

        /**
         * Maximum idle connections in the pool.
         */
        private int maxIdle = 8;

        /**
         * Minimum idle connections in the pool.
         */
        private int minIdle = 0;

        /**
         * Comma-separated {@code host:port} cluster node list. Applies to {@code redis-cluster} mode.
         */
        private String nodes;

        /**
         * Creates a Redis options holder with the module defaults.
         */
        public Redis() {
        }

        /**
         * Returns the Redis host name.
         *
         * @return Redis host
         */
        public String getHost() {
            return host;
        }

        /**
         * Sets the Redis host name.
         *
         * @param host Redis host
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * Returns the Redis port.
         *
         * @return Redis port
         */
        public int getPort() {
            return port;
        }

        /**
         * Sets the Redis port.
         *
         * @param port Redis port
         */
        public void setPort(int port) {
            this.port = port;
        }

        /**
         * Returns the Redis password.
         *
         * @return Redis password
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets the Redis password.
         *
         * @param password Redis password
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * Returns the Redis timeout in milliseconds.
         *
         * @return timeout in milliseconds
         */
        public int getTimeout() {
            return timeout;
        }

        /**
         * Sets the Redis timeout in milliseconds.
         *
         * @param timeout timeout in milliseconds
         */
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        /**
         * Returns the maximum number of active pooled connections.
         *
         * @return maximum active connections
         */
        public int getMaxActive() {
            return maxActive;
        }

        /**
         * Sets the maximum number of active pooled connections.
         *
         * @param maxActive maximum active connections
         */
        public void setMaxActive(int maxActive) {
            this.maxActive = maxActive;
        }

        /**
         * Returns the maximum number of idle pooled connections.
         *
         * @return maximum idle connections
         */
        public int getMaxIdle() {
            return maxIdle;
        }

        /**
         * Sets the maximum number of idle pooled connections.
         *
         * @param maxIdle maximum idle connections
         */
        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        /**
         * Returns the minimum number of idle pooled connections.
         *
         * @return minimum idle connections
         */
        public int getMinIdle() {
            return minIdle;
        }

        /**
         * Sets the minimum number of idle pooled connections.
         *
         * @param minIdle minimum idle connections
         */
        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        /**
         * Returns the configured Redis cluster node list.
         *
         * @return cluster node list
         */
        public String getNodes() {
            return nodes;
        }

        /**
         * Sets the configured Redis cluster node list.
         *
         * @param nodes cluster node list
         */
        public void setNodes(String nodes) {
            this.nodes = nodes;
        }

    }

}

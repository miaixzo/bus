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
package org.miaixz.bus.spring.jdbc;

import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Defines one named datasource after compatible property binding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DataSourceDefinition {

    /**
     * Hikari-specific JDBC URL property name.
     */
    private static final String JDBC_URL = "jdbc-url";

    /**
     * Routing name.
     */
    private String name;
    /**
     * JDBC connection URL.
     */
    private String url;
    /**
     * Authentication username.
     */
    private String username;
    /**
     * Authentication password.
     */
    private String password;
    /**
     * JDBC driver class name.
     */
    private String driverClassName;
    /**
     * Datasource implementation class name.
     */
    private String type;
    /**
     * Hikari-compatible pool properties.
     */
    private Map<String, Object> hikari = Map.of();

    /**
     * Creates an empty definition for Spring property binding.
     */
    public DataSourceDefinition() {
        // No initialization required.
    }

    /**
     * Creates and validates a complete datasource definition.
     *
     * @param name            unique routing name
     * @param url             JDBC connection URL
     * @param username        authentication username
     * @param password        authentication password
     * @param driverClassName JDBC driver class name
     * @param type            optional datasource implementation type
     * @param hikari          Hikari-compatible pool properties
     */
    public DataSourceDefinition(String name, String url, String username, String password, String driverClassName,
            String type, Map<String, Object> hikari) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.type = type;
        setHikari(hikari);
        validate();
    }

    /**
     * Normalizes and validates this definition after property binding.
     *
     * @return this validated definition
     */
    public DataSourceDefinition validate() {
        this.name = StringKit.trim(this.name);
        if (StringKit.isEmpty(this.name)) {
            throw new IllegalArgumentException("Datasource name is required");
        }
        this.url = StringKit.trim(this.url);
        if (StringKit.isEmpty(this.url)) {
            this.url = StringKit.trim(StringKit.toString(this.hikari.get(JDBC_URL)));
        }
        if (StringKit.isEmpty(this.url)) {
            throw new IllegalArgumentException("Datasource url is required for: " + this.name);
        }
        this.type = StringKit.trim(this.type);
        return this;
    }

    /**
     * Returns the unique datasource routing name.
     *
     * @return routing name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the datasource routing name before validation.
     *
     * @param name routing name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the normalized JDBC connection URL.
     *
     * @return JDBC connection URL
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the JDBC connection URL before validation.
     *
     * @param url JDBC connection URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the configured datasource authentication username.
     *
     * @return authentication username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the datasource authentication username.
     *
     * @param username authentication username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the configured datasource authentication password.
     *
     * @return authentication password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the datasource authentication password.
     *
     * @param password authentication password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the configured JDBC driver class name.
     *
     * @return JDBC driver class name
     */
    public String getDriverClassName() {
        return this.driverClassName;
    }

    /**
     * Sets the JDBC driver class name.
     *
     * @param driverClassName JDBC driver class name
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * Returns the normalized datasource implementation class name.
     *
     * @return datasource implementation class name
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the datasource implementation class name before validation.
     *
     * @param type datasource implementation class name
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns an immutable snapshot of Hikari-compatible pool properties.
     *
     * @return immutable Hikari-compatible pool properties
     */
    public Map<String, Object> getHikari() {
        return this.hikari;
    }

    /**
     * Replaces Hikari-compatible pool properties with an immutable copy.
     *
     * @param hikari Hikari-compatible pool properties
     */
    public void setHikari(Map<String, Object> hikari) {
        this.hikari = hikari == null ? Map.of() : Map.copyOf(hikari);
    }

    /**
     * Compares all datasource definition values.
     *
     * @param object candidate object
     * @return {@code true} when all values match
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DataSourceDefinition that)) {
            return false;
        }
        return Objects.equals(this.name, that.name) && Objects.equals(this.url, that.url)
                && Objects.equals(this.username, that.username) && Objects.equals(this.password, that.password)
                && Objects.equals(this.driverClassName, that.driverClassName) && Objects.equals(this.type, that.type)
                && Objects.equals(this.hikari, that.hikari);
    }

    /**
     * Calculates a hash code from all datasource definition values.
     *
     * @return hash code for all datasource definition values
     */
    @Override
    public int hashCode() {
        return Objects
                .hash(this.name, this.url, this.username, this.password, this.driverClassName, this.type, this.hikari);
    }

    /**
     * Returns diagnostic text without exposing endpoint or authentication credentials.
     *
     * @return redacted datasource definition summary
     */
    @Override
    public String toString() {
        return "DataSourceDefinition[name=" + this.name + ", url=***, username=***, password=***, driverClassName="
                + this.driverClassName + ", type=" + this.type + ", hikari=" + this.hikari.keySet() + "]";
    }

}

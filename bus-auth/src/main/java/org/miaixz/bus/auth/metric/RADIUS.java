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
package org.miaixz.bus.auth.metric;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.radius.client.RadiusClient;
import org.miaixz.bus.auth.metric.radius.packet.RadiusAttribute;
import org.miaixz.bus.auth.metric.radius.server.RadiusServer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;

/**
 * Defines the sole RADIUS authentication/accounting client and dual-binding server facades.
 */
public final class RADIUS {

    /**
     * Prevents construction.
     */
    private RADIUS() {
        // No initialization required.
    }

    /**
     * Creates one datagram client.
     *
     * @param configuration client configuration
     * @param runtime       runtime
     * @return client
     */
    public static Client client(final ClientConfig configuration, final Runtime runtime) {
        return new RadiusClient(configuration, runtime);
    }

    /**
     * Creates one dual-binding server.
     *
     * @param configuration server configuration
     * @param runtime       runtime
     * @param access        access handler
     * @param accounting    accounting handler
     * @return server
     */
    public static Server server(
            final ServerConfig configuration,
            final Runtime runtime,
            final AccessHandler access,
            final AccountingHandler accounting) {
        return new RadiusServer(configuration, runtime, access, accounting);
    }

    /**
     * Validates and snapshots attributes.
     *
     * @param values attributes
     * @param name   name
     * @return immutable attributes
     */
    static List<RadiusAttribute> bounded(final List<RadiusAttribute> values, final String name) {
        final List<RadiusAttribute> result = List
                .copyOf(Assert.notNull(values, () -> new ValidateException(name + " must not be null")));
        Assert.isTrue(result.size() <= Normal._1024, () -> new ValidateException(name + " exceeds its count limit"));
        return result;
    }

    /**
     * Validates one UDP endpoint and registered port.
     *
     * @param endpoint endpoint
     * @param port     required port
     * @param name     name
     */
    static void endpoint(final Endpoint endpoint, final int port, final String name) {
        final Endpoint value = Assert.notNull(endpoint, () -> new ValidateException("RADIUS " + name + " is null"));
        Assert.isTrue(
                value.protocol() == Protocol.UDP && value.port() == port,
                () -> new ValidateException("RADIUS " + name + " protocol or port is invalid"));
    }

    /**
     * Validates one bounded positive duration.
     *
     * @param value duration
     * @param name  name
     */
    static void duration(final Duration value, final String name) {
        final Duration duration = Assert
                .notNull(value, () -> new ValidateException("RADIUS " + name + " must not be null"));
        Assert.isTrue(
                !duration.isNegative() && !duration.isZero() && duration.compareTo(Duration.ofSeconds(30)) <= 0,
                () -> new ValidateException("RADIUS " + name + " is invalid"));
    }

    /**
     * RADIUS access decision.
     */
    public enum DecisionType {

        /**
         * Accepts access.
         */
        ACCEPT,

        /**
         * Rejects access.
         */
        REJECT,

        /**
         * Continues an authentication challenge.
         */
        CHALLENGE
    }

    /**
     * RADIUS client contract.
     */
    public interface Client {

        /**
         * Exchanges one Access-Request.
         *
         * @param invocation operation context
         * @param request    request
         * @return response outcome
         */
        CompletionStage<Outcome<AccessResponse>> authenticate(Invocation invocation, AccessRequest request);

        /**
         * Exchanges one Accounting-Request.
         *
         * @param invocation operation context
         * @param request    request
         * @return response outcome
         */
        CompletionStage<Outcome<AccountingResponse>> account(Invocation invocation, AccountingRequest request);
    }

    /**
     * Managed dual-binding server contract.
     */
    public interface Server {

        /**
         * Starts both bindings.
         *
         * @param invocation operation context
         * @return start outcome
         */
        CompletionStage<Outcome<Void>> start(Invocation invocation);

        /**
         * Returns the effective authentication endpoint.
         *
         * @return endpoint
         */
        Endpoint authenticationEndpoint();

        /**
         * Returns the effective accounting endpoint.
         *
         * @return endpoint
         */
        Endpoint accountingEndpoint();

        /**
         * Returns whether both bindings are running.
         *
         * @return running state
         */
        boolean running();

        /**
         * Closes accounting then authentication bindings.
         *
         * @return close stage
         */
        CompletionStage<Void> close();
    }

    /**
     * Product access decision port.
     */
    @FunctionalInterface
    public interface AccessHandler {

        /**
         * Handles one verified request.
         *
         * @param invocation operation context
         * @param request    request
         * @return decision stage
         */
        CompletionStage<AccessDecision> handle(Invocation invocation, AccessRequest request);
    }

    /**
     * Product accounting port.
     */
    @FunctionalInterface
    public interface AccountingHandler {

        /**
         * Handles one verified accounting request.
         *
         * @param invocation operation context
         * @param request    request
         * @return completion stage
         */
        CompletionStage<Void> handle(Invocation invocation, AccountingRequest request);
    }

    /**
     * Immutable access request.
     *
     * @param attributes request attributes
     */
    public record AccessRequest(List<RadiusAttribute> attributes) {

        /**
         * Snapshots attributes.
         *
         * @param attributes attributes
         */
        public AccessRequest {
            attributes = bounded(attributes, "RADIUS access attributes");
        }
    }

    /**
     * Immutable access response.
     *
     * @param decision   response decision
     * @param attributes response attributes
     */
    public record AccessResponse(DecisionType decision, List<RadiusAttribute> attributes) {

        /**
         * Validates one response.
         *
         * @param decision   decision
         * @param attributes attributes
         */
        public AccessResponse {
            decision = Assert.notNull(decision, () -> new ValidateException("RADIUS decision must not be null"));
            attributes = bounded(attributes, "RADIUS response attributes");
        }
    }

    /**
     * Immutable accounting request.
     *
     * @param attributes request attributes
     */
    public record AccountingRequest(List<RadiusAttribute> attributes) {

        /**
         * Snapshots attributes.
         *
         * @param attributes attributes
         */
        public AccountingRequest {
            attributes = bounded(attributes, "RADIUS accounting attributes");
        }
    }

    /**
     * Immutable accounting response.
     *
     * @param attributes response attributes
     */
    public record AccountingResponse(List<RadiusAttribute> attributes) {

        /**
         * Snapshots attributes.
         *
         * @param attributes attributes
         */
        public AccountingResponse {
            attributes = bounded(attributes, "RADIUS accounting response attributes");
        }
    }

    /**
     * Immutable product access decision.
     *
     * @param type       decision type
     * @param attributes response attributes
     */
    public record AccessDecision(DecisionType type, List<RadiusAttribute> attributes) {

        /**
         * Validates one decision.
         *
         * @param type       type
         * @param attributes attributes
         */
        public AccessDecision {
            type = Assert.notNull(type, () -> new ValidateException("RADIUS decision type must not be null"));
            attributes = bounded(attributes, "RADIUS decision attributes");
        }
    }

    /**
     * Immutable client configuration.
     *
     * @param authenticationTarget    authentication endpoint
     * @param accountingTarget        accounting endpoint
     * @param authenticationPolicy    authentication policy
     * @param accountingPolicy        accounting policy
     * @param secretKey               SecretResolver identifier
     * @param requestTimeout          single-attempt timeout
     * @param retryCount              retry count from zero through three
     * @param maximumEapFragmentState maximum fragment state bytes
     */
    public record ClientConfig(Endpoint authenticationTarget, Endpoint accountingTarget,
            TransportPolicy authenticationPolicy, TransportPolicy accountingPolicy, String secretKey,
            Duration requestTimeout, int retryCount, int maximumEapFragmentState) {

        /**
         * Validates client configuration.
         *
         * @param authenticationTarget    authentication target
         * @param accountingTarget        accounting target
         * @param authenticationPolicy    authentication client policy
         * @param accountingPolicy        accounting client policy
         * @param secretKey               SecretResolver identifier
         * @param requestTimeout          request timeout
         * @param retryCount              retry count
         * @param maximumEapFragmentState maximum EAP fragment state bytes
         */
        public ClientConfig {
            endpoint(authenticationTarget, Port._1812.getPort(), "authentication target");
            endpoint(accountingTarget, Port._1813.getPort(), "accounting target");
            authenticationPolicy = Assert.notNull(
                    authenticationPolicy,
                    () -> new ValidateException("RADIUS authentication policy must not be null"));
            accountingPolicy = Assert.notNull(
                    accountingPolicy,
                    () -> new ValidateException("RADIUS accounting policy must not be null"));
            secretKey = Assert.notBlank(secretKey, () -> new ValidateException("RADIUS secret key must not be blank"));
            duration(requestTimeout, "request timeout");
            Assert.isTrue(
                    retryCount >= Normal._0 && retryCount <= Normal._3,
                    () -> new ValidateException("RADIUS retry count must be between zero and three"));
            Assert.isTrue(
                    maximumEapFragmentState > Normal._0 && maximumEapFragmentState <= 64 * Normal._1024,
                    () -> new ValidateException("RADIUS EAP fragment state limit is invalid"));
        }
    }

    /**
     * Immutable server configuration.
     *
     * @param authenticationBind   authentication bind endpoint
     * @param accountingBind       accounting bind endpoint
     * @param authenticationPolicy authentication server policy
     * @param accountingPolicy     accounting server policy
     * @param secretKey            SecretResolver identifier
     * @param maxInFlight          maximum concurrent packets
     * @param requestTimeout       handler timeout
     * @param duplicateCacheTtl    duplicate cache TTL
     * @param shutdownTimeout      shutdown timeout
     */
    public record ServerConfig(Endpoint authenticationBind, Endpoint accountingBind,
            TransportPolicy authenticationPolicy, TransportPolicy accountingPolicy, String secretKey, int maxInFlight,
            Duration requestTimeout, Duration duplicateCacheTtl, Duration shutdownTimeout) {

        /**
         * Validates server configuration.
         *
         * @param authenticationBind   authentication bind endpoint
         * @param accountingBind       accounting bind endpoint
         * @param authenticationPolicy authentication server policy
         * @param accountingPolicy     accounting server policy
         * @param secretKey            SecretResolver identifier
         * @param maxInFlight          maximum concurrent packets
         * @param requestTimeout       handler timeout
         * @param duplicateCacheTtl    duplicate cache TTL
         * @param shutdownTimeout      shutdown timeout
         */
        public ServerConfig {
            endpoint(authenticationBind, Port._1812.getPort(), "authentication bind");
            endpoint(accountingBind, Port._1813.getPort(), "accounting bind");
            authenticationPolicy = Assert.notNull(
                    authenticationPolicy,
                    () -> new ValidateException("RADIUS authentication policy must not be null"));
            accountingPolicy = Assert.notNull(
                    accountingPolicy,
                    () -> new ValidateException("RADIUS accounting policy must not be null"));
            secretKey = Assert.notBlank(secretKey, () -> new ValidateException("RADIUS secret key must not be blank"));
            Assert.isTrue(
                    maxInFlight > Normal._0 && maxInFlight <= Normal._1024,
                    () -> new ValidateException("RADIUS maxInFlight is invalid"));
            duration(requestTimeout, "request timeout");
            duration(duplicateCacheTtl, "duplicate cache TTL");
            duration(shutdownTimeout, "shutdown timeout");
        }
    }

}

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
package org.miaixz.bus.auth.protocol.radius;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.auth.protocol.radius.client.RadiusClient;
import org.miaixz.bus.auth.protocol.radius.packet.RadiusAttribute;
import org.miaixz.bus.auth.protocol.radius.server.RadiusServer;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.udp.UdpNetwork;

/**
 * Defines the sole RADIUS authentication/accounting client and dual-binding server facades.
 *
 * @author Kimi Liu
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
     * @param network       Fabric UDP network
     * @param resolver      runtime-bound DNS resolver
     * @param secrets       shared-secret resolver
     * @param random        secure identifier and authenticator entropy
     * @return client
     * @throws ValidateException if a required configuration or collaborator is {@code null}
     */
    public static Client client(
            final ClientConfig configuration,
            final UdpNetwork network,
            final DnsResolver resolver,
            final SecretResolver secrets,
            final SecureRandom random) {
        return new RadiusClient(configuration, network, resolver, secrets, random);
    }

    /**
     * Creates one dual-binding server.
     *
     * @param configuration server configuration
     * @param network       Fabric UDP network
     * @param resolver      runtime-bound DNS resolver
     * @param secrets       shared-secret resolver
     * @param access        access handler
     * @param accounting    accounting handler
     * @return server
     * @throws ValidateException if a required configuration or collaborator is {@code null}
     */
    public static Server server(
            final ServerConfig configuration,
            final UdpNetwork network,
            final DnsResolver resolver,
            final SecretResolver secrets,
            final AccessHandler access,
            final AccountingHandler accounting) {
        return new RadiusServer(configuration, network, resolver, secrets, access, accounting);
    }

    /**
     * Validates and snapshots attributes.
     *
     * @param values attributes
     * @param name   name
     * @return immutable attributes
     * @throws ValidateException if values are absent or exceed 1024 entries
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
     * @throws ValidateException if the endpoint is absent or is not the required UDP port
     */
    static void endpoint(final Address endpoint, final int port, final String name) {
        final Address value = Assert.notNull(endpoint, () -> new ValidateException("RADIUS " + name + " is null"));
        Assert.isTrue(
                value.protocol() == Protocol.UDP && value.port() == port,
                () -> new ValidateException("RADIUS " + name + " protocol or port is invalid"));
    }

    /**
     * Validates one bounded positive duration.
     *
     * @param value duration
     * @param name  name
     * @throws ValidateException if the duration is absent, non-positive, or exceeds 30 seconds
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
     *
     * @author Kimi Liu
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
     *
     * @author Kimi Liu
     */
    public interface Client {

        /**
         * Exchanges one Access-Request.
         *
         * @param invocation operation context
         * @param request    request
         * @return response outcome
         */
        CompletionStage<Outcome<AccessResponse>> authenticate(Context invocation, AccessRequest request);

        /**
         * Exchanges one Accounting-Request.
         *
         * @param invocation operation context
         * @param request    request
         * @return response outcome
         */
        CompletionStage<Outcome<AccountingResponse>> account(Context invocation, AccountingRequest request);
    }

    /**
     * Managed dual-binding server contract.
     *
     * @author Kimi Liu
     */
    public interface Server extends Lifecycle {

        /**
         * Maps the observable binding state to the shared lifecycle contract.
         *
         * @return {@link State#RUNNING} while both bindings run, otherwise {@link State#UNKNOWN}
         */
        @Override
        default State state() {
            return running() ? State.RUNNING : State.UNKNOWN;
        }

        /**
         * Starts both bindings.
         *
         * @param invocation operation context
         * @return start outcome
         */
        CompletionStage<Outcome<Void>> start(Context invocation);

        /**
         * Returns the effective authentication endpoint.
         *
         * @return endpoint
         */
        Address authenticationEndpoint();

        /**
         * Returns the effective accounting endpoint.
         *
         * @return endpoint
         */
        Address accountingEndpoint();

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
     *
     * @author Kimi Liu
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
        CompletionStage<AccessDecision> handle(Context invocation, AccessRequest request);
    }

    /**
     * Product accounting port.
     *
     * @author Kimi Liu
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
        CompletionStage<Void> handle(Context invocation, AccountingRequest request);
    }

    /**
     * Immutable access request.
     *
     * @param attributes request attributes
     * @author Kimi Liu
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
     * @author Kimi Liu
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
     * @author Kimi Liu
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
     * @author Kimi Liu
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
     * @author Kimi Liu
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
     * @author Kimi Liu
     */
    public record ClientConfig(Address authenticationTarget, Address accountingTarget,
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
     * @author Kimi Liu
     */
    public record ServerConfig(Address authenticationBind, Address accountingBind, TransportPolicy authenticationPolicy,
            TransportPolicy accountingPolicy, String secretKey, int maxInFlight, Duration requestTimeout,
            Duration duplicateCacheTtl, Duration shutdownTimeout) {

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

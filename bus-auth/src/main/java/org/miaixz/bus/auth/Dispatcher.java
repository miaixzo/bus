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
package org.miaixz.bus.auth;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.core.lang.Optional;

/**
 * Dispatches one explicitly selected capability against one compiled Source in the current authentication runtime.
 * <p>
 * This interface is the public execution boundary between a project transport adapter and bus-auth. Server-role
 * protocol drivers compile authorization, token, UserInfo, discovery, JWK Set, logout, and related operations into
 * strongly typed capabilities. A project HTTP service retains ownership of its public host, route registration, reverse
 * proxy rules, request lifecycle, and response writing, then invokes the matching capability through this interface.
 * Dispatcher does not start an HTTP server and does not infer a Source or capability from an untrusted request path.
 * </p>
 *
 * <h2>Obtaining the dispatcher</h2>
 * <p>
 * {@link org.miaixz.bus.auth.runtime.RuntimeManager} owns the Dispatcher together with the Registry and runtime
 * lifecycle. Obtain and retain the Dispatcher after the runtime has successfully loaded its first complete registration
 * snapshot:
 * </p>
 *
 * <pre>{@code
 *
 * RuntimeManager runtime = Authorizer.standard(services, registrationLoader).build(startupContext, startupBudget)
 *         .toCompletableFuture().join();
 * Dispatcher dispatcher = runtime.dispatcher();
 * }</pre>
 *
 * <h2>Protocol-specific endpoint configuration</h2>
 * <p>
 * Dispatcher is shared infrastructure; public addresses are not shared protocol semantics. The project must configure a
 * separate route or listener namespace for every server protocol and map each namespace only to capabilities declared
 * by the matching Scheme. Public paths must not expose the bus-auth Source identifier. The project route assembly binds
 * each fixed protocol route set to one exact {@link Registry.Reference} inside the Namespace already selected by the
 * upper project boundary. Namespace selection is complete before the request enters bus-auth; Dispatcher never routes
 * across Namespaces. The path fragments below are recommendations rather than Controller implementations.
 * </p>
 *
 * <h3>OAuth 2.x authorization server</h3>
 * <p>
 * An OAuth-only Source normally uses a fixed {@code /oauth2} namespace and the capability constants in
 * {@link org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ServerScheme}. OAuth endpoint adapters accept and return
 * Fabric HTTP models, so the project preserves the complete inbound request when invoking Dispatcher.
 * </p>
 * <table>
 * <caption>Recommended OAuth 2.x routes</caption> <thead>
 * <tr>
 * <th scope="col">Route</th>
 * <th scope="col">Capability</th>
 * <th scope="col">Operation</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code GET /oauth2/authorize}</td>
 * <td>{@code AUTHORIZATION}</td>
 * <td>Authorization request.</td>
 * </tr>
 * <tr>
 * <td>{@code POST /oauth2/token}</td>
 * <td>{@code TOKEN}</td>
 * <td>Token grants.</td>
 * </tr>
 * <tr>
 * <td>{@code POST /oauth2/introspect}</td>
 * <td>{@code INTROSPECTION}</td>
 * <td>Token introspection.</td>
 * </tr>
 * <tr>
 * <td>{@code POST /oauth2/revoke}</td>
 * <td>{@code REVOCATION}</td>
 * <td>Token revocation.</td>
 * </tr>
 * <tr>
 * <td>{@code POST /oauth2/device-authorization}</td>
 * <td>{@code DEVICE_AUTHORIZATION}</td>
 * <td>Device and user code issuance.</td>
 * </tr>
 * <tr>
 * <td>{@code GET /oauth2/.well-known/oauth-authorization-server}</td>
 * <td>{@code AUTHORIZATION_SERVER_METADATA}</td>
 * <td>RFC 8414 metadata.</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <h3>OpenID Connect Provider</h3>
 * <p>
 * An OpenID Provider uses a distinct fixed {@code /oidc} issuer and route namespace even though it composes OAuth token
 * operations. Its discovery, UserInfo, JWK Set, ID Token, subject, and end-session semantics do not exist in a generic
 * OAuth-only Source. Use the constants in {@link org.miaixz.bus.auth.protocol.oidc.server.OpenIdServerScheme}.
 * </p>
 * <table>
 * <caption>Recommended OpenID Connect routes</caption> <thead>
 * <tr>
 * <th scope="col">Route</th>
 * <th scope="col">Capability</th>
 * <th scope="col">Operation</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code GET /oidc/authorize}</td>
 * <td>{@code AUTHENTICATION}</td>
 * <td>OIDC Authentication Request.</td>
 * </tr>
 * <tr>
 * <td>{@code POST /oidc/token}</td>
 * <td>{@code TOKEN}</td>
 * <td>OAuth token and ID Token issuance.</td>
 * </tr>
 * <tr>
 * <td>{@code GET /oidc/userinfo}</td>
 * <td>{@code USERINFO}</td>
 * <td>Bearer-authorized claims.</td>
 * </tr>
 * <tr>
 * <td>{@code GET /oidc/jwks}</td>
 * <td>{@code JWK_SET}</td>
 * <td>Provider public JWK Set.</td>
 * </tr>
 * <tr>
 * <td>{@code GET /oidc/.well-known/openid-configuration}</td>
 * <td>{@code DISCOVERY}</td>
 * <td>OpenID Provider Metadata.</td>
 * </tr>
 * <tr>
 * <td>{@code GET /oidc/end-session}</td>
 * <td>{@code END_SESSION}</td>
 * <td>RP-Initiated Logout.</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * Optional OAuth introspection, revocation, device authorization, and authorization-server metadata remain under the
 * same OIDC Source namespace when enabled. Do not publish one registration simultaneously as unrelated OAuth and OIDC
 * route roots; the selected server Scheme and its frozen endpoint metadata must remain unambiguous.
 * </p>
 *
 * <h3>SAML 2.0 identity provider</h3>
 * <p>
 * SAML does not use OAuth endpoints or OAuth HTTP message models. A SAML Source normally uses a fixed {@code /saml}
 * namespace and the capabilities in {@link org.miaixz.bus.auth.protocol.saml.server.SamlServerScheme}. The project SAML
 * transport adapter decodes the inbound HTTP-Redirect Binding into a typed {@code AuthnRequest} or
 * {@code LogoutRequest}, invokes Dispatcher, and encodes the signed typed response using the configured HTTP-POST or
 * HTTP-Redirect Binding. The metadata operation has no request model and returns a typed {@code EntityDescriptor}.
 * </p>
 * <table>
 * <caption>Recommended SAML 2.0 routes</caption> <thead>
 * <tr>
 * <th scope="col">Route</th>
 * <th scope="col">Capability</th>
 * <th scope="col">Binding</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code GET /saml/sso}</td>
 * <td>{@code SINGLE_SIGN_ON}</td>
 * <td>Inbound HTTP-Redirect AuthnRequest; outbound HTTP-POST Response to the SP ACS.</td>
 * </tr>
 * <tr>
 * <td>{@code GET /saml/slo}</td>
 * <td>{@code SINGLE_LOGOUT}</td>
 * <td>Inbound HTTP-Redirect LogoutRequest; bound LogoutResponse.</td>
 * </tr>
 * <tr>
 * <td>{@code GET /saml/metadata}</td>
 * <td>{@code METADATA}</td>
 * <td>Signed SAML EntityDescriptor XML.</td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * The SAML {@code entityID}, SingleSignOnService URL, SingleLogoutService URL, and Metadata URL are separate SAML
 * deployment values. They must never be derived from an OAuth issuer or mounted on OAuth token, UserInfo, or discovery
 * routes.
 * </p>
 *
 * <h3>SCIM 2.0 service provider</h3>
 * <p>
 * SCIM uses an independent fixed {@code /scim/v2} resource namespace and typed resource capabilities from
 * {@link org.miaixz.bus.auth.protocol.scim.server.ScimServerScheme}. The project HTTP adapter owns JSON and HTTP status
 * conversion around the typed SCIM models.
 * </p>
 * <table>
 * <caption>Recommended SCIM 2.0 route groups</caption> <thead>
 * <tr>
 * <th scope="col">Route group</th>
 * <th scope="col">Capabilities</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@code /scim/v2/{resourceType}}</td>
 * <td>{@code CREATE}, {@code RETRIEVE}, {@code REPLACE}, {@code PATCH}, {@code DELETE}, {@code SEARCH_GET}</td>
 * </tr>
 * <tr>
 * <td>{@code POST /scim/v2/{resourceType}/.search}</td>
 * <td>{@code SEARCH_POST}</td>
 * </tr>
 * <tr>
 * <td>{@code POST /scim/v2/Bulk}</td>
 * <td>{@code BULK}</td>
 * </tr>
 * <tr>
 * <td>{@code GET /scim/v2/ServiceProviderConfig}</td>
 * <td>{@code SERVICE_PROVIDER_CONFIG}</td>
 * </tr>
 * <tr>
 * <td>{@code GET /scim/v2/ResourceTypes}</td>
 * <td>{@code RESOURCE_TYPES}</td>
 * </tr>
 * <tr>
 * <td>{@code GET /scim/v2/Schemas}</td>
 * <td>{@code SCHEMAS}</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <h3>LDAP and RADIUS listeners</h3>
 * <p>
 * LDAP and RADIUS do not expose HTTP paths. The project supplies protocol listeners and connection or datagram
 * lifecycle management. An LDAP adapter typically binds {@code ldap://host:389} and/or {@code ldaps://host:636},
 * decodes BER into a complete {@code LdapMessage}, selects the matching
 * {@link org.miaixz.bus.auth.protocol.ldap.server.LdapServerScheme} capability, and preserves one connection identifier
 * in Context. A RADIUS adapter binds its configured UDP or TLS Access and Accounting addresses, validates packet
 * framing, decodes the typed request, and invokes
 * {@link org.miaixz.bus.auth.protocol.radius.server.RadiusServerScheme#ACCESS} or
 * {@link org.miaixz.bus.auth.protocol.radius.server.RadiusServerScheme#ACCOUNTING}. Listener address, port, TLS, and
 * connection ownership remain project deployment concerns.
 * </p>
 * <p>
 * Client-role and Vendor Sources publish no inbound server address. They use Dispatcher for outbound authentication
 * capabilities and obtain remote endpoint addresses from their own Options or Vendor manifest.
 * </p>
 * <p>
 * Fixed routes are valid because route assembly already binds each route set to exactly one compatible Source.
 * Dispatcher deliberately does not inspect an unverified {@code client_id}, SAML issuer, LDAP bind name, or packet
 * attribute to guess across Sources, Namespaces, or protocols.
 * </p>
 *
 * <h2>Protocol-specific invocation forms</h2>
 * <p>
 * Every adapter creates a trusted non-secret {@link Context} and one decreasing {@link Timeout}. The Source reference
 * and capability come from protocol-specific route or listener configuration rather than arbitrary message fields. In
 * the example, {@code oidcReference} was resolved once when the project assembled its fixed OIDC routes; it is not
 * reconstructed from the request URL. OAuth and OIDC preserve the Fabric HTTP request and response directly:
 * </p>
 *
 * <pre>{@code
 * CompletionStage<Outcome<HttpResponse>> stage = dispatcher
 *         .invoke(oidcReference, OpenIdServerScheme.TOKEN, request, context, timeout);
 *
 * stage.thenAccept(outcome -> {
 *     switch (outcome) {
 *         case Outcome.Succeeded<HttpResponse> success -> write(success.value());
 *         case Outcome.Rejected<HttpResponse> rejected -> reject(rejected.failure());
 *         case Outcome.Failed<HttpResponse> failed -> fail(failed.failure());
 *     }
 * });
 * }</pre>
 * <p>
 * SAML crosses the Dispatcher boundary as typed protocol models rather than Fabric HTTP models:
 * </p>
 *
 * <pre>{@code
 *
 * AuthnRequest authnRequest = redirectBinding.decode(inbound, AuthnRequest.class).document().message();
 * CompletionStage<Outcome<Response>> stage = dispatcher
 *         .invoke(samlReference, SamlServerScheme.SINGLE_SIGN_ON, authnRequest, authenticatedContext, timeout);
 * }</pre>
 * <p>
 * SCIM similarly dispatches typed {@code Resource}, {@code PatchRequest}, {@code SearchQuery}, or discovery models.
 * LDAP dispatches a complete {@code LdapMessage}; RADIUS dispatches {@code AccessRequest} or {@code AccountingRequest}.
 * The caller must consult the selected Scheme constant for the exact generic request and success types instead of
 * assuming every server operation is {@code HttpRequest -> HttpResponse}.
 * </p>
 * <p>
 * These examples are adapter pseudocode: {@code write}, {@code reject}, {@code fail}, Binding decoding, Binding
 * encoding, JSON conversion, BER framing, and packet framing belong to the corresponding project transport integration.
 * Endpoint-level OAuth and OpenID errors have already been converted to a formal {@code HttpResponse}; other protocols
 * return their declared typed success value or a closed Outcome. Projects must never serialize {@link Outcome} or its
 * failure details as protocol wire content.
 * </p>
 *
 * <h2>Context and security requirements</h2>
 * <ul>
 * <li>The authorization route must first establish an authenticated project subject and place the matching
 * {@link Context.Authentication} facts in Context before invoking {@code AUTHENTICATION} or {@code AUTHORIZATION}.</li>
 * <li>OAuth and OIDC token, UserInfo, discovery, JWK Set, logout, introspection, and revocation requests retain their
 * wire credentials in {@code HttpRequest}; the corresponding bus-auth endpoint performs protocol authentication and
 * validation.</li>
 * <li>SAML, SCIM, LDAP, and RADIUS adapters must use only the typed request class declared by the selected Scheme
 * capability and must preserve Binding, connection, or packet context required by that protocol.</li>
 * <li>{@link Context.Network} must contain values observed by a trusted edge. Do not trust forwarded addresses or
 * headers unless the project has already applied its proxy policy.</li>
 * <li>{@link Context#clientId()} may contain only an identifier already verified by the external boundary. Do not copy
 * an unverified OAuth {@code client_id} parameter into it.</li>
 * <li>One {@link Timeout} covers the complete request. A route adapter must not reset the deadline between Dispatcher,
 * loader, cache, cryptographic, and transport operations.</li>
 * </ul>
 *
 * <h2>Endpoint URL consistency</h2>
 * <p>
 * The public routes registered by the project must exactly match the server endpoints configured in the Source options,
 * because discovery and authorization-server metadata publish those configured endpoint values. Dispatcher executes
 * capabilities but does not register routes or reconcile configured URLs with project routes. The project must
 * therefore derive both route registration and Source endpoint configuration from one deployment-owned endpoint
 * definition until a dedicated endpoint publication integration is provided.
 * </p>
 * <p>
 * This contract owns capability lookup, boundary validation, and invocation only. It does not load or mutate
 * registrations, configure public URLs, persist protocol or business data, authenticate project users, bind accounts,
 * create business sessions, apply project permissions, or emit project audit events.
 * </p>
 *
 * @author Kimi Liu
 */
public interface Dispatcher {

    /**
     * Reports whether the current runtime container contains a compiled worker for a Source reference.
     * <p>
     * A project route assembler may use this method as a fast preflight after resolving its deployment configuration to
     * a Source reference. A {@code true} result means that the Source exists in the current compiled revision; it does
     * not guarantee that a particular capability is enabled and is not an HTTP health check.
     * </p>
     *
     * @param reference Source reference
     * @return {@code true} when the reference is currently invocable
     */
    boolean available(Registry.Reference reference);

    /**
     * Returns the capabilities exposed by a Source in the current runtime container.
     * <p>
     * The manifest allows management or route-assembly code to determine whether the selected Source exposes token,
     * discovery, UserInfo, logout, or another operation. It contains capability declarations, not public URLs or
     * mutable route state. Request-time routing should still use a fixed project allow-list rather than accepting a
     * capability key directly from an untrusted request.
     * </p>
     *
     * @param reference Source reference
     * @return immutable manifest, or empty when the Source is unavailable
     */
    Optional<Capability.Manifest> manifest(Registry.Reference reference);

    /**
     * Executes one capability declared by the referenced Source.
     * <p>
     * Request and success types are defined by the selected capability. OAuth and OIDC server endpoints use Fabric
     * {@code HttpRequest} and {@code HttpResponse}; SAML, SCIM, LDAP, and RADIUS use their declared typed protocol
     * models. The caller must preserve the transport information required by that protocol, provide the exact Source
     * reference selected by its route or listener, select a constant from the matching server Scheme, supply trusted
     * Context data, and propagate one existing Timeout. Dispatcher validates lifecycle, reference, manifest, request
     * type, and declared security before delegating to the compiled Source worker.
     * </p>
     * <p>
     * Ordinary protocol errors are returned by the endpoint as a successful {@code HttpResponse} containing the
     * standard wire error. {@link Outcome.Rejected} represents an expected failure at the framework invocation
     * boundary, while {@link Outcome.Failed} represents an operational failure. The returned stage may still complete
     * exceptionally for cancellation or an unrecoverable caller/programming failure.
     * </p>
     *
     * @param reference  registered Source reference
     * @param capability strongly typed requested capability
     * @param request    request matching the capability request type
     * @param context    immutable invocation context
     * @param timeout    shared decreasing operation timeout
     * @param <Q>        request type
     * @param <S>        success type
     * @return asynchronous protocol-neutral outcome
     */
    <Q, S> CompletionStage<Outcome<S>> invoke(
            Registry.Reference reference,
            Capability<Q, S> capability,
            Q request,
            Context context,
            Timeout timeout);

}

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
package org.miaixz.bus.auth.protocol.ldap.server;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.ldap.*;

/**
 * Defines the external project port for LDAPv3 connection state, directory access, and atomic mutations.
 * <p>
 * Implementations own all directory data and maintain Bind authentication and operation state under the exact
 * {@code providerId + connectionId} pair supplied by the framework. They enforce directory authorization, schema,
 * matching rules, alias behavior, atomic modifications, and Abandon correlation. Bus-auth retains the standard wire
 * models and applies provider limits but does not supply an in-memory directory, hidden session map, or persistence
 * implementation.
 * </p>
 *
 * @author Kimi Liu
 */
public interface DirectoryStore {

    /**
     * Establishes or replaces the authentication state of one LDAP connection.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Bind request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing the standard Bind response or a closed Bus failure
     */
    CompletionStage<Outcome<BindResponse>> bind(
            String providerId,
            String connectionId,
            BindRequest request,
            Context context,
            Timeout timeout);

    /**
     * Releases connection-level authentication and directory operation state without producing a protocol response.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Unbind request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing empty success or a closed Bus failure; no LDAP response may be generated
     */
    CompletionStage<Outcome<Void>> unbind(
            String providerId,
            String connectionId,
            UnbindRequest request,
            Context context,
            Timeout timeout);

    /**
     * Searches the directory under the current connection authentication state.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Search request already tightened to Provider limits
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing Entry/Reference operations followed by exactly one SearchResultDone, or a closed failure
     */
    CompletionStage<Outcome<List<LdapMessage.ProtocolOp>>> search(
            String providerId,
            String connectionId,
            SearchRequest request,
            Context context,
            Timeout timeout);

    /**
     * Compares one assertion with an entry under the current connection authorization state.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Compare request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing compareTrue, compareFalse, or another standard Compare result
     */
    CompletionStage<Outcome<CompareResponse>> compare(
            String providerId,
            String connectionId,
            CompareRequest request,
            Context context,
            Timeout timeout);

    /**
     * Applies one ordered ModifyRequest atomically to an existing directory entry.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      complete ordered standard Modify request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing the standard Modify response or a closed Bus failure
     */
    CompletionStage<Outcome<ModifyResponse>> modify(
            String providerId,
            String connectionId,
            ModifyRequest request,
            Context context,
            Timeout timeout);

    /**
     * Adds one complete entry atomically after schema and authorization validation.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Add request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing the standard Add response or a closed Bus failure
     */
    CompletionStage<Outcome<AddResponse>> add(
            String providerId,
            String connectionId,
            AddRequest request,
            Context context,
            Timeout timeout);

    /**
     * Deletes one entry under the current connection authorization state.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Delete request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing the standard Delete response or a closed Bus failure
     */
    CompletionStage<Outcome<DeleteResponse>> delete(
            String providerId,
            String connectionId,
            DeleteRequest request,
            Context context,
            Timeout timeout);

    /**
     * Atomically renames or moves one entry according to the complete ModifyDN request.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Modify DN request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing the standard Modify DN response or a closed Bus failure
     */
    CompletionStage<Outcome<ModifyDNResponse>> modifyDN(
            String providerId,
            String connectionId,
            ModifyDNRequest request,
            Context context,
            Timeout timeout);

    /**
     * Requests cancellation of the operation identified by the Abandon target message identifier.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Abandon request containing the target message identifier
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing empty success or a closed Bus failure; no LDAP response may be generated
     */
    CompletionStage<Outcome<Void>> abandon(
            String providerId,
            String connectionId,
            AbandonRequest request,
            Context context,
            Timeout timeout);

    /**
     * Executes an externally implemented extended operation other than framework-handled StartTLS.
     *
     * @param providerId   owning LDAP Provider identifier
     * @param connectionId stable trusted transport connection identifier
     * @param request      standard Extended request
     * @param context      immutable invocation context carrying the same connection snapshot
     * @param timeout      shared end-to-end timeout
     * @return stage containing the standard Extended response or a closed Bus failure
     */
    CompletionStage<Outcome<ExtendedResponse>> extended(
            String providerId,
            String connectionId,
            ExtendedRequest request,
            Context context,
            Timeout timeout);

}

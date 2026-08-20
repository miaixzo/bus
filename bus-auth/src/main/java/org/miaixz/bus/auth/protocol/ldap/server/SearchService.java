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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.ldap.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Applies Provider limits and validates the complete RFC 4511 Search response sequence.
 *
 * @author Kimi Liu
 */
public final class SearchService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String providerId;

    /**
     * Frozen Provider search limits.
     */
    private final LdapServerOptions options;

    /**
     * External connection-state and directory implementation.
     */
    private final DirectoryStore store;

    /**
     * Creates a Search service for one compiled LDAP Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param options    validated LDAP Provider options
     * @param store      externally implemented directory store
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public SearchService(final String providerId, final LdapServerOptions options, final DirectoryStore store) {
        this.providerId = Assert.notBlank(providerId, "LDAP Search Provider id must not be blank");
        this.options = Assert.notNull(options, "LDAP Search Provider options must not be null");
        this.store = Assert.notNull(store, "LDAP Search directory store must not be null");
    }

    /**
     * Validates and correlates one external Search outcome.
     *
     * @param messageId request message identifier
     * @param sizeLimit effective maximum entry count
     * @param outcome   external store outcome
     * @param thrown    asynchronous store failure
     * @return complete validated Search outcome
     */
    private static Outcome<List<LdapMessage>> map(
            final int messageId,
            final int sizeLimit,
            final Outcome<List<LdapMessage.ProtocolOp>> outcome,
            final Throwable thrown) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(failure("LDAP Search directory store failed"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<List<LdapMessage.ProtocolOp>> succeeded -> messages(
                    messageId,
                    sizeLimit,
                    succeeded.value());
            case Outcome.Rejected<List<LdapMessage.ProtocolOp>> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<List<LdapMessage.ProtocolOp>> failed -> Outcome.failed(failed.failure());
        };
    }

    /**
     * Enforces Entry/Reference/Done ordering and converts operations to complete correlated messages.
     *
     * @param messageId  request message identifier
     * @param sizeLimit  effective maximum entry count
     * @param operations external store protocol operations
     * @return validated successful outcome or a closed store-contract failure
     */
    private static Outcome<List<LdapMessage>> messages(
            final int messageId,
            final int sizeLimit,
            final List<LdapMessage.ProtocolOp> operations) {
        if (operations == null || operations.isEmpty()) {
            return Outcome.failed(failure("LDAP Search directory store returned no completion"));
        }
        final List<LdapMessage> messages = new ArrayList<>(operations.size());
        int entries = 0;
        boolean done = false;
        for (LdapMessage.ProtocolOp operation : operations) {
            if (operation == null || done || !(operation instanceof SearchResultEntry
                    || operation instanceof SearchResultReference || operation instanceof SearchResultDone)) {
                return Outcome.failed(failure("LDAP Search directory store returned an invalid sequence"));
            }
            if (operation instanceof SearchResultEntry) {
                entries++;
                if (entries > sizeLimit) {
                    return Outcome.failed(failure("LDAP Search directory store exceeded the entry limit"));
                }
            }
            done = operation instanceof SearchResultDone;
            messages.add(new LdapMessage(messageId, operation, List.of()));
        }
        return done ? Outcome.succeeded(List.copyOf(messages))
                : Outcome.failed(failure("LDAP Search directory store omitted SearchResultDone"));
    }

    /**
     * Creates a single-message local SearchResultDone response.
     *
     * @param messageId  request message identifier
     * @param code       standard LDAP result code
     * @param diagnostic fixed non-sensitive diagnostic
     * @return successful outcome containing one terminal Search message
     */
    private static Outcome<List<LdapMessage>> local(
            final int messageId,
            final LdapResultCode code,
            final String diagnostic) {
        final LdapResult result = new LdapResult(code, new DistinguishedName(Normal.EMPTY), diagnostic,
                org.miaixz.bus.core.lang.Optional.empty());
        final LdapMessage response = new LdapMessage(Math.max(0, messageId), new SearchResultDone(result), List.of());
        return Outcome.succeeded(List.of(response));
    }

    /**
     * Creates a safe external-store or timeout failure.
     *
     * @param description fixed non-sensitive description
     * @return closed operational failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._503, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates an already completed stage.
     *
     * @param outcome completed value
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Executes one Search and returns Entry/Reference messages followed by exactly one SearchResultDone.
     *
     * @param message complete Search request message
     * @param context immutable invocation context with a trusted connection snapshot
     * @param timeout shared end-to-end time budget
     * @return stage containing the immutable complete Search message sequence or a closed failure
     */
    public CompletionStage<Outcome<List<LdapMessage>>> search(
            final LdapMessage message,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(message, "LDAP Search message must not be null");
        Assert.notNull(context, "LDAP Search context must not be null");
        Assert.notNull(timeout, "LDAP Search time budget must not be null");
        if (!(message.protocolOp() instanceof SearchRequest request) || message.messageId() <= 0) {
            return completed(
                    local(message.messageId(), LdapResultCode.PROTOCOL_ERROR, "The LDAP Search request is malformed."));
        }
        if (message.controls().stream().anyMatch(control -> control.criticality())) {
            return completed(
                    local(
                            message.messageId(),
                            LdapResultCode.UNAVAILABLE_CRITICAL_EXTENSION,
                            "The LDAP Search control is not supported."));
        }
        Assert.isTrue(context.network().connection().isPresent(), "LDAP Search requires a trusted connection snapshot");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("LDAP Search time budget expired")));
        }

        final SearchRequest bounded = bounded(request);
        final CompletionStage<Outcome<List<LdapMessage.ProtocolOp>>> stage;
        try {
            stage = store
                    .search(providerId, context.network().connection().getOrThrow().id(), bounded, context, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure("LDAP Search directory store failed")));
        }
        if (stage == null) {
            return completed(Outcome.failed(failure("LDAP Search directory store returned no stage")));
        }
        return stage.handle((outcome, thrown) -> map(message.messageId(), bounded.sizeLimit(), outcome, thrown));
    }

    /**
     * Rebuilds the standard Search request with Provider search limits applied.
     *
     * @param request decoded Search request
     * @return request whose size and time limits cannot exceed Provider options
     */
    private SearchRequest bounded(final SearchRequest request) {
        final int sizeLimit = request.sizeLimit() == 0 ? options.maximumSearchEntries()
                : Math.min(request.sizeLimit(), options.maximumSearchEntries());
        final int timeLimit = request.timeLimit() == 0 ? options.maximumSearchTimeSeconds()
                : Math.min(request.timeLimit(), options.maximumSearchTimeSeconds());
        return new SearchRequest(request.baseObject(), request.scope(), request.derefAliases(), sizeLimit, timeLimit,
                request.typesOnly(), request.filter(), request.attributes());
    }

}

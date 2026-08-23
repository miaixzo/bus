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
package org.miaixz.bus.auth.source.protocol.radius.server;

import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.radius.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Defines the external client-data, access-decision, and accounting port for one server-role RADIUS Source.
 * <p>
 * Bus-auth validates the wire packet and hop-by-hop security before invoking Access or Accounting. The external
 * implementation owns client lookup, user authentication data, authorization policy, and durable accounting data; it
 * performs no packet cryptography, network I/O, or reverse Roster lookup through this contract.
 * </p>
 *
 * @author Kimi Liu
 */
public interface RadiusRequestHandler {

    /**
     * Resolves the RADIUS client bound to the trusted transport source and complete request.
     *
     * @param sourceId      exact current server-role Source identifier
     * @param remoteAddress trusted remote address observed by the transport boundary
     * @param request       complete Access or Accounting packet
     * @param context       immutable non-secret invocation context
     * @param timeout       shared end-to-end operation timeout
     * @return stage containing the resolved client or a silent-discard failure
     */
    CompletionStage<Outcome<Client>> resolve(
            String sourceId,
            String remoteAddress,
            RadiusPacket request,
            Context context,
            Timeout timeout);

    /**
     * Processes one wire-validated Access-Request.
     * <p>
     * An authentication or authorization denial is a successful {@code AccessReject} packet, not an exceptional stage
     * or failed Outcome. The framework validates, correlates, and authenticates the returned packet before transport.
     * </p>
     *
     * @param sourceId exact current server-role Source identifier
     * @param client   resolved RADIUS client binding
     * @param request  validated standard Access-Request
     * @param context  immutable invocation context carrying verified client identity only after resolution
     * @param timeout  shared end-to-end operation timeout
     * @return stage containing Access-Accept, Access-Reject, or Access-Challenge as a standard packet
     */
    CompletionStage<Outcome<RadiusPacket>> access(
            String sourceId,
            Client client,
            AccessRequest request,
            Context context,
            Timeout timeout);

    /**
     * Durably processes one wire-validated Accounting-Request.
     * <p>
     * If the external implementation cannot successfully record the request, it returns a failed Outcome so the
     * framework emits no Accounting-Response, as required by RFC 2866.
     * </p>
     *
     * @param sourceId exact current server-role Source identifier
     * @param client   resolved RADIUS client binding
     * @param request  validated standard Accounting-Request
     * @param context  immutable invocation context carrying verified client identity only after resolution
     * @param timeout  shared end-to-end operation timeout
     * @return stage containing the standard Accounting-Response after durable processing
     */
    CompletionStage<Outcome<AccountingResponse>> accounting(
            String sourceId,
            Client client,
            AccountingRequest request,
            Context context,
            Timeout timeout);

    /**
     * Carries one resolved RADIUS client and its hop-by-hop protocol policy.
     *
     * @param id           stable external RADIUS client identifier
     * @param sharedSecret optional historic RADIUS shared-secret reference; absent for RADIUS/1.1
     * @param allowedCodes non-empty allowed request Codes
     * @author Kimi Liu
     */
    record Client(String id, Optional<Credential.Reference> sharedSecret, Set<RadiusCode> allowedCodes) {

        /**
         * Validates and freezes a resolved client binding.
         *
         * @param id           non-blank external client identifier
         * @param sharedSecret optional SHARED_SECRET reference
         * @param allowedCodes non-empty subset of Access-Request and Accounting-Request
         * @throws IllegalArgumentException if any component violates the client boundary
         */
        public Client {
            Assert.notBlank(id, "RADIUS client id must not be blank");
            Assert.notNull(sharedSecret, "RADIUS client shared-secret container must not be null");
            final Credential.Reference reference = sharedSecret.getOrNull();
            if (reference != null) {
                Assert.isTrue(
                        reference.type() == Credential.Type.SHARED_SECRET,
                        "RADIUS client credential must reference a shared secret");
            }
            sharedSecret = Optional.ofNullable(reference);
            Assert.notNull(allowedCodes, "RADIUS client allowed Codes must not be null");
            Assert.notEmpty(allowedCodes, "RADIUS client allowed Codes must not be empty");
            allowedCodes = Set.copyOf(allowedCodes);
            for (RadiusCode code : allowedCodes) {
                Assert.notNull(code, "RADIUS client allowed Code must not be null");
                Assert.isTrue(
                        code.equals(new RadiusCode(Radius.Codes.ACCESS_REQUEST))
                                || code.equals(new RadiusCode(Radius.Codes.ACCOUNTING_REQUEST)),
                        "RADIUS client may allow only Access-Request or Accounting-Request");
            }
        }

        /**
         * Returns safe diagnostics without exposing the credential reference identifier.
         *
         * @return client id, shared-secret presence, and allowed Codes
         */
        @Override
        public String toString() {
            return "Client[id=" + id + ", sharedSecretPresent=" + sharedSecret.isPresent() + ", allowedCodes="
                    + allowedCodes + Symbol.BRACKET_RIGHT;
        }

    }

}

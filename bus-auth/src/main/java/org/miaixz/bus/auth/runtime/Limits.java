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
package org.miaixz.bus.auth.runtime;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Policy;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;

/**
 * Closed parser and allocation limits shared by authentication protocols.
 *
 * @param maxHeaderBytes        maximum decoded authentication header bytes
 * @param maxParameters         maximum decoded parameter count
 * @param maxParameterBytes     maximum UTF-8 bytes per decoded parameter
 * @param maxJsonBytes          maximum JSON document bytes
 * @param maxJsonDepth          maximum JSON nesting depth
 * @param maxJwtBytes           maximum compact JWT bytes
 * @param maxLdapMessageBytes   maximum LDAP message bytes
 * @param maxLdapDepth          maximum LDAP BER nesting depth
 * @param maxScimBulkBytes      maximum SCIM bulk document bytes
 * @param maxScimBulkOperations maximum SCIM bulk operation count
 * @param maxRadiusPacketBytes  maximum RADIUS packet bytes
 * @param maxSsfSetBytes        maximum SSF SET bytes
 * @author Kimi Liu
 */
public record Limits(int maxHeaderBytes, int maxParameters, int maxParameterBytes, int maxJsonBytes, int maxJsonDepth,
        int maxJwtBytes, int maxLdapMessageBytes, int maxLdapDepth, int maxScimBulkBytes, int maxScimBulkOperations,
        int maxRadiusPacketBytes, int maxSsfSetBytes) implements Policy {

    /**
     * Shared immutable strict policy.
     */
    private static final Limits STRICT = new Limits(32 * Normal._1024, 128, 8 * Normal._1024, (int) Normal.MEBI, 32,
            16 * Normal._1024, 2 * (int) Normal.MEBI, 32, (int) Normal.MEBI, 1000, 4096, 64 * Normal._1024);

    /**
     * Validates that every closed parser and allocation bound is positive.
     *
     * @throws ValidateException if any bound is zero or negative
     */
    public Limits {
        if (maxHeaderBytes < 1 || maxParameters < 1 || maxParameterBytes < 1 || maxJsonBytes < 1 || maxJsonDepth < 1
                || maxJwtBytes < 1 || maxLdapMessageBytes < 1 || maxLdapDepth < 1 || maxScimBulkBytes < 1
                || maxScimBulkOperations < 1 || maxRadiusPacketBytes < 1 || maxSsfSetBytes < 1) {
            throw new ValidateException("Authentication limits must be positive");
        }
    }

    /**
     * Returns the shared strict authentication limit policy.
     *
     * @return immutable strict limits
     */
    public static Limits strict() {
        return STRICT;
    }

    /**
     * Writes every limit into its matching typed authentication option.
     *
     * @param options non-null source options
     * @return immutable options containing all twelve limits
     */
    @Override
    public Options from(final Options options) {
        return options.with(Builder.OPTION_MAX_HEADER_BYTES, maxHeaderBytes)
                .with(Builder.OPTION_MAX_PARAMETERS, maxParameters)
                .with(Builder.OPTION_MAX_PARAMETER_BYTES, maxParameterBytes)
                .with(Builder.OPTION_MAX_JSON_BYTES, maxJsonBytes).with(Builder.OPTION_MAX_JSON_DEPTH, maxJsonDepth)
                .with(Builder.OPTION_MAX_TOKEN_BYTES, maxJwtBytes)
                .with(Builder.OPTION_MAX_LDAP_MESSAGE_BYTES, maxLdapMessageBytes)
                .with(Builder.OPTION_MAX_LDAP_DEPTH, maxLdapDepth)
                .with(Builder.OPTION_MAX_SCIM_BULK_BYTES, maxScimBulkBytes)
                .with(Builder.OPTION_MAX_SCIM_BULK_OPERATIONS, maxScimBulkOperations)
                .with(Builder.OPTION_MAX_RADIUS_PACKET_BYTES, maxRadiusPacketBytes)
                .with(Builder.OPTION_MAX_SSF_SET_BYTES, maxSsfSetBytes);
    }

}

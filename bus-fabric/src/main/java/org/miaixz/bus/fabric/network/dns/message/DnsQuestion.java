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
package org.miaixz.bus.fabric.network.dns.message;

import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * Immutable DNS question section entry.
 *
 * @author Kimi Liu
 */
public final class DnsQuestion {

    /**
     * Queried owner name.
     */
    private final String name;

    /**
     * Numeric query type code.
     */
    private final int typeCode;

    /**
     * Numeric query class code.
     */
    private final int recordClass;

    /**
     * Creates a DNS question.
     *
     * @param name        queried owner name
     * @param typeCode    unsigned 16-bit query type code
     * @param recordClass unsigned 16-bit query class code
     */
    public DnsQuestion(final String name, final int typeCode, final int recordClass) {
        this.name = DnsName.normalize(name);
        this.typeCode = DnsCodec.validateUnsignedShort(typeCode, "DNS question type");
        this.recordClass = DnsCodec.validateUnsignedShort(recordClass, "DNS question class");
    }

    /**
     * Returns the queried owner name.
     *
     * @return canonical DNS name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the known query type.
     *
     * @return known type constant, or {@link DnsRecordType#UNKNOWN}
     */
    public DnsRecordType type() {
        return DnsRecordType.fromCode(typeCode);
    }

    /**
     * Returns the numeric query type code.
     *
     * @return unsigned 16-bit query type code
     */
    public int typeCode() {
        return typeCode;
    }

    /**
     * Returns the numeric query class code.
     *
     * @return unsigned 16-bit query class code
     */
    public int recordClass() {
        return recordClass;
    }

    /**
     * Returns whether this question targets the Internet class.
     *
     * @return true when the class equals IN
     */
    public boolean internetClass() {
        return recordClass == DnsRecord.CLASS_IN;
    }

}

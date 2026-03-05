/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.mac.jna;

import org.miaixz.bus.health.Builder;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.NativeLongByReference;

/**
 * The I/O Kit framework implements non-kernel access to I/O Kit objects (drivers and nubs) through the device-interface
 * mechanism.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface IOKit extends com.sun.jna.platform.mac.IOKit {

    /**
     * Singleton instance of the IOKit library.
     */
    IOKit INSTANCE = Native.load("IOKit", IOKit.class);

    /**
     * Beta/Non-API do not commit to JNA. Calls a structured method on an I/O Kit connection.
     *
     * @param connection          The I/O Kit connection.
     * @param selector            The selector for the method to call.
     * @param inputStructure      The input structure for the method.
     * @param structureInputSize  The size of the input structure.
     * @param outputStructure     The output structure for the method.
     * @param structureOutputSize A pointer to the size of the output structure.
     * @return An integer result code.
     */
    int IOConnectCallStructMethod(
            IOConnect connection,
            int selector,
            Structure inputStructure,
            NativeLong structureInputSize,
            Structure outputStructure,
            NativeLongByReference structureOutputSize);

    /**
     * JNA wrapper for the SMCKeyDataVers structure.
     * <p>
     * This class maps to the native macOS SMC version data structure which holds the return value of SMC version query.
     * </p>
     */
    @FieldOrder({ "major", "minor", "build", "reserved", "release" })
    class SMCKeyDataVers extends Structure {

        /**
         * Major version number.
         */
        public byte major;
        /**
         * Minor version number.
         */
        public byte minor;
        /**
         * Build number.
         */
        public byte build;
        /**
         * Reserved byte.
         */
        public byte reserved;
        /**
         * Release version.
         */
        public short release;
    }

    /**
     * JNA wrapper for the SMCKeyDataPLimitData structure.
     * <p>
     * This class maps to the native macOS SMC power limit data structure which holds the return value of SMC pLimit
     * query.
     * </p>
     */
    @FieldOrder({ "version", "length", "cpuPLimit", "gpuPLimit", "memPLimit" })
    class SMCKeyDataPLimitData extends Structure {

        /**
         * Version of the power limit data structure.
         */
        public short version;
        /**
         * Length of the power limit data structure.
         */
        public short length;
        /**
         * CPU power limit.
         */
        public int cpuPLimit;
        /**
         * GPU power limit.
         */
        public int gpuPLimit;
        /**
         * Memory power limit.
         */
        public int memPLimit;
    }

    /**
     * JNA wrapper for the SMCKeyDataKeyInfo structure.
     * <p>
     * This class maps to the native macOS SMC key information structure which holds the return value of SMC KeyInfo
     * query.
     * </p>
     */
    @FieldOrder({ "dataSize", "dataType", "dataAttributes" })
    class SMCKeyDataKeyInfo extends Structure {

        /**
         * Size of the data.
         */
        public int dataSize;
        /**
         * Type of the data.
         */
        public int dataType;
        /**
         * Attributes of the data.
         */
        public byte dataAttributes;
    }

    /**
     * JNA wrapper for the SMCKeyData structure.
     * <p>
     * This class maps to the native macOS SMC data structure which holds the return value of SMC query.
     * </p>
     */
    @FieldOrder({ "key", "vers", "pLimitData", "keyInfo", "result", "status", "data8", "data32", "bytes" })
    class SMCKeyData extends Structure implements AutoCloseable {

        /**
         * The SMC key.
         */
        public int key;
        /**
         * SMC version data.
         */
        public SMCKeyDataVers vers;
        /**
         * SMC power limit data.
         */
        public SMCKeyDataPLimitData pLimitData;
        /**
         * SMC key information.
         */
        public SMCKeyDataKeyInfo keyInfo;
        /**
         * Result code.
         */
        public byte result;
        /**
         * Status code.
         */
        public byte status;
        /**
         * 8-bit data.
         */
        public byte data8;
        /**
         * 32-bit data.
         */
        public int data32;
        /**
         * Raw bytes of data.
         */
        public byte[] bytes = new byte[32];

        /**
         * Closes the memory associated with this structure.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * JNA wrapper for the SMCVal structure.
     * <p>
     * This class maps to the native macOS SMC value structure which holds SMC key-value data.
     * </p>
     */
    @FieldOrder({ "key", "dataSize", "dataType", "bytes" })
    class SMCVal extends Structure implements AutoCloseable {

        /**
         * The SMC key.
         */
        public byte[] key = new byte[5];
        /**
         * Size of the data.
         */
        public int dataSize;
        /**
         * Type of the data.
         */
        public byte[] dataType = new byte[5];
        /**
         * Raw bytes of data.
         */
        public byte[] bytes = new byte[32];

        /**
         * Closes the memory associated with this structure.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

}

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
package org.miaixz.bus.health.windows.jna;

import com.sun.jna.Native;

/**
 * Kernel32. This class should be considered non-API as it may be removed if/when its code is incorporated into the JNA
 * project.
 *
 * @author Kimi Liu
 */
public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {

    /**
     * Constant <code>INSTANCE</code>
     */
    Kernel32 INSTANCE = Native.load("Kernel32", Kernel32.class);

    /**
     * The ProcessorFeature enum.
     *
     * @author Kimi Liu
     */
    enum ProcessorFeature {

        /**
         * The floating point precision errata processor feature.
         */
        PF_FLOATING_POINT_PRECISION_ERRATA(0),
        /**
         * The floating point emulated processor feature.
         */
        PF_FLOATING_POINT_EMULATED(1),
        /**
         * The compare exchange double processor feature.
         */
        PF_COMPARE_EXCHANGE_DOUBLE(2),
        /**
         * The mmx instructions available processor feature.
         */
        PF_MMX_INSTRUCTIONS_AVAILABLE(3),
        /**
         * The ppc movemem 64 bit ok processor feature.
         */
        PF_PPC_MOVEMEM_64BIT_OK(4),
        /**
         * The alpha byte instructions processor feature.
         */
        PF_ALPHA_BYTE_INSTRUCTIONS(5),
        /**
         * The xmmi instructions available processor feature.
         */
        PF_XMMI_INSTRUCTIONS_AVAILABLE(6),
        /**
         * The 3dnow instructions available processor feature.
         */
        PF_3DNOW_INSTRUCTIONS_AVAILABLE(7),
        /**
         * The rdtsc instruction available processor feature.
         */
        PF_RDTSC_INSTRUCTION_AVAILABLE(8),
        /**
         * The pae enabled processor feature.
         */
        PF_PAE_ENABLED(9),
        /**
         * The xmmi64 instructions available processor feature.
         */
        PF_XMMI64_INSTRUCTIONS_AVAILABLE(10),
        /**
         * The sse daz mode available processor feature.
         */
        PF_SSE_DAZ_MODE_AVAILABLE(11),
        /**
         * The nx enabled processor feature.
         */
        PF_NX_ENABLED(12),
        /**
         * The sse3 instructions available processor feature.
         */
        PF_SSE3_INSTRUCTIONS_AVAILABLE(13),
        /**
         * The compare exchange128 processor feature.
         */
        PF_COMPARE_EXCHANGE128(14),
        /**
         * The compare64 exchange128 processor feature.
         */
        PF_COMPARE64_EXCHANGE128(15),
        /**
         * The channels enabled processor feature.
         */
        PF_CHANNELS_ENABLED(16),
        /**
         * The xsave enabled processor feature.
         */
        PF_XSAVE_ENABLED(17),
        /**
         * The arm vfp 32 registers available processor feature.
         */
        PF_ARM_VFP_32_REGISTERS_AVAILABLE(18),
        /**
         * The arm neon instructions available processor feature.
         */
        PF_ARM_NEON_INSTRUCTIONS_AVAILABLE(19),
        /**
         * The second level address translation processor feature.
         */
        PF_SECOND_LEVEL_ADDRESS_TRANSLATION(20),
        /**
         * The virt firmware enabled processor feature.
         */
        PF_VIRT_FIRMWARE_ENABLED(21),
        /**
         * The rdwrfsgsbase available processor feature.
         */
        PF_RDWRFSGSBASE_AVAILABLE(22),
        /**
         * The fastfail available processor feature.
         */
        PF_FASTFAIL_AVAILABLE(23),
        /**
         * The arm divide instruction available processor feature.
         */
        PF_ARM_DIVIDE_INSTRUCTION_AVAILABLE(24),
        /**
         * The arm 64 bit loadstore atomic processor feature.
         */
        PF_ARM_64BIT_LOADSTORE_ATOMIC(25),
        /**
         * The arm external cache available processor feature.
         */
        PF_ARM_EXTERNAL_CACHE_AVAILABLE(26),
        /**
         * The arm fmac instructions available processor feature.
         */
        PF_ARM_FMAC_INSTRUCTIONS_AVAILABLE(27),
        /**
         * The rdrand instruction available processor feature.
         */
        PF_RDRAND_INSTRUCTION_AVAILABLE(28),
        /**
         * The arm v8 instructions available processor feature.
         */
        PF_ARM_V8_INSTRUCTIONS_AVAILABLE(29),
        /**
         * The arm v8 crypto instructions available processor feature.
         */
        PF_ARM_V8_CRYPTO_INSTRUCTIONS_AVAILABLE(30),
        /**
         * The arm v8 crc32 instructions available processor feature.
         */
        PF_ARM_V8_CRC32_INSTRUCTIONS_AVAILABLE(31),
        /**
         * The rdtscp instruction available processor feature.
         */
        PF_RDTSCP_INSTRUCTION_AVAILABLE(32),
        /**
         * The rdpid instruction available processor feature.
         */
        PF_RDPID_INSTRUCTION_AVAILABLE(33),
        /**
         * The arm v81 atomic instructions available processor feature.
         */
        PF_ARM_V81_ATOMIC_INSTRUCTIONS_AVAILABLE(34),
        /**
         * The monitorx instruction available processor feature.
         */
        PF_MONITORX_INSTRUCTION_AVAILABLE(35),
        /**
         * The ssse3 instructions available processor feature.
         */
        PF_SSSE3_INSTRUCTIONS_AVAILABLE(36),
        /**
         * The sse4 1 instructions available processor feature.
         */
        PF_SSE4_1_INSTRUCTIONS_AVAILABLE(37),
        /**
         * The sse4 2 instructions available processor feature.
         */
        PF_SSE4_2_INSTRUCTIONS_AVAILABLE(38),
        /**
         * The avx instructions available processor feature.
         */
        PF_AVX_INSTRUCTIONS_AVAILABLE(39),
        /**
         * The avx2 instructions available processor feature.
         */
        PF_AVX2_INSTRUCTIONS_AVAILABLE(40),
        /**
         * The avx512f instructions available processor feature.
         */
        PF_AVX512F_INSTRUCTIONS_AVAILABLE(41),
        /**
         * The enhanced rep movsb and stosb available processor feature.
         */
        PF_ERMS_AVAILABLE(42),
        /**
         * The arm v82 dp instructions available processor feature.
         */
        PF_ARM_V82_DP_INSTRUCTIONS_AVAILABLE(43),
        /**
         * The arm v83 jscvt instructions available processor feature.
         */
        PF_ARM_V83_JSCVT_INSTRUCTIONS_AVAILABLE(44),
        /**
         * The arm v83 lrcpc instructions available processor feature.
         */
        PF_ARM_V83_LRCPC_INSTRUCTIONS_AVAILABLE(45),
        /**
         * The arm sve instructions available processor feature.
         */
        PF_ARM_SVE_INSTRUCTIONS_AVAILABLE(46),
        /**
         * The arm sve2 instructions available processor feature.
         */
        PF_ARM_SVE2_INSTRUCTIONS_AVAILABLE(47),
        /**
         * The arm sve2.1 instructions available processor feature.
         */
        PF_ARM_SVE2_1_INSTRUCTIONS_AVAILABLE(48),
        /**
         * The arm sve aes instructions available processor feature.
         */
        PF_ARM_SVE_AES_INSTRUCTIONS_AVAILABLE(49),
        /**
         * The arm sve pmull128 instructions available processor feature.
         */
        PF_ARM_SVE_PMULL128_INSTRUCTIONS_AVAILABLE(50),
        /**
         * The arm sve bit permutation instructions available processor feature.
         */
        PF_ARM_SVE_BITPERM_INSTRUCTIONS_AVAILABLE(51),
        /**
         * The arm sve bfloat16 instructions available processor feature.
         */
        PF_ARM_SVE_BF16_INSTRUCTIONS_AVAILABLE(52),
        /**
         * The arm sve enhanced bfloat16 instructions available processor feature.
         */
        PF_ARM_SVE_EBF16_INSTRUCTIONS_AVAILABLE(53),
        /**
         * The arm sve b16b16 instructions available processor feature.
         */
        PF_ARM_SVE_B16B16_INSTRUCTIONS_AVAILABLE(54),
        /**
         * The arm sve sha3 instructions available processor feature.
         */
        PF_ARM_SVE_SHA3_INSTRUCTIONS_AVAILABLE(55),
        /**
         * The arm sve sm4 instructions available processor feature.
         */
        PF_ARM_SVE_SM4_INSTRUCTIONS_AVAILABLE(56),
        /**
         * The arm sve int8 matrix multiplication instructions available processor feature.
         */
        PF_ARM_SVE_I8MM_INSTRUCTIONS_AVAILABLE(57),
        /**
         * The arm sve float32 matrix multiplication instructions available processor feature.
         */
        PF_ARM_SVE_F32MM_INSTRUCTIONS_AVAILABLE(58),
        /**
         * The arm sve float64 matrix multiplication instructions available processor feature.
         */
        PF_ARM_SVE_F64MM_INSTRUCTIONS_AVAILABLE(59),
        /**
         * The bmi2 instructions available processor feature.
         */
        PF_BMI2_INSTRUCTIONS_AVAILABLE(60),
        /**
         * The movdir64b instruction available processor feature.
         */
        PF_MOVDIR64B_INSTRUCTION_AVAILABLE(61),
        /**
         * The arm lse2 available processor feature.
         */
        PF_ARM_LSE2_AVAILABLE(62),
        /**
         * The reserved processor feature placeholder.
         */
        PF_RESERVED_FEATURE(63),
        /**
         * The arm sha3 instructions available processor feature.
         */
        PF_ARM_SHA3_INSTRUCTIONS_AVAILABLE(64),
        /**
         * The arm sha512 instructions available processor feature.
         */
        PF_ARM_SHA512_INSTRUCTIONS_AVAILABLE(65),
        /**
         * The arm v82 int8 matrix multiplication instructions available processor feature.
         */
        PF_ARM_V82_I8MM_INSTRUCTIONS_AVAILABLE(66),
        /**
         * The arm v82 float16 instructions available processor feature.
         */
        PF_ARM_V82_FP16_INSTRUCTIONS_AVAILABLE(67),
        /**
         * The arm v86 bfloat16 instructions available processor feature.
         */
        PF_ARM_V86_BF16_INSTRUCTIONS_AVAILABLE(68),
        /**
         * The arm v86 enhanced bfloat16 instructions available processor feature.
         */
        PF_ARM_V86_EBF16_INSTRUCTIONS_AVAILABLE(69),
        /**
         * The arm sme instructions available processor feature.
         */
        PF_ARM_SME_INSTRUCTIONS_AVAILABLE(70),
        /**
         * The arm sme2 instructions available processor feature.
         */
        PF_ARM_SME2_INSTRUCTIONS_AVAILABLE(71),
        /**
         * The arm sme2.1 instructions available processor feature.
         */
        PF_ARM_SME2_1_INSTRUCTIONS_AVAILABLE(72),
        /**
         * The arm sme2.2 instructions available processor feature.
         */
        PF_ARM_SME2_2_INSTRUCTIONS_AVAILABLE(73),
        /**
         * The arm sme aes instructions available processor feature.
         */
        PF_ARM_SME_AES_INSTRUCTIONS_AVAILABLE(74),
        /**
         * The arm sme sbitperm instructions available processor feature.
         */
        PF_ARM_SME_SBITPERM_INSTRUCTIONS_AVAILABLE(75),
        /**
         * The arm sme sf8mm4 instructions available processor feature.
         */
        PF_ARM_SME_SF8MM4_INSTRUCTIONS_AVAILABLE(76),
        /**
         * The arm sme sf8mm8 instructions available processor feature.
         */
        PF_ARM_SME_SF8MM8_INSTRUCTIONS_AVAILABLE(77),
        /**
         * The arm sme sf8dp2 instructions available processor feature.
         */
        PF_ARM_SME_SF8DP2_INSTRUCTIONS_AVAILABLE(78),
        /**
         * The arm sme sf8dp4 instructions available processor feature.
         */
        PF_ARM_SME_SF8DP4_INSTRUCTIONS_AVAILABLE(79),
        /**
         * The arm sme sf8fma instructions available processor feature.
         */
        PF_ARM_SME_SF8FMA_INSTRUCTIONS_AVAILABLE(80),
        /**
         * The arm sme f8f32 instructions available processor feature.
         */
        PF_ARM_SME_F8F32_INSTRUCTIONS_AVAILABLE(81),
        /**
         * The arm sme f8f16 instructions available processor feature.
         */
        PF_ARM_SME_F8F16_INSTRUCTIONS_AVAILABLE(82),
        /**
         * The arm sme f16f16 instructions available processor feature.
         */
        PF_ARM_SME_F16F16_INSTRUCTIONS_AVAILABLE(83),
        /**
         * The arm sme b16b16 instructions available processor feature.
         */
        PF_ARM_SME_B16B16_INSTRUCTIONS_AVAILABLE(84),
        /**
         * The arm sme f64f64 instructions available processor feature.
         */
        PF_ARM_SME_F64F64_INSTRUCTIONS_AVAILABLE(85),
        /**
         * The arm sme i16i64 instructions available processor feature.
         */
        PF_ARM_SME_I16I64_INSTRUCTIONS_AVAILABLE(86),
        /**
         * The arm sme lutv2 instructions available processor feature.
         */
        PF_ARM_SME_LUTv2_INSTRUCTIONS_AVAILABLE(87),
        /**
         * The arm sme fa64 instructions available processor feature.
         */
        PF_ARM_SME_FA64_INSTRUCTIONS_AVAILABLE(88),
        /**
         * The umonitor instruction available processor feature.
         */
        PF_UMONITOR_INSTRUCTION_AVAILABLE(89);

        /**
         * The value value.
         */
        private final int value;

        /**
         * Creates a new ProcessorFeature instance.
         *
         * @param value the value
         */
        ProcessorFeature(int value) {
            this.value = value;
        }

        /**
         * Executes the value operation.
         *
         * @return the value
         */
        public int value() {
            return value;
        }

    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.data;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Arrays;
import java.util.StringTokenizer;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.galaxy.SafeBuffer;
import org.miaixz.bus.logger.Logger;

/**
 * 特定字符集类，用于处理DICOM中的各种字符编码。 该类提供了字符集的编码、解码、转换等功能，支持多种DICOM标准中定义的字符集， 包括ASCII、ISO-8859系列、JIS系列、GB系列、UTF-8等。 该类还支持ISO
 * 2022字符集切换机制，允许在同一文本中使用多种字符集。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpecificCharacterSet {

    /**
     * ASCII字符集实例
     */
    public static final SpecificCharacterSet ASCII = new SpecificCharacterSet(new Codec[] { Codec.ISO_646 });

    /**
     * 缓存编码器1的线程局部变量
     */
    private static final ThreadLocal<SoftReference<Encoder>> cachedEncoder1 = new ThreadLocal<>();

    /**
     * 缓存编码器2的线程局部变量
     */
    private static final ThreadLocal<SoftReference<Encoder>> cachedEncoder2 = new ThreadLocal<>();

    /**
     * 默认字符集
     */
    private static SpecificCharacterSet DEFAULT = ASCII;

    /**
     * 编解码器数组
     */
    protected final Codec[] codecs;

    /**
     * DICOM字符集代码数组
     */
    protected final String[] dicomCodes;

    /**
     * 构造一个特定字符集
     *
     * @param codecs 编解码器数组
     * @param codes  DICOM字符集代码数组
     */
    protected SpecificCharacterSet(Codec[] codecs, String... codes) {
        this.codecs = codecs;
        this.dicomCodes = codes;
    }

    /**
     * 获取默认字符集
     *
     * @return 默认字符集
     */
    public static SpecificCharacterSet getDefaultCharacterSet() {
        return DEFAULT;
    }

    /**
     * 设置默认字符集
     *
     * @param code 字符集代码
     * @throws IllegalArgumentException 如果默认字符集不包含ASCII
     */
    public static void setDefaultCharacterSet(String code) {
        SpecificCharacterSet cs = code != null ? valueOf(code) : ASCII;
        if (!cs.containsASCII())
            throw new IllegalArgumentException("Default Character Set must contain ASCII - " + code);
        DEFAULT = cs;
    }

    /**
     * 覆盖DICOM特定字符集(0008,0005)的值到命名字符集的映射。
     * <p>
     * 例如，{@code SpecificCharacterSet.setCharsetNameMapping("ISO_IR 100", "ISO-8859-15")}将 ISO-8859-15
     * (Latin-9)，{@code SpecificCharacterSet.setCharsetNameMapping("ISO_IR 100", "windows-1252")} Windows-1252
     * (CP-1252)，与DICOM特定字符集(0008,0005)代码值{@code ISO_IR 100}关联 - 替换 默认映射到ISO-8859-1 (Latin-1) -
     * 两者(ISO-8859-15和Windows-1252)都包含拉丁语1中没有的字符Š/š 和Ž/ž，但在爱沙尼亚语和芬兰语中用于转录外国名称。
     *
     * @param code        DICOM特定字符集(0008,0005)的值
     * @param charsetName 映射的字符集名称
     * @throws IllegalCharsetNameException 如果给定的代码或字符集名称不合法
     * @throws IllegalArgumentException    如果给定的{@code charsetName}为null
     * @throws UnsupportedCharsetException 如果Java虚拟机实例中不支持命名字符集
     */
    public static void setCharsetNameMapping(String code, String charsetName) {
        Codec.forCode(code, false).setCharsetName(checkCharsetName(charsetName));
    }

    /**
     * 重置DICOM特定字符集(0008,0005)值到命名字符集的映射，按照
     * <a href="http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.12.html#table_C.12-2"> DICOM PS
     * 3.3 表 C.12-2</a>的规范。
     */
    public static void resetCharsetNameMappings() {
        Codec.resetCharsetNames();
    }

    /**
     * 检查特定字符集代码是否有效
     *
     * @param code 字符集代码
     * @return 有效的字符集代码
     */
    public static String checkSpecificCharacterSet(String code) {
        Codec.forCode(code, false);
        return code;
    }

    /**
     * 检查字符集名称是否有效
     *
     * @param charsetName 字符集名称
     * @return 有效的字符集名称
     * @throws UnsupportedCharsetException 如果不支持该字符集
     */
    public static String checkCharsetName(String charsetName) {
        if (!Charset.isSupported(charsetName))
            throw new UnsupportedCharsetException(charsetName);
        return charsetName;
    }

    /**
     * 根据字符集代码创建特定字符集实例
     *
     * @param codes 字符集代码数组
     * @return 特定字符集实例
     */
    public static SpecificCharacterSet valueOf(String... codes) {
        if (codes == null || codes.length == 0)
            return DEFAULT;
        boolean iso2022 = codes.length > 1;
        Codec defCodec = SpecificCharacterSet.DEFAULT.codecs[0];
        if (iso2022) {
            codes = checkISO2022(codes);
            if (defCodec == Codec.UTF_8) {
                defCodec = Codec.ISO_646;
            }
        }
        Codec[] infos = new Codec[codes.length];
        for (int i = 0; i < codes.length; i++) {
            infos[i] = Codec.forCode(codes[i], true, defCodec);
        }
        return iso2022 ? new ISO2022(infos, codes) : new SpecificCharacterSet(infos, codes);
    }

    /**
     * 将带代码扩展的单字节字符集的单个代码替换为不带代码扩展的单字节字符集的代码。
     *
     * @param codes 代码数组
     * @return 如果代码被替换则返回true
     */
    public static boolean trimISO2022(String[] codes) {
        if (codes != null && codes.length == 1 && codes[0].startsWith("ISO 2022")) {
            switch (codes[0]) {
            case "ISO 2022 IR 6":
                codes[0] = Normal.EMPTY;
                return true;
            case "ISO 2022 IR 100":
                codes[0] = "ISO_IR 100";
                return true;
            case "ISO 2022 IR 101":
                codes[0] = "ISO_IR 101";
                return true;
            case "ISO 2022 IR 109":
                codes[0] = "ISO_IR 109";
                return true;
            case "ISO 2022 IR 110":
                codes[0] = "ISO_IR 110";
                return true;
            case "ISO 2022 IR 144":
                codes[0] = "ISO_IR 144";
                return true;
            case "ISO 2022 IR 127":
                codes[0] = "ISO_IR 127";
                return true;
            case "ISO 2022 IR 126":
                codes[0] = "ISO_IR 126";
                return true;
            case "ISO 2022 IR 138":
                codes[0] = "ISO_IR 138";
                return true;
            case "ISO 2022 IR 148":
                codes[0] = "ISO_IR 148";
                return true;
            case "ISO 2022 IR 13":
                codes[0] = "ISO_IR 13";
                return true;
            case "ISO 2022 IR 166":
                codes[0] = "ISO_IR 166";
                return true;
            }
        }
        return false;
    }

    /**
     * 检查并修正ISO 2022字符集代码
     *
     * @param codes 字符集代码数组
     * @return 修正后的字符集代码数组
     */
    private static String[] checkISO2022(String[] codes) {
        String[] results = codes;
        for (int i = 0; i < codes.length; i++) {
            String code = codes[i];
            if (code != null && !code.isEmpty() && !code.startsWith("ISO 2022")) {
                switch (code) {
                case "ISO_IR 100":
                case "ISO_IR 101":
                case "ISO_IR 109":
                case "ISO_IR 110":
                case "ISO_IR 144":
                case "ISO_IR 127":
                case "ISO_IR 126":
                case "ISO_IR 138":
                case "ISO_IR 148":
                case "ISO_IR 13":
                case "ISO_IR 166":
                    if (results == codes)
                        results = codes.clone();
                    results[i] = "ISO 2022 " + code.substring(4);
                    continue;
                }
                Logger.info("Invalid Specific Character Set: [{}] - treat as [{}]", Builder.concat(codes, '\\'),
                        Builder.maskNull(codes[0], ""));
                return new String[] { codes[0] };
            }
        }
        if (codes != results) {
            Logger.info("Invalid Specific Character Set: [{}] - treat as [{}]", Builder.concat(codes, '\\'),
                    Builder.concat(results, '\\'));
        }
        return ensureFirstContainsASCII(results);
    }

    /**
     * 确保第一个字符集包含ASCII
     *
     * @param codes 字符集代码数组
     * @return 修正后的字符集代码数组
     */
    private static String[] ensureFirstContainsASCII(String[] codes) {
        for (int i = 0; i < codes.length; i++) {
            if (Codec.forCode(codes[i]).containsASCII()) {
                if (i == 0)
                    return codes;
                String[] clone = codes.clone();
                clone[0] = codes[i];
                clone[i] = codes[0];
                Logger.info("Invalid Specific Character Set: [{}] - treat as [{}]", Builder.concat(codes, '\\'),
                        Builder.concat(clone, '\\'));
                return clone;
            }
        }
        String[] withASCII = new String[1 + codes.length];
        withASCII[0] = "";
        System.arraycopy(codes, 0, withASCII, 1, codes.length);
        Logger.info("Invalid Specific Character Set: [{}] - treat as [{}]", Builder.concat(codes, '\\'),
                Builder.concat(withASCII, '\\'));
        return withASCII;
    }

    /**
     * 获取线程局部变量中的编码器
     *
     * @param tl    线程局部变量
     * @param codec 编解码器
     * @return 编码器
     */
    private static Encoder encoder(ThreadLocal<SoftReference<Encoder>> tl, Codec codec) {
        SoftReference<Encoder> sr;
        Encoder enc;
        if ((sr = tl.get()) == null || (enc = sr.get()) == null || enc.codec != codec)
            tl.set(new SoftReference<>(enc = new Encoder(codec)));
        return enc;
    }

    /**
     * 获取DICOM字符集代码数组
     *
     * @return DICOM字符集代码数组
     */
    public String[] toCodes() {
        return dicomCodes;
    }

    /**
     * 将字符串编码为字节数组
     *
     * @param val        要编码的字符串
     * @param delimiters 分隔符
     * @return 编码后的字节数组
     */
    public byte[] encode(String val, String delimiters) {
        return codecs[0].encode(val);
    }

    /**
     * 将字节数组解码为字符串
     *
     * @param val        要解码的字节数组
     * @param delimiters 分隔符
     * @return 解码后的字符串
     */
    public String decode(byte[] val, String delimiters) {
        return codecs[0].decode(val, 0, val.length);
    }

    /**
     * 检查是否为UTF-8字符集
     *
     * @return 如果是UTF-8字符集则返回true，否则返回false
     */
    public boolean isUTF8() {
        return codecs[0].equals(Codec.UTF_8);
    }

    /**
     * 检查是否为ASCII字符集
     *
     * @return 如果是ASCII字符集则返回true，否则返回false
     */
    public boolean isASCII() {
        return codecs[0].equals(Codec.ISO_646);
    }

    /**
     * 检查是否包含ASCII字符集
     *
     * @return 如果包含ASCII字符集则返回true，否则返回false
     */
    public boolean containsASCII() {
        return codecs[0].containsASCII();
    }

    /**
     * 检查是否包含指定的字符集
     *
     * @param other 要检查的字符集
     * @return 如果包含指定的字符集则返回true，否则返回false
     */
    public boolean contains(SpecificCharacterSet other) {
        return Arrays.equals(codecs, other.codecs) || (other.isASCII() || other == ASCII) && containsASCII();
    }

    /**
     * 将字符串转换为文本格式
     *
     * @param s 要转换的字符串
     * @return 转换后的文本
     */
    public String toText(String s) {
        return codecs[0].toText(s);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final SpecificCharacterSet othercs = (SpecificCharacterSet) other;
        return Arrays.equals(this.codecs, othercs.codecs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.codecs);
    }

    /**
     * 编解码器枚举，定义了DICOM中支持的各种字符集
     */
    private enum Codec {
        /** ISO 646 (ASCII)字符集 */
        ISO_646(true, 0x2842, 0, 1),

        /** ISO 8859-1 (Latin-1)字符集 */
        ISO_8859_1(true, 0x2842, 0x2d41, 1),

        /** ISO 8859-2 (Latin-2)字符集 */
        ISO_8859_2(true, 0x2842, 0x2d42, 1),

        /** ISO 8859-3 (Latin-3)字符集 */
        ISO_8859_3(true, 0x2842, 0x2d43, 1),

        /** ISO 8859-4 (Latin-4)字符集 */
        ISO_8859_4(true, 0x2842, 0x2d44, 1),

        /** ISO 8859-5 (Cyrillic)字符集 */
        ISO_8859_5(true, 0x2842, 0x2d4c, 1),

        /** ISO 8859-6 (Arabic)字符集 */
        ISO_8859_6(true, 0x2842, 0x2d47, 1),

        /** ISO 8859-7 (Greek)字符集 */
        ISO_8859_7(true, 0x2842, 0x2d46, 1),

        /** ISO 8859-8 (Hebrew)字符集 */
        ISO_8859_8(true, 0x2842, 0x2d48, 1),

        /** ISO 8859-9 (Latin-5)字符集 */
        ISO_8859_9(true, 0x2842, 0x2d4d, 1),

        /** JIS X 0201字符集 */
        JIS_X_201(true, 0x284a, 0x2949, 1) {
            @Override
            public String toText(String s) {
                return s.replace(Symbol.C_BACKSLASH, Symbol.C_CNY);
            }
        },

        /** TIS-620 (Thai)字符集 */
        TIS_620(true, 0x2842, 0x2d54, 1),

        /** JIS X 0208字符集 */
        JIS_X_208(false, 0x2442, 0, 1),

        /** JIS X 0212字符集 */
        JIS_X_212(false, 0x242844, 0, 2),

        /** KS X 1001 (Korean)字符集 */
        KS_X_1001(false, 0, 0x242943, -1),

        /** GB2312 (Simplified Chinese)字符集 */
        GB2312(false, 0, 0x242941, -1),

        /** UTF-8字符集 */
        UTF_8(true, 0, 0, -1),

        /** GB18030字符集 */
        GB18030(false, 0, 0, -1);

        /** 字符集名称数组 */
        private static final String[] charsetNames = resetCharsetNames(new String[18]);

        /** 是否包含ASCII标志 */
        private final boolean containsASCII;

        /** 转义序列0 */
        private final int escSeq0;

        /** 转义序列1 */
        private final int escSeq1;

        /** 每个字符的字节数 */
        private final int bytesPerChar;

        /**
         * 构造一个编解码器
         *
         * @param containsASCII 是否包含ASCII
         * @param escSeq0       转义序列0
         * @param escSeq1       转义序列1
         * @param bytesPerChar  每个字符的字节数
         */
        Codec(boolean containsASCII, int escSeq0, int escSeq1, int bytesPerChar) {
            this.containsASCII = containsASCII;
            this.escSeq0 = escSeq0;
            this.escSeq1 = escSeq1;
            this.bytesPerChar = bytesPerChar;
        }

        /**
         * 重置字符集名称
         */
        private static void resetCharsetNames() {
            resetCharsetNames(charsetNames);
        }

        /**
         * 重置字符集名称数组
         *
         * @param charsetNames 字符集名称数组
         * @return 重置后的字符集名称数组
         */
        private static String[] resetCharsetNames(String[] charsetNames) {
            charsetNames[0] = "US-ASCII";
            charsetNames[1] = "ISO-8859-1";
            charsetNames[2] = "ISO-8859-2";
            charsetNames[3] = "ISO-8859-3";
            charsetNames[4] = "ISO-8859-4";
            charsetNames[5] = "ISO-8859-5";
            charsetNames[6] = "ISO-8859-6";
            charsetNames[7] = "ISO-8859-7";
            charsetNames[8] = "ISO-8859-8";
            charsetNames[9] = "ISO-8859-9";
            charsetNames[10] = "JIS_X0201";
            charsetNames[11] = "TIS-620";
            charsetNames[12] = "x-JIS0208";
            charsetNames[13] = "JIS_X0212-1990";
            charsetNames[14] = "EUC-KR";
            charsetNames[15] = "GB2312";
            charsetNames[16] = "UTF-8";
            charsetNames[17] = "GB18030";
            return charsetNames;
        }

        /**
         * 根据代码获取编解码器
         *
         * @param code 字符集代码
         * @return 编解码器
         */
        public static Codec forCode(String code) {
            return forCode(code, true);
        }

        /**
         * 根据代码获取编解码器
         *
         * @param code    字符集代码
         * @param lenient 是否宽松模式
         * @return 编解码器
         */
        private static Codec forCode(String code, boolean lenient) {
            return forCode(code, lenient, SpecificCharacterSet.DEFAULT.codecs[0]);
        }

        /**
         * 根据代码获取编解码器
         *
         * @param code     字符集代码
         * @param lenient  是否宽松模式
         * @param defCodec 默认编解码器
         * @return 编解码器
         */
        private static Codec forCode(String code, boolean lenient, Codec defCodec) {
            switch (code != null ? code : Normal.EMPTY) {
            case Normal.EMPTY:
            case "ISO 2022 IR 6":
                return defCodec;
            case "ISO_IR 100":
            case "ISO 2022 IR 100":
                return Codec.ISO_8859_1;
            case "ISO_IR 101":
            case "ISO 2022 IR 101":
                return Codec.ISO_8859_2;
            case "ISO_IR 109":
            case "ISO 2022 IR 109":
                return Codec.ISO_8859_3;
            case "ISO_IR 110":
            case "ISO 2022 IR 110":
                return Codec.ISO_8859_4;
            case "ISO_IR 144":
            case "ISO 2022 IR 144":
                return Codec.ISO_8859_5;
            case "ISO_IR 127":
            case "ISO 2022 IR 127":
                return Codec.ISO_8859_6;
            case "ISO_IR 126":
            case "ISO 2022 IR 126":
                return Codec.ISO_8859_7;
            case "ISO_IR 138":
            case "ISO 2022 IR 138":
                return Codec.ISO_8859_8;
            case "ISO_IR 148":
            case "ISO 2022 IR 148":
                return Codec.ISO_8859_9;
            case "ISO_IR 13":
            case "ISO 2022 IR 13":
                return Codec.JIS_X_201;
            case "ISO_IR 166":
            case "ISO 2022 IR 166":
                return Codec.TIS_620;
            case "ISO 2022 IR 87":
                return Codec.JIS_X_208;
            case "ISO 2022 IR 159":
                return Codec.JIS_X_212;
            case "ISO 2022 IR 149":
                return Codec.KS_X_1001;
            case "ISO 2022 IR 58":
                return Codec.GB2312;
            case "ISO_IR 192":
                return Codec.UTF_8;
            case "GB18030":
            case "GBK":
                return Codec.GB18030;
            }
            if (!lenient)
                throw new IllegalArgumentException("No such Specific Character Set Code: " + code);
            return defCodec;
        }

        /**
         * 将字符串编码为字节数组
         *
         * @param val 要编码的字符串
         * @return 编码后的字节数组
         */
        public byte[] encode(String val) {
            try {
                return val.getBytes(charsetName());
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        /**
         * 获取字符集名称
         *
         * @return 字符集名称
         */
        private String charsetName() {
            return charsetNames[ordinal()];
        }

        /**
         * 设置字符集名称
         *
         * @param charsetName 字符集名称
         */
        private void setCharsetName(String charsetName) {
            charsetNames[ordinal()] = charsetName;
        }

        /**
         * 将字节数组解码为字符串
         *
         * @param b   字节数组
         * @param off 偏移量
         * @param len 长度
         * @return 解码后的字符串
         */
        public String decode(byte[] b, int off, int len) {
            try {
                return new String(b, off, len, charsetName());
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        /**
         * 检查是否包含ASCII
         *
         * @return 如果包含ASCII则返回true，否则返回false
         */
        public boolean containsASCII() {
            return containsASCII;
        }

        /**
         * 获取转义序列0
         *
         * @return 转义序列0
         */
        public int getEscSeq0() {
            return escSeq0;
        }

        /**
         * 获取转义序列1
         *
         * @return 转义序列1
         */
        public int getEscSeq1() {
            return escSeq1;
        }

        /**
         * 获取每个字符的字节数
         *
         * @return 每个字符的字节数
         */
        public int getBytesPerChar() {
            return bytesPerChar;
        }

        /**
         * 将字符串转换为文本格式
         *
         * @param s 要转换的字符串
         * @return 转换后的文本
         */
        public String toText(String s) {
            return s;
        }
    }

    /**
     * G0/G1字符集范围枚举
     */
    private enum G0G1 {
        /** 仅G0 */
        G0,
        /** 仅G1 */
        G1,
        /** G0和G1 */
        Both
    }

    /**
     * 编码器类，用于将字符编码为字节
     */
    private static final class Encoder {
        /** 编解码器 */
        final Codec codec;

        /** 字符集编码器 */
        final CharsetEncoder encoder;

        /**
         * 构造一个编码器
         *
         * @param codec 编解码器
         */
        public Encoder(Codec codec) {
            this.codec = codec;
            this.encoder = Charset.forName(codec.charsetName()).newEncoder();
        }

        /**
         * 添加转义序列到字节缓冲区
         *
         * @param bb  字节缓冲区
         * @param seq 转义序列
         */
        private static void escSeq(ByteBuffer bb, int seq) {
            if (seq == 0)
                return;
            bb.put((byte) 0x1b);
            int b1 = seq >> 16;
            if (b1 != 0)
                bb.put((byte) b1);
            bb.put((byte) (seq >> 8));
            bb.put((byte) seq);
        }

        /**
         * 编码字符缓冲区到字节缓冲区
         *
         * @param cb          字符缓冲区
         * @param bb          字节缓冲区
         * @param escSeq      转义序列
         * @param useRange    使用的范围
         * @param errorAction 错误处理动作
         * @return 如果编码成功则返回true，否则返回false
         */
        public boolean encode(CharBuffer cb, ByteBuffer bb, int escSeq, G0G1 useRange, CodingErrorAction errorAction) {
            encoder.onMalformedInput(errorAction).onUnmappableCharacter(errorAction).reset();
            int cbmark = cb.position();
            int bbmark = bb.position();
            try {
                escSeq(bb, escSeq);
                int graphicCharStart = bb.position();
                CoderResult cr = encoder.encode(cb, bb, true);
                if (!cr.isUnderflow())
                    cr.throwException();
                cr = encoder.flush(bb);
                if (!cr.isUnderflow())
                    cr.throwException();
                if (useRange == G0G1.G0) {
                    for (int i = graphicCharStart, end = bb.position(); i < end; ++i) {
                        if (0 > bb.get(i)) {
                            throw new CharacterCodingException();
                        }
                    }
                } else if (useRange == G0G1.G1) {
                    for (int i = graphicCharStart, end = bb.position(); i < end; ++i) {
                        if (0 <= bb.get(i)) {
                            throw new CharacterCodingException();
                        }
                    }
                }
                // if useRange == G0G1.Both, then do nothing
            } catch (CharacterCodingException x) {
                SafeBuffer.position(cb, cbmark);
                SafeBuffer.position(bb, bbmark);
                return false;
            }
            return true;
        }

        /**
         * 获取替换字节
         *
         * @return 替换字节
         */
        public byte[] replacement() {
            return encoder.replacement();
        }
    }

    /**
     * ISO 2022字符集类，支持在同一文本中使用多种字符集
     */
    private static final class ISO2022 extends SpecificCharacterSet {
        /**
         * 构造一个ISO 2022字符集
         *
         * @param charsetInfos 字符集信息数组
         * @param codes        DICOM字符集代码数组
         */
        private ISO2022(Codec[] charsetInfos, String... codes) {
            super(charsetInfos, codes);
        }

        @Override
        public byte[] encode(String val, String delimiters) {
            int strlen = val.length();
            CharBuffer cb = CharBuffer.wrap(val.toCharArray());
            Encoder enc1 = encoder(cachedEncoder1, codecs[0]);
            byte[] buf = new byte[strlen];
            ByteBuffer bb = ByteBuffer.wrap(buf);
            // 尝试使用(0008,0005)特定字符集的value1指定的字符集编码整个字符串值
            if (!enc1.encode(cb, bb, 0, G0G1.Both, CodingErrorAction.REPORT)) {
                // 根据VR特定的分隔符分割整个字符串值
                // 并尝试分别编码每个组件
                Encoder[] encs = new Encoder[codecs.length];
                encs[0] = enc1;
                encs[1] = encoder(cachedEncoder2, codecs[1]);
                StringTokenizer comps = new StringTokenizer(val, delimiters, true);
                buf = new byte[(2 + 4) * strlen];
                bb = ByteBuffer.wrap(buf);
                int[] cur = { 0, 0 };
                while (comps.hasMoreTokens()) {
                    String comp = comps.nextToken();
                    if (comp.length() == 1 && delimiters.indexOf(comp.charAt(0)) >= 0) { // 如果是分隔符
                        activateInitialCharacterSet(bb, cur);
                        bb.put((byte) comp.charAt(0));
                        continue;
                    }
                    cb = CharBuffer.wrap(comp.toCharArray());
                    encodeComponent(encs, cb, bb, cur);
                }
                activateInitialCharacterSet(bb, cur);
            }
            return Arrays.copyOf(buf, bb.position());
        }

        /**
         * 编码组件
         *
         * @param encs 编码器数组
         * @param cb   字符缓冲区
         * @param bb   字节缓冲区
         * @param cur  当前字符集索引
         */
        private void encodeComponent(Encoder[] encs, CharBuffer cb, ByteBuffer bb, int[] cur) {
            // 尝试使用G1的当前活动字符集编码组件
            if (codecs[cur[1]].getEscSeq1() != 0 && encs[cur[1]].encode(cb, bb, 0, G0G1.G1, CodingErrorAction.REPORT))
                return;
            // 尝试使用G0的当前活动字符集编码组件，如果与G1不同
            if ((codecs[cur[1]].getEscSeq1() == 0 || codecs[cur[1]].getEscSeq0() != codecs[cur[0]].getEscSeq0())
                    && encs[cur[0]].encode(cb, bb, 0, G0G1.G0, CodingErrorAction.REPORT))
                return;
            int next = encs.length;
            while (--next >= 0) {
                if (encs[next] == null)
                    encs[next] = new Encoder(codecs[next]);
                if (codecs[next].getEscSeq1() != 0) {
                    if (encs[next].encode(cb, bb, codecs[next].getEscSeq1(), G0G1.G1, CodingErrorAction.REPORT)) {
                        cur[1] = next;
                        break;
                    }
                }
                if (codecs[next].getEscSeq0() != 0) {
                    if (encs[next].encode(cb, bb, codecs[next].getEscSeq0(), G0G1.G0, CodingErrorAction.REPORT)) {
                        cur[0] = next;
                        break;
                    }
                }
            }
            if (next < 0) {
                if (cb.length() > 1) {
                    for (int i = 0; i < cb.length(); i++) {
                        encodeComponent(encs, cb.subSequence(i, i + 1), bb, cur);
                    }
                } else {
                    // 无法使用任何指定的字符集编码字符，
                    // 使用G0的当前字符集编码它，使用字符集解码器的默认替换
                    // 替换无法编码的字符
                    bb.put(encs[cur[0]].replacement());
                }
            }
        }

        /**
         * 激活初始字符集
         *
         * @param bb  字节缓冲区
         * @param cur 当前字符集索引
         */
        private void activateInitialCharacterSet(ByteBuffer bb, int[] cur) {
            if (cur[0] != 0) {
                Encoder.escSeq(bb, codecs[0].getEscSeq0());
                cur[0] = 0;
            }
            if (cur[1] != 0) {
                Encoder.escSeq(bb, codecs[0].getEscSeq1());
                cur[1] = 0;
            }
        }

        @Override
        public String decode(byte[] b, String delimiters) {
            Codec[] codec = { codecs[0], codecs[0] };
            int g = 0;
            int off = 0;
            int cur = 0;
            StringBuilder sb = new StringBuilder(b.length);
            while (cur < b.length) {
                if (b[cur] == 0x1b && cur + 2 < b.length) { // ESC
                    if (off < cur) {
                        sb.append(codec[g].decode(b, off, cur - off));
                    }
                    int esc0 = cur++;
                    int esc1 = cur++;
                    int esc2 = cur++;
                    switch (((b[esc1] & 255) << 8) + (b[esc2] & 255)) {
                    case 0x2428:
                        if (cur < b.length && b[cur++] == 0x44) {
                            codec[0] = Codec.JIS_X_212;
                        } else { // 将无效的ESC序列解码为字符
                            sb.append(codec[0].decode(b, esc0, cur - esc0));
                        }
                        break;
                    case 0x2429:
                        switch (cur < b.length ? b[cur++] : -1) {
                        case 0x41:
                            switchCodec(codec, 1, Codec.GB2312);
                            break;
                        case 0x43:
                            switchCodec(codec, 1, Codec.KS_X_1001);
                            break;
                        default: // 将无效的ESC序列解码为字符
                            sb.append(codec[0].decode(b, esc0, cur - esc0));
                        }
                        break;
                    case 0x2442:
                        codec[0] = Codec.JIS_X_208;
                        break;
                    case 0x2842:
                        switchCodec(codec, 0, Codec.ISO_646);
                        break;
                    case 0x284a:
                        codec[0] = Codec.JIS_X_201;
                        if (codec[1].getEscSeq1() == 0)
                            codec[1] = codec[0];
                        break;
                    case 0x2949:
                        codec[1] = Codec.JIS_X_201;
                        break;
                    case 0x2d41:
                        switchCodec(codec, 1, Codec.ISO_8859_1);
                        break;
                    case 0x2d42:
                        switchCodec(codec, 1, Codec.ISO_8859_2);
                        break;
                    case 0x2d43:
                        switchCodec(codec, 1, Codec.ISO_8859_3);
                        break;
                    case 0x2d44:
                        switchCodec(codec, 1, Codec.ISO_8859_4);
                        break;
                    case 0x2d46:
                        switchCodec(codec, 1, Codec.ISO_8859_7);
                        break;
                    case 0x2d47:
                        switchCodec(codec, 1, Codec.ISO_8859_6);
                        break;
                    case 0x2d48:
                        switchCodec(codec, 1, Codec.ISO_8859_8);
                        break;
                    case 0x2d4c:
                        switchCodec(codec, 1, Codec.ISO_8859_5);
                        break;
                    case 0x2d4d:
                        switchCodec(codec, 1, Codec.ISO_8859_9);
                        break;
                    case 0x2d54:
                        switchCodec(codec, 1, Codec.TIS_620);
                        break;
                    default: // 将无效的ESC序列解码为字符
                        sb.append(codec[0].decode(b, esc0, cur - esc0));
                    }
                    off = cur;
                } else {
                    if (codec[0] != codec[1] && g == (b[cur] < 0 ? 0 : 1)) {
                        if (off < cur) {
                            sb.append(codec[g].decode(b, off, cur - off));
                        }
                        off = cur;
                        g = 1 - g;
                    }
                    if (g == 0 && codec[g].containsASCII & delimiters.indexOf(b[cur]) >= 0)
                        codec[0] = codec[1] = codecs[0];
                    int bytesPerChar = codec[g].getBytesPerChar();
                    cur += bytesPerChar > 0 ? bytesPerChar : b[cur] < 0 ? 2 : 1;
                }
            }
            if (off < cur) {
                sb.append(codec[g].decode(b, off, Math.min(cur, b.length) - off));
            }
            return sb.toString();
        }

        /**
         * 切换编解码器
         *
         * @param codecs 编解码器数组
         * @param i      索引
         * @param codec  要切换到的编解码器
         */
        private void switchCodec(Codec[] codecs, int i, Codec codec) {
            codecs[i] = codec;
            if (codecs[0].getEscSeq0() == codecs[1].getEscSeq0())
                codecs[0] = codecs[1];
        }
    }

}
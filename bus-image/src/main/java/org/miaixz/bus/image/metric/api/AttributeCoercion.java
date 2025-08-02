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
package org.miaixz.bus.image.metric.api;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.metric.TransferCapability;

/**
 * 属性强制转换类，用于定义在特定条件下对DICOM属性进行强制转换的规则。 该类实现了Serializable和Comparable接口，支持序列化和比较操作。
 * 属性强制转换规则可以基于SOP类、DIMSE操作、角色和AE标题等条件进行匹配。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AttributeCoercion implements Serializable, Comparable<AttributeCoercion> {

    @Serial
    private static final long serialVersionUID = 2852262397936L;

    /**
     * 通用名称
     */
    private final String commonName;

    /**
     * 条件对象
     */
    private final Condition condition;

    /**
     * URI标识
     */
    private final String uri;

    /**
     * 构造一个属性强制转换规则
     *
     * @param commonName 通用名称
     * @param sopClasses SOP类数组
     * @param dimse      DIMSE操作
     * @param role       角色（SCU或SCP）
     * @param aeTitles   AE标题数组
     * @param uri        URI标识
     * @throws NullPointerException     如果commonName为null
     * @throws IllegalArgumentException 如果commonName为空
     */
    public AttributeCoercion(String commonName, String[] sopClasses, Dimse dimse, TransferCapability.Role role,
            String[] aeTitles, String uri) {
        if (commonName == null)
            throw new NullPointerException("commonName");
        if (commonName.isEmpty())
            throw new IllegalArgumentException("commonName cannot be empty");
        this.commonName = commonName;
        this.condition = new Condition(Builder.maskNull(sopClasses), dimse, role, Builder.maskNull(aeTitles));
        this.uri = uri;
    }

    /**
     * 将CUID列表追加到字符串构建器
     *
     * @param sb     字符串构建器
     * @param indent 缩进字符串
     * @param cuids  CUID数组
     */
    private static void promptCUIDsTo(StringBuilder sb, String indent, String[] cuids) {
        if (cuids.length == 0)
            return;
        sb.append(indent).append("cuids: ");
        for (String cuid : cuids)
            UID.promptTo(cuid, sb).append(Symbol.C_COMMA);
        sb.setLength(sb.length() - 1);
        sb.append(Builder.LINE_SEPARATOR);
    }

    /**
     * 将AE标题列表追加到字符串构建器
     *
     * @param sb     字符串构建器
     * @param indent 缩进字符串
     * @param aets   AE标题数组
     */
    private static void promptAETsTo(StringBuilder sb, String indent, String[] aets) {
        if (aets.length == 0)
            return;
        sb.append(indent).append("aets: ");
        for (String aet : aets)
            sb.append(aet).append(Symbol.C_COMMA);
        sb.setLength(sb.length() - 1);
        sb.append(Builder.LINE_SEPARATOR);
    }

    /**
     * 获取通用名称
     *
     * @return 通用名称
     */
    public final String getCommonName() {
        return commonName;
    }

    /**
     * 获取SOP类数组
     *
     * @return SOP类数组
     */
    public final String[] getSOPClasses() {
        return condition.sopClasses;
    }

    /**
     * 获取DIMSE操作
     *
     * @return DIMSE操作
     */
    public final Dimse getDIMSE() {
        return condition.dimse;
    }

    /**
     * 获取角色
     *
     * @return 角色（SCU或SCP）
     */
    public final TransferCapability.Role getRole() {
        return condition.role;
    }

    /**
     * 获取AE标题数组
     *
     * @return AE标题数组
     */
    public final String[] getAETitles() {
        return condition.aeTitles;
    }

    /**
     * 获取URI标识
     *
     * @return URI标识
     */
    public final String getURI() {
        return uri;
    }

    /**
     * 检查是否匹配指定条件
     *
     * @param sopClass SOP类
     * @param dimse    DIMSE操作
     * @param role     角色（SCU或SCP）
     * @param aeTitle  AE标题
     * @return 如果匹配则返回true，否则返回false
     */
    public boolean matchesCondition(String sopClass, Dimse dimse, TransferCapability.Role role, String aeTitle) {
        return condition.matches(sopClass, dimse, role, aeTitle);
    }

    /**
     * 比较两个属性强制转换规则的优先级
     *
     * @param o 要比较的属性强制转换规则
     * @return 如果此规则的优先级高于指定规则则返回负数，如果低于则返回正数，如果相等则返回0
     */
    @Override
    public int compareTo(AttributeCoercion o) {
        return condition.compareTo(o.condition);
    }

    /**
     * 返回此属性强制转换规则的字符串表示
     *
     * @return 属性强制转换规则的字符串表示
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._64), Normal.EMPTY).toString();
    }

    /**
     * 将此属性强制转换规则的提示信息追加到指定的字符串构建器
     *
     * @param sb     字符串构建器
     * @param indent 缩进字符串
     * @return 追加后的字符串构建器
     */
    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Builder.appendLine(sb, indent, "AttributeCoercion[cn: ", commonName);
        Builder.appendLine(sb, indent2, "dimse: ", condition.dimse);
        Builder.appendLine(sb, indent2, "role: ", condition.role);
        promptCUIDsTo(sb, indent2, condition.sopClasses);
        promptAETsTo(sb, indent2, condition.aeTitles);
        Builder.appendLine(sb, indent2, "cuids: ", Arrays.toString(condition.sopClasses));
        Builder.appendLine(sb, indent2, "aets: ", Arrays.toString(condition.aeTitles));
        Builder.appendLine(sb, indent2, "uri: ", uri);
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    /**
     * 条件类，用于存储和匹配属性强制转换的条件
     */
    private static class Condition implements Serializable, Comparable<Condition> {

        @Serial
        private static final long serialVersionUID = 2852262539097L;

        /**
         * SOP类数组
         */
        final String[] sopClasses;

        /**
         * DIMSE操作
         */
        final Dimse dimse;

        /**
         * 角色（SCU或SCP）
         */
        final TransferCapability.Role role;

        /**
         * AE标题数组
         */
        final String[] aeTitles;

        /**
         * 权重，用于比较条件优先级
         */
        final int weight;

        /**
         * 构造一个条件对象
         *
         * @param sopClasses SOP类数组
         * @param dimse      DIMSE操作
         * @param role       角色（SCU或SCP）
         * @param aeTitles   AE标题数组
         * @throws NullPointerException 如果dimse或role为null
         */
        public Condition(String[] sopClasses, Dimse dimse, TransferCapability.Role role, String[] aeTitles) {
            if (dimse == null)
                throw new NullPointerException("dimse");
            if (role == null)
                throw new NullPointerException("role");
            this.sopClasses = sopClasses;
            this.dimse = dimse;
            this.role = role;
            this.aeTitles = aeTitles;
            this.weight = (aeTitles.length != 0 ? 2 : 0) + (sopClasses.length != 0 ? 1 : 0);
        }

        /**
         * 检查数组是否为空或包含指定对象
         *
         * @param a 数组
         * @param o 要查找的对象
         * @return 如果数组为空或包含指定对象则返回true，否则返回false
         */
        private static boolean isEmptyOrContains(Object[] a, Object o) {
            if (o == null || a.length == 0)
                return true;
            for (Object object : a)
                if (o.equals(object))
                    return true;
            return false;
        }

        /**
         * 比较两个条件的优先级
         *
         * @param o 要比较的条件
         * @return 如果此条件的优先级高于指定条件则返回负数，如果低于则返回正数，如果相等则返回0
         */
        @Override
        public int compareTo(Condition o) {
            return o.weight - weight;
        }

        /**
         * 检查是否匹配指定条件
         *
         * @param sopClass SOP类
         * @param dimse    DIMSE操作
         * @param role     角色（SCU或SCP）
         * @param aeTitle  AE标题
         * @return 如果匹配则返回true，否则返回false
         */
        public boolean matches(String sopClass, Dimse dimse, TransferCapability.Role role, String aeTitle) {
            return this.dimse == dimse && this.role == role && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.sopClasses, sopClass);
        }
    }

}
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
package org.miaixz.bus.goalie.handler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.goalie.annotation.TerminalVersion;
import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;

import jakarta.servlet.http.HttpServletRequest;

/**
 * API 版本请求条件类，用于匹配基于终端类型和版本的请求，支持 TerminalVersion 注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApiVersionRequestCondition extends AbstractRequestCondition<ApiVersionRequestCondition> {

    /**
     * 终端版本表达式集合，用于存储解析后的匹配规则
     */
    private final Set<TerminalVersionExpression> expressions;

    /**
     * 构造器，初始化表达式集合
     *
     * @param expressions 终端版本表达式集合
     */
    protected ApiVersionRequestCondition(Set<TerminalVersionExpression> expressions) {
        this.expressions = expressions;
    }

    /**
     * 构造器，通过字符串表达式解析初始化
     *
     * @param stringExpressions 字符串表达式数组，格式如 "1,2>=1.0"
     */
    public ApiVersionRequestCondition(String[] stringExpressions) {
        this.expressions = Collections.unmodifiableSet(parseByExpression(stringExpressions));
    }

    /**
     * 构造器，通过 TerminalVersion 注解解析初始化
     *
     * @param terminalVersions TerminalVersion 注解数组
     */
    public ApiVersionRequestCondition(TerminalVersion[] terminalVersions) {
        this.expressions = Collections.unmodifiableSet(parseByTerminalVersion(terminalVersions));
    }

    /**
     * 解析 TerminalVersion 注解为表达式集合
     *
     * @param terminalVersions TerminalVersion 注解数组
     * @return 解析后的表达式集合
     */
    private static Set<TerminalVersionExpression> parseByTerminalVersion(TerminalVersion[] terminalVersions) {
        Set<TerminalVersionExpression> expressions = new LinkedHashSet<>();
        for (TerminalVersion terminalVersion : terminalVersions) {
            expressions.add(new TerminalVersionExpression(terminalVersion.terminals(), terminalVersion.version(),
                    terminalVersion.op()));
        }
        return expressions;
    }

    /**
     * 解析字符串表达式为终端版本表达式集合
     *
     * @param stringExpressions 字符串表达式数组
     * @return 解析后的表达式集合
     */
    private static Set<TerminalVersionExpression> parseByExpression(String[] stringExpressions) {
        Set<TerminalVersionExpression> terminalExpressions = new LinkedHashSet<>();
        for (String expression : stringExpressions) {
            String regex = "([\\d,*]+)([!=<>]*)([\\d\\.]*)"; // 匹配终端类型、操作符和版本
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(expression);
            while (matcher.find()) {
                int[] terminals = new int[] {};
                String version = Normal.EMPTY;
                TerminalVersion.Version operator = TerminalVersion.Version.NIL;
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String content = matcher.group(i);
                    if (i == 1) { // 解析终端类型
                        if (StringKit.isNotBlank(content) && !content.equalsIgnoreCase(Symbol.STAR)) {
                            String[] split = content.split(Symbol.COMMA);
                            terminals = new int[split.length];
                            for (int j = 0; j < split.length; j++) {
                                try {
                                    terminals[j] = Integer.parseInt(split[j]);
                                } catch (Exception e) {
                                    throw new IllegalArgumentException("is there a wrong number for terminal type?");
                                }
                            }
                        }
                    } else if (i == 2) { // 解析操作符
                        operator = TerminalVersion.Version.parse(content);
                        if (null == operator) {
                            throw new IllegalArgumentException("check the versionOperator!!!");
                        }
                    } else if (i == 3) { // 解析版本号
                        version = content;
                    }
                }
                terminalExpressions.add(new TerminalVersionExpression(terminals, version, operator));
                break;
            }
        }
        return terminalExpressions;
    }

    /**
     * 获取条件内容，即表达式集合
     *
     * @return 表达式集合
     */
    @Override
    protected Collection<?> getContent() {
        return this.expressions;
    }

    /**
     * 获取字符串表示的分隔符
     *
     * @return 分隔符
     */
    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    /**
     * 合并两个条件，合并表达式集合
     *
     * @param other 另一个 ApiVersionRequestCondition
     * @return 合并后的新条件
     */
    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        Set<TerminalVersionExpression> set = new LinkedHashSet<>(this.expressions);
        set.addAll(other.expressions);
        return new ApiVersionRequestCondition(set);
    }

    /**
     * 获取匹配的条件，检查所有表达式是否匹配请求
     *
     * @param request HTTP 请求
     * @return 匹配的条件，若任意表达式不匹配返回 null
     */
    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        for (TerminalVersionExpression expression : expressions) {
            if (!expression.match(request)) {
                return null;
            }
        }
        return this;
    }

    /**
     * 比较两个条件（当前实现不区分优先级）
     *
     * @param other   另一个条件
     * @param request HTTP 请求
     * @return 始终返回 0，表示无优先级差异
     */
    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        return 0;
    }

    /**
     * 终端版本表达式类，封装终端类型、版本和操作符的匹配逻辑
     */
    static class TerminalVersionExpression {

        /**
         * 请求头：版本号
         */
        public static final String HEADER_VERSION = "cv";

        /**
         * 请求头：终端类型
         */
        public static final String HEADER_TERMINAL = "terminal";

        /**
         * 版本号
         */
        private final String version;

        /**
         * 操作符（如 GT、EQ 等）
         */
        private final TerminalVersion.Version operator;

        /**
         * 终端类型数组
         */
        private int[] terminals;

        /**
         * 构造器，初始化表达式
         *
         * @param terminals 终端类型数组
         * @param version   版本号
         * @param operator  操作符
         */
        public TerminalVersionExpression(int[] terminals, String version, TerminalVersion.Version operator) {
            Arrays.sort(terminals); // 排序终端类型，便于二分查找
            if (StringKit.isNotBlank(version) && operator == TerminalVersion.Version.NIL) {
                throw new IllegalArgumentException("operator cant be nil when version is existing...");
            }
            this.terminals = terminals;
            this.version = version;
            this.operator = operator;
        }

        /**
         * 获取终端类型数组
         *
         * @return 终端类型数组
         */
        public int[] getTerminals() {
            return terminals;
        }

        /**
         * 设置终端类型数组
         *
         * @param terminals 终端类型数组
         */
        public void setTerminals(int[] terminals) {
            this.terminals = terminals;
        }

        /**
         * 获取版本号
         *
         * @return 版本号
         */
        public String getVersion() {
            return version;
        }

        /**
         * 生成字符串表示
         *
         * @return 表达式字符串
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (null != terminals && terminals.length != 0) {
                builder.append(ArrayKit.join(terminals, Symbol.COMMA));
            } else {
                builder.append(Symbol.STAR);
            }
            builder.append(operator.getCode());
            if (StringKit.isNotBlank(version)) {
                builder.append(version);
            }
            return builder.toString();
        }

        /**
         * 判断表达式是否相等，基于字符串表示
         *
         * @param object 比较对象
         * @return 若字符串表示相等返回 true
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (null != object && object instanceof TerminalVersionExpression) {
                return this.toString().equalsIgnoreCase(object.toString());
            }
            return false;
        }

        /**
         * 计算哈希值，基于字符串表示
         *
         * @return 哈希值
         */
        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        /**
         * 检查请求是否匹配表达式
         *
         * @param request HTTP 请求
         * @return 若匹配返回 true，否则返回 false
         */
        public final boolean match(HttpServletRequest request) {
            // 匹配终端类型
            if (null != this.terminals && this.terminals.length > 0) {
                int terminal = getTerminal(request);
                int i = Arrays.binarySearch(terminals, terminal);
                if (i < 0) {
                    return false;
                }
            }
            // 匹配版本号
            if (null != this.operator && this.operator != TerminalVersion.Version.NIL) {
                String clientVersion = getVersion(request);
                String checkVersion = getVersion();
                if (StringKit.isBlank(clientVersion)) {
                    return false;
                }
                int i = clientVersion.compareToIgnoreCase(checkVersion);
                switch (operator) {
                case GT:
                    return i > 0;
                case GTE:
                    return i >= 0;
                case LT:
                    return i < 0;
                case LTE:
                    return i <= 0;
                case EQ:
                    return i == 0;
                case NE:
                    return i != 0;
                default:
                    break;
                }
            }
            return true;
        }

        /**
         * 从请求头获取终端类型
         *
         * @param request HTTP 请求
         * @return 终端类型值，解析失败返回 -1
         */
        private int getTerminal(HttpServletRequest request) {
            String value = request.getHeader(HEADER_TERMINAL);
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                return -1;
            }
        }

        /**
         * 从请求头获取客户端版本号
         *
         * @param request HTTP 请求
         * @return 版本号字符串
         */
        private String getVersion(HttpServletRequest request) {
            return request.getHeader(HEADER_VERSION);
        }

    }

}
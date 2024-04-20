/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.goalie.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.goalie.annotation.TerminalVersion;
import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kimi Liu
 * @since Java 17++
 */
public class ApiVersionRequestCondition extends AbstractRequestCondition<ApiVersionRequestCondition> {

    private final Set<TerminalVersionExpression> expressions;

    protected ApiVersionRequestCondition(Set<TerminalVersionExpression> expressions) {
        this.expressions = expressions;
    }

    public ApiVersionRequestCondition(String[] stringExpressions) {
        // 待实现，适配string，可以使用正则快速匹配
        expressions = Collections.unmodifiableSet(parseByExpression(stringExpressions));
    }

    public ApiVersionRequestCondition(TerminalVersion[] terminalVersions) {
        expressions = Collections.unmodifiableSet(parseByTerminalVersion(terminalVersions));
    }

    private static Set<TerminalVersionExpression> parseByTerminalVersion(TerminalVersion[] terminalVersions) {
        Set<TerminalVersionExpression> expressions = new LinkedHashSet<>();
        for (TerminalVersion terminalVersion : terminalVersions) {
            expressions.add(new TerminalVersionExpression(terminalVersion.terminals(), terminalVersion.version(), terminalVersion.op()));
        }
        return expressions;
    }

    private static Set<TerminalVersionExpression> parseByExpression(String[] stringExpressions) {
        Set<TerminalVersionExpression> terminalExpressions = new LinkedHashSet<>();
        for (String expression : stringExpressions) {
            String regex = "([\\d,*]+)([!=<>]*)([\\d\\.]*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(expression);
            while (matcher.find()) {
                int[] terminals = new int[]{};
                String version = Normal.EMPTY;
                TerminalVersion.Version operator = TerminalVersion.Version.NIL;
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String content = matcher.group(i);
                    if (i == 1) {
                        if (StringKit.isNotBlank(content) && !content.equalsIgnoreCase("*")) {
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
                    } else if (i == 2) {
                        operator = TerminalVersion.Version.parse(content);
                        if (null == operator) {
                            throw new IllegalArgumentException("check the versionOperator!!!");
                        }
                    } else if (i == 3) {
                        version = content;
                    }
                }
                terminalExpressions.add(new TerminalVersionExpression(terminals, version, operator));
                break;
            }
        }
        return terminalExpressions;
    }

    @Override
    protected Collection<?> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        Set<TerminalVersionExpression> set = new LinkedHashSet<TerminalVersionExpression>(this.expressions);
        set.addAll(other.expressions);
        return new ApiVersionRequestCondition(set);
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        for (TerminalVersionExpression expression : expressions) {
            if (!expression.match(request)) {// 同param condition,任意一个失败则失败
                return null;
            }
        }
        return this;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        return 0;
    }

    static class TerminalVersionExpression {

        public static final String HEADER_VERSION = "cv";
        public static final String HEADER_TERMINAL = "terminal";
        private final String version;
        private final TerminalVersion.Version operator;
        private int[] terminals;

        public TerminalVersionExpression(int[] terminals, String version, TerminalVersion.Version operator) {
            Arrays.sort(terminals);
            if (StringKit.isNotBlank(version) && operator == TerminalVersion.Version.NIL) {
                throw new IllegalArgumentException("opetator cant be nil when version is existing...");
            }
            this.terminals = terminals;
            this.version = version;
            this.operator = operator;
        }

        public int[] getTerminals() {
            return terminals;
        }

        public void setTerminals(int[] terminals) {
            this.terminals = terminals;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (null != terminals && terminals.length != 0) {
                builder.append(ArrayKit.join(Symbol.COMMA, terminals));
            } else {
                builder.append("*");
            }
            builder.append(operator.getCode());
            if (StringKit.isNotBlank(version)) {
                builder.append(version);
            }
            return builder.toString();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (null != object && object instanceof TerminalVersionExpression) {
                // 暂定最终的表达式结果一致确定唯一性，后期有需要调整
                return this.toString().equalsIgnoreCase(object.toString());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        public final boolean match(HttpServletRequest request) {
            // 匹配客户端类型
            if (null != this.terminals && this.terminals.length > 0) {
                int terminal = getTerminal(request);
                int i = Arrays.binarySearch(terminals, terminal);
                // 未找到则匹配失败
                if (i < 0) {
                    return false;
                }
            }
            if (null != this.operator && this.operator != TerminalVersion.Version.NIL) {
                String clientVersion = getVersion(request);
                String checkVersion = getVersion();
                if (StringKit.isBlank(clientVersion)) {
                    // 尽量保证快速失败
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

        private int getTerminal(HttpServletRequest request) {
            String value = request.getHeader(HEADER_TERMINAL);
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                return -1;
            }
        }

        private String getVersion(HttpServletRequest request) {
            return request.getHeader(HEADER_VERSION);
        }

    }

}

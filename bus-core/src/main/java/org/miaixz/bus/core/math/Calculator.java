/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.math;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Stack;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 数学表达式计算工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Calculator {

    /**
     * 后缀式栈
     */
    private final Stack<String> postfixStack = new Stack<>();
    /**
     * 运用运算符ASCII码-40做索引的运算符优先级
     */
    private final int[] operatPriority = new int[] { 0, 3, 2, 1, -1, 1, 0, 2 };

    /**
     * 计算表达式的值
     *
     * @param expression 表达式
     * @return 计算结果
     */
    public static double conversion(final String expression) {
        return (new Calculator()).calculate(expression);
    }

    /**
     * 将表达式中负数的符号更改
     *
     * @param expression 例如-2+-1*(-3E-2)-(-1) 被转为 ~2+~1*(~3E~2)-(~1)
     * @return 转换后的字符串
     */
    private static String transform(String expression) {
        expression = StringKit.cleanBlank(expression);
        expression = StringKit.removeSuffix(expression, Symbol.EQUAL);
        final char[] arr = expression.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == Symbol.C_MINUS) {
                if (i == 0) {
                    arr[i] = Symbol.C_TILDE;
                } else {
                    final char c = arr[i - 1];
                    if (c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_STAR || c == '/'
                            || c == Symbol.C_PARENTHESE_LEFT || c == 'E' || c == 'e') {
                        arr[i] = Symbol.C_TILDE;
                    }
                }
            }
        }
        if (arr[0] == Symbol.C_TILDE && (arr.length > 1 && arr[1] == Symbol.C_PARENTHESE_LEFT)) {
            arr[0] = Symbol.C_MINUS;
            return "0" + new String(arr);
        } else {
            return new String(arr);
        }
    }

    /**
     * 按照给定的表达式计算
     *
     * @param expression 要计算的表达式例如:5+12*(3+5)/7
     * @return 计算结果
     */
    public double calculate(final String expression) {
        prepare(transform(expression));

        final Stack<String> resultStack = new Stack<>();
        Collections.reverse(postfixStack);// 将后缀式栈反转
        String firstValue, secondValue, currentOp;// 参与计算的第一个值，第二个值和算术运算符
        while (!postfixStack.isEmpty()) {
            currentOp = postfixStack.pop();
            if (!isOperator(currentOp.charAt(0))) {// 如果不是运算符则存入操作数栈中
                currentOp = currentOp.replace(Symbol.TILDE, Symbol.MINUS);
                resultStack.push(currentOp);
            } else {// 如果是运算符则从操作数栈中取两个值和该数值一起参与运算
                secondValue = resultStack.pop();
                firstValue = resultStack.pop();

                // 将负数标记符改为负号
                firstValue = firstValue.replace(Symbol.TILDE, Symbol.MINUS);
                secondValue = secondValue.replace(Symbol.TILDE, Symbol.MINUS);

                final BigDecimal tempResult = calculate(firstValue, secondValue, currentOp.charAt(0));
                resultStack.push(tempResult.toString());
            }
        }

        // 当结果集中有多个数字时，可能是省略*，类似(1+2)3
        return MathKit.mul(resultStack.toArray(new String[0])).doubleValue();
    }

    /**
     * 数据准备阶段将表达式转换成为后缀式栈
     *
     * @param expression 表达式
     */
    private void prepare(final String expression) {
        final Stack<Character> opStack = new Stack<>();
        opStack.push(Symbol.C_COMMA);// 运算符放入栈底元素逗号，此符号优先级最低
        final char[] arr = expression.toCharArray();
        int currentIndex = 0;// 当前字符的位置
        int count = 0;// 上次算术运算符到本次算术运算符的字符的长度便于或者之间的数值
        char currentOp, peekOp;// 当前操作符和栈顶操作符
        for (int i = 0; i < arr.length; i++) {
            currentOp = arr[i];
            if (isOperator(currentOp)) {// 如果当前字符是运算符
                if (count > 0) {
                    postfixStack.push(new String(arr, currentIndex, count));// 取两个运算符之间的数字
                }
                peekOp = opStack.peek();
                if (currentOp == ')') {// 遇到反括号则将运算符栈中的元素移除到后缀式栈中直到遇到左括号
                    while (opStack.peek() != Symbol.C_PARENTHESE_LEFT) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                    }
                    opStack.pop();
                } else {
                    while (currentOp != Symbol.C_PARENTHESE_LEFT && peekOp != Symbol.C_COMMA
                            && compare(currentOp, peekOp)) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                        peekOp = opStack.peek();
                    }
                    opStack.push(currentOp);
                }
                count = 0;
                currentIndex = i + 1;
            } else {
                count++;
            }
        }
        if (count > 1 || (count == 1 && !isOperator(arr[currentIndex]))) {// 最后一个字符不是括号或者其他运算符的则加入后缀式栈中
            postfixStack.push(new String(arr, currentIndex, count));
        }

        while (opStack.peek() != Symbol.C_COMMA) {
            postfixStack.push(String.valueOf(opStack.pop()));// 将操作符栈中的剩余的元素添加到后缀式栈中
        }
    }

    /**
     * 判断是否为算术符号
     *
     * @param c 字符
     * @return 是否为算术符号
     */
    private boolean isOperator(final char c) {
        return c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_STAR || c == '/'
                || c == Symbol.C_PARENTHESE_LEFT || c == ')' || c == Symbol.C_PERCENT;
    }

    /**
     * 利用ASCII码-40做下标去算术符号优先级
     *
     * @param cur  下标
     * @param peek peek
     * @return 优先级，如果cur高或相等，返回true，否则false
     */
    private boolean compare(char cur, char peek) {// 如果是peek优先级高于cur，返回true，默认都是peek优先级要低
        final int offset = 40;
        if (cur == Symbol.C_PERCENT) {
            // %优先级最高
            cur = 47;
        }
        if (peek == Symbol.C_PERCENT) {
            // %优先级最高
            peek = 47;
        }

        return operatPriority[peek - offset] >= operatPriority[cur - offset];
    }

    /**
     * 按照给定的算术运算符做计算
     *
     * @param firstValue  第一个值
     * @param secondValue 第二个值
     * @param currentOp   算数符，只支持'+'、'-'、'*'、'/'、'%'
     * @return 结果
     */
    private BigDecimal calculate(final String firstValue, final String secondValue, final char currentOp) {
        final BigDecimal result;
        switch (currentOp) {
        case Symbol.C_PLUS:
            result = MathKit.add(firstValue, secondValue);
            break;
        case Symbol.C_MINUS:
            result = MathKit.sub(firstValue, secondValue);
            break;
        case Symbol.C_STAR:
            result = MathKit.mul(firstValue, secondValue);
            break;
        case '/':
            result = MathKit.div(firstValue, secondValue);
            break;
        case Symbol.C_PERCENT:
            result = MathKit.toBigDecimal(firstValue).remainder(MathKit.toBigDecimal(secondValue));
            break;
        default:
            throw new IllegalStateException("Unexpected value: " + currentOp);
        }
        return result;
    }

}

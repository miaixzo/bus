/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.math;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.MathKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Stack;

/**
 * 数学表达式计算
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
public class Calculator {

    /**
     * 后缀式栈
     */
    private final Stack<String> postfixStack = new Stack<>();
    /**
     * 运算符栈
     */
    private final Stack<Character> opStack = new Stack<>();
    /**
     * 运用运算符ASCII码-40做索引的运算符优先级
     */
    private final int[] operatPriority = new int[]{0, 3, 2, 1, -1, 1, 0, 2};

    /**
     * 计算表达式的值
     *
     * @param expression 表达式
     * @return 计算结果
     */
    public static double conversion(String expression) {
        final Calculator cal = new Calculator();
        expression = transform(expression);
        return cal.calculate(expression);
    }

    /**
     * 将表达式中负数的符号更改
     *
     * @param expression 例如-2+-1*(-3E-2)-(-1) 被转为 ~2+~1*(~3E~2)-(~1)
     * @return 更改后的表达式
     */
    private static String transform(String expression) {
        expression = StringKit.cleanBlank(expression);
        expression = StringKit.removeSuffix(expression, Symbol.EQUAL);
        final char[] arr = expression.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == Symbol.C_HYPHEN) {
                if (i == 0) {
                    arr[i] = Symbol.C_TILDE;
                } else {
                    char c = arr[i - 1];
                    if (c == Symbol.C_PLUS
                            || c == Symbol.C_HYPHEN
                            || c == Symbol.C_STAR
                            || c == Symbol.C_SLASH
                            || c == Symbol.C_PARENTHESE_LEFT
                            || c == 'E' || c == 'e') {
                        arr[i] = Symbol.C_TILDE;
                    }
                }
            }
        }
        if (arr[0] == Symbol.C_TILDE || (arr.length > 1 && arr[1] == Symbol.C_PARENTHESE_LEFT)) {
            arr[0] = Symbol.C_HYPHEN;
            return Symbol.ZERO + new String(arr);
        } else {
            return new String(arr);
        }
    }

    /**
     * 按照给定的表达式计算
     *
     * @param expression 要计算的表达式例如: 1+2*(3+5)/7
     * @return 计算结果
     */
    public double calculate(String expression) {
        Stack<String> resultStack = new Stack<>();
        prepare(expression);
        // 将后缀式栈反转
        Collections.reverse(postfixStack);
        // 参与计算的第一个值，第二个值和算术运算符
        String firstValue, secondValue, currentValue;
        while (false == postfixStack.isEmpty()) {
            currentValue = postfixStack.pop();
            // 如果不是运算符则存入操作数栈中
            if (false == isOperator(currentValue.charAt(0))) {
                currentValue = currentValue.replace(Symbol.TILDE, Symbol.HYPHEN);
                resultStack.push(currentValue);
            } else {
                // 如果是运算符则从操作数栈中取两个值和该数值一起参与运算
                secondValue = resultStack.pop();
                firstValue = resultStack.pop();

                // 将负数标记符改为负号
                firstValue = firstValue.replace(Symbol.TILDE, Symbol.HYPHEN);
                secondValue = secondValue.replace(Symbol.TILDE, Symbol.HYPHEN);

                BigDecimal tempResult = calculate(firstValue, secondValue, currentValue.charAt(0));
                resultStack.push(tempResult.toString());
            }
        }
        return Double.parseDouble(resultStack.pop());
    }

    /**
     * 数据准备阶段将表达式转换成为后缀式栈
     *
     * @param expression 表达式
     */
    private void prepare(String expression) {
        // 运算符放入栈底元素逗号，此符号优先级最低
        opStack.push(Symbol.C_COMMA);
        char[] arr = expression.toCharArray();
        // 当前字符的位置
        int currentIndex = 0;
        // 上次算术运算符到本次算术运算符的字符的长度便于或者之间的数值
        int count = 0;
        // 当前操作符和栈顶操作符
        char currentOp, peekOp;
        for (int i = 0; i < arr.length; i++) {
            currentOp = arr[i];
            // 如果当前字符是运算符
            if (isOperator(currentOp)) {
                if (count > 0) {
                    // 取两个运算符之间的数字
                    postfixStack.push(new String(arr, currentIndex, count));
                }
                peekOp = opStack.peek();
                if (currentOp == Symbol.C_PARENTHESE_RIGHT) {
                    // 遇到反括号则将运算符栈中的元素移除到后缀式栈中直到遇到左括号
                    while (opStack.peek() != Symbol.C_PARENTHESE_LEFT) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                    }
                    opStack.pop();
                } else {
                    while (currentOp != Symbol.C_PARENTHESE_LEFT
                            && peekOp != Symbol.C_COMMA
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
        if (count > 1 || (count == 1 && !isOperator(arr[currentIndex]))) {
            // 最后一个字符不是括号或者其他运算符的则加入后缀式栈中
            postfixStack.push(new String(arr, currentIndex, count));
        }

        while (opStack.peek() != Symbol.C_COMMA) {
            // 将操作符栈中的剩余的元素添加到后缀式栈中
            postfixStack.push(String.valueOf(opStack.pop()));
        }
    }

    /**
     * 判断是否为算术符号
     *
     * @param c 字符
     * @return 是否为算术符号
     */
    private boolean isOperator(char c) {
        return c == Symbol.C_PLUS
                || c == Symbol.C_HYPHEN
                || c == Symbol.C_STAR
                || c == Symbol.C_SLASH
                || c == Symbol.C_PARENTHESE_LEFT
                || c == Symbol.C_PARENTHESE_RIGHT;
    }

    /**
     * 利用ASCII码-40做下标去算术符号优先级
     *
     * @param cur  下标
     * @param peek peek
     * @return 优先级
     */
    public boolean compare(char cur, char peek) {
        // 如果是peek优先级高于cur，返回true，默认都是peek优先级要低
        boolean result = false;
        if (operatPriority[(peek) - 40] >= operatPriority[(cur) - 40]) {
            result = true;
        }
        return result;
    }

    /**
     * 按照给定的算术运算符做计算
     *
     * @param firstValue  第一个值
     * @param secondValue 第二个值
     * @param currentOp   算数符，只支持'+'、'-'、'*'、'/'
     * @return 结果
     */
    private BigDecimal calculate(String firstValue, String secondValue, char currentOp) {
        BigDecimal result;
        switch (currentOp) {
            case Symbol.C_PLUS:
                result = MathKit.add(firstValue, secondValue);
                break;
            case Symbol.C_HYPHEN:
                result = MathKit.sub(firstValue, secondValue);
                break;
            case Symbol.C_STAR:
                result = MathKit.mul(firstValue, secondValue);
                break;
            case Symbol.C_SLASH:
                result = MathKit.div(firstValue, secondValue);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + currentOp);
        }
        return result;
    }

}

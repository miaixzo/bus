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
package org.miaixz.bus.core.text;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Utility class for calculating text similarity. This class provides methods to compute the similarity between two
 * strings primarily using variations of the Levenshtein distance and longest common subsequence algorithms.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextSimilarity {

    /**
     * Calculates the similarity between two strings using the Levenshtein distance algorithm. If both strings are
     * empty, the similarity is considered 1 (identical). The comparison method involves:
     * <ul>
     * <li>Only comparing alphanumeric and Chinese characters; other symbols are removed.</li>
     * <li>Calculating the length of the longest common subsequence and dividing it by the length of the longer
     * string.</li>
     * </ul>
     *
     * @param strA The first string.
     * @param strB The second string.
     * @return The similarity score, a double value between 0 and 1.
     */
    public static double similar(final String strA, final String strB) {
        final String newStrA;
        final String newStrB;
        if (strA.length() < strB.length()) {
            newStrA = removeSign(strB);
            newStrB = removeSign(strA);
        } else {
            newStrA = removeSign(strA);
            newStrB = removeSign(strB);
        }

        // Use the length of the longer string as the denominator, and the common subsequence as the numerator to
        // calculate similarity.
        final int temp = Math.max(newStrA.length(), newStrB.length());
        if (0 == temp) {
            // If both are empty strings, similarity is 1, considered identical.
            return 1;
        }

        final int commonLength = longestCommonSubstringLength(newStrA, newStrB);
        return MathKit.div(commonLength, temp).doubleValue();
    }

    /**
     * Calculates the similarity percentage between two strings using the Levenshtein distance algorithm.
     *
     * @param strA  The first string.
     * @param strB  The second string.
     * @param scale The number of decimal places to retain for the percentage.
     * @return The similarity as a formatted percentage string.
     */
    public static String similar(final String strA, final String strB, final int scale) {
        return MathKit.formatPercent(similar(strA, strB), scale);
    }

    /**
     * Finds the longest common subsequence between two strings using a dynamic programming algorithm. This method does
     * not require the characters in the subsequence to be contiguous in the original strings. Algorithm explanation:
     * <a href=
     * "https://leetcode-cn.com/problems/longest-common-subsequence/solution/zui-chang-gong-gong-zi-xu-lie-by-leetcod-y7u0/">Longest
     * Common Subsequence</a>
     *
     * @param strA The first string.
     * @param strB The second string.
     * @return The longest common subsequence as a string.
     */
    public static String longestCommonSubstring(final String strA, final String strB) {
        // Initialize matrix data. matrix[0][0] is 0. If corresponding characters in chars_strA and chars_strB are the
        // same,
        // matrix[i][j] is the value of the top-left cell + 1. Otherwise, matrix[i][j] is the maximum of the values
        // from the cell directly above and the cell directly to the left. Other matrix values are 0.
        final int[][] matrix = generateMatrix(strA, strB);

        int m = strA.length();
        int n = strB.length();
        // In the matrix, if matrix[m][n] is not equal to matrix[m-1][n] and not equal to matrix[m][n-1],
        // then the character corresponding to matrix[m][n] is a common character and is stored in the result array.
        final char[] result = new char[matrix[m][n]];
        int currentIndex = result.length - 1;
        while (matrix[m][n] != 0) {
            if (matrix[m][n] == matrix[m][n - 1]) {
                n--;
            } else if (matrix[m][n] == matrix[m - 1][n]) {
                m--;
            } else {
                result[currentIndex] = strA.charAt(m - 1);
                currentIndex--;
                n--;
                m--;
            }
        }
        return new String(result);
    }

    /**
     * Removes non-alphanumeric and non-Chinese characters from a string. This method iterates through the input string
     * and appends only valid characters (Chinese characters, digits, or letters) to a new string builder.
     *
     * @param text The input string.
     * @return The processed string with only valid characters.
     */
    private static String removeSign(final String text) {
        final int length = text.length();
        final StringBuilder sb = StringKit.builder(length);
        // Iterate through the string. If a character is a Chinese character, digit, or letter, append it to sb.
        char c;
        for (int i = 0; i < length; i++) {
            c = text.charAt(i);
            if (isValidChar(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Checks if a character is a valid character for similarity comparison. Valid characters include Chinese
     * characters, digits, and letters. Symbols are not considered as they have no practical meaning in similarity
     * comparison.
     *
     * @param charValue The character to check.
     * @return {@code true} if the character is a Chinese character, digit, or letter; {@code false} otherwise.
     */
    private static boolean isValidChar(final char charValue) {
        return (charValue >= 0x4E00 && charValue <= 0X9FFF) || // Chinese characters
                (charValue >= 'a' && charValue <= 'z') || // Lowercase letters
                (charValue >= 'A' && charValue <= 'Z') || // Uppercase letters
                (charValue >= '0' && charValue <= '9'); // Digits
    }

    /**
     * Calculates the length of the longest common subsequence between two strings using a dynamic programming
     * algorithm. This method does not require the characters in the subsequence to be contiguous in the original
     * strings. Optimized on 2023-04-06 to reduce heap memory usage, as only the bottom-right value of the matrix is
     * needed.
     *
     * @param strA The first string.
     * @param strB The second string.
     * @return The length of the longest common subsequence.
     */
    public static int longestCommonSubstringLength(final String strA, final String strB) {
        final int m = strA.length();
        final int n = strB.length();

        // Initialize matrix data. matrix[0][0] is 0. If corresponding characters in chars_strA and chars_strB are the
        // same,
        // matrix[i][j] is the value of the top-left cell + 1. Otherwise, matrix[i][j] is the maximum of the values
        // from the cell directly above and the cell directly to the left. Other matrix values are 0.
        int[] lastLine = new int[n + 1];
        int[] currLine = new int[n + 1];
        int[] temp;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (strA.charAt(i - 1) == strB.charAt(j - 1)) {
                    currLine[j] = lastLine[j - 1] + 1;
                } else {
                    currLine[j] = Math.max(currLine[j - 1], lastLine[j]);
                }
            }
            temp = lastLine;
            lastLine = currLine;
            currLine = temp;
        }

        return lastLine[n];
    }

    /**
     * Generates the dynamic programming matrix for calculating the longest common subsequence. This method does not
     * require the characters in the subsequence to be contiguous in the original strings.
     *
     * @param strA The first string.
     * @param strB The second string.
     * @return A 2D integer array representing the common subsequence matrix.
     */
    private static int[][] generateMatrix(final String strA, final String strB) {
        final int m = strA.length();
        final int n = strB.length();

        // Initialize matrix data. matrix[0][0] is 0. If corresponding characters in chars_strA and chars_strB are the
        // same,
        // matrix[i][j] is the value of the top-left cell + 1. Otherwise, matrix[i][j] is the maximum of the values
        // from the cell directly above and the cell directly to the left. Other matrix values are 0.
        final int[][] matrix = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (strA.charAt(i - 1) == strB.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                } else {
                    matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
                }
            }
        }

        return matrix;
    }

}

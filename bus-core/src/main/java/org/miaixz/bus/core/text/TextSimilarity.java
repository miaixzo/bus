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
package org.miaixz.bus.core.text;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Provides longest common subsequence (LCS) matching, raw scores and normalized similarity.
 * {@link #match(String, String)} returns one LCS and {@link #score(String, String)} returns its length. Both compare
 * unfiltered UTF-16 code units without Unicode normalization or code point/grapheme grouping. The {@code similar}
 * methods first retain only ASCII letters, ASCII digits and characters in U+4E00-U+9FFF, then normalize the LCS score
 * of the filtered inputs. {@link #filter(String)} exposes this preprocessing rule, and {@link #accepts(char)} checks
 * whether an individual UTF-16 code unit is retained.
 *
 * @author Kimi Liu
 */
public class TextSimilarity {

    /**
     * Creates a text-similarity calculator.
     */
    public TextSimilarity() {
        // No initialization required.
    }

    /**
     * Tests whether a single UTF-16 code unit is retained by {@link #filter(String)}. Accepted ranges are ASCII A-Z,
     * ASCII a-z, ASCII 0-9 and the inclusive interval U+4E00-U+9FFF. This predicate checks the fixed filtering rule; it
     * does not validate whether an arbitrary Unicode character is legal or accept every letter, digit or Han character.
     * Supplementary characters require two UTF-16 code units, and neither surrogate code unit is accepted.
     *
     * @param character The UTF-16 code unit to check.
     * @return {@code true} if the code unit belongs to one of the accepted ranges; {@code false} otherwise.
     */
    public static boolean accepts(final char character) {
        return (character >= 0x4E00 && character <= 0X9FFF) || // Basic CJK range used by the legacy filter
                (character >= 'a' && character <= 'z') || // ASCII lowercase letters
                (character >= 'A' && character <= 'Z') || // ASCII uppercase letters
                (character >= Symbol.C_ZERO && character <= Symbol.C_NINE); // ASCII digits
    }

    /**
     * Retains exactly the UTF-16 code units accepted by {@link #accepts(char)}: ASCII letters A-Z/a-z, digits 0-9 and
     * U+4E00-U+9FFF. Order and case are preserved; no Unicode normalization or case folding is performed. All other
     * code units are removed, including accented letters, supplementary characters and symbols. An empty input or an
     * input with no accepted code units produces an empty string.
     * <p>
     * This is the preprocessing used by {@link #similar(String, String)}. {@link #match(String, String)} and
     * {@link #score(String, String)} compare their inputs directly and do not apply this filter automatically.
     *
     * @param text The non-null input string.
     * @return The filtered string, possibly empty.
     * @throws NullPointerException if the input is null
     */
    public static String filter(final String text) {
        final int textLength = text.length();
        final StringBuilder filteredText = StringKit.builder(textLength);
        // Apply the fixed legacy ranges without Unicode normalization or case folding.
        char character;
        for (int index = 0; index < textLength; index++) {
            character = text.charAt(index);
            if (accepts(character)) {
                filteredText.append(character);
            }
        }

        return filteredText.toString();
    }

    /**
     * Calculates the raw longest common subsequence (LCS) score using two rolling rows. Each matched UTF-16 code unit
     * contributes one point; matches need not be contiguous. The score equals the LCS length and is not normalized.
     * Inputs are unfiltered and compared case-sensitively, including individual surrogate code units. Unlike this
     * method, {@link #similar(String, String)} filters its inputs before scoring and normalizes the result. An empty
     * input produces zero. For non-empty inputs of lengths m and n, the computation takes O(m*n) time; each working row
     * has {@code secondText.length() + 1} integers. The inputs are not reordered to minimize row size.
     *
     * @param firstText  The first non-null string.
     * @param secondText The second non-null string.
     * @return The raw LCS score, equal to the matched UTF-16 code unit count.
     * @throws NullPointerException if either input is null
     */
    public static int score(final String firstText, final String secondText) {
        final int firstLength = firstText.length();
        final int secondLength = secondText.length();

        // Matching code units extend the diagonal LCS by one; other cells take the larger left/upper value.
        // Column zero stays zero, and the completed current row becomes the previous row after each iteration.
        int[] previousRow = new int[secondLength + 1];
        int[] currentRow = new int[secondLength + 1];
        int[] reusableRow;
        for (int rowIndex = 1; rowIndex <= firstLength; rowIndex++) {
            for (int columnIndex = 1; columnIndex <= secondLength; columnIndex++) {
                if (firstText.charAt(rowIndex - 1) == secondText.charAt(columnIndex - 1)) {
                    currentRow[columnIndex] = previousRow[columnIndex - 1] + 1;
                } else {
                    currentRow[columnIndex] = Math.max(currentRow[columnIndex - 1], previousRow[columnIndex]);
                }
            }
            reusableRow = previousRow;
            previousRow = currentRow;
            currentRow = reusableRow;
        }

        return previousRow[secondLength];
    }

    /**
     * Returns one longest common subsequence using a full dynamic-programming matrix and backtracking. Matches need not
     * be contiguous. Inputs are unfiltered and compared case-sensitively as UTF-16 code units, so parts of surrogate
     * pairs may match independently. An empty input or no common code units produces an empty result. When several
     * longest subsequences exist, this method returns one of them. For non-empty inputs of lengths m and n, matrix
     * construction takes O(m*n) time and space.
     *
     * @param firstText  The first non-null string.
     * @param secondText The second non-null string.
     * @return One longest common subsequence as a string.
     * @throws NullPointerException if either input is null
     */
    public static String match(final String firstText, final String secondText) {
        // Rows and columns index prefixes of firstText and secondText; the empty-prefix row and column are zero.
        final int[][] lengthMatrix = matrix(firstText, secondText);

        int rowIndex = firstText.length();
        int columnIndex = secondText.length();
        // Backtrack from the full prefixes. Skip equal-valued neighbors; otherwise record the matching code unit
        // and move diagonally. Fill the matched-character buffer from right to left.
        final char[] matchedChars = new char[lengthMatrix[rowIndex][columnIndex]];
        int writeIndex = matchedChars.length - 1;
        while (lengthMatrix[rowIndex][columnIndex] != 0) {
            if (lengthMatrix[rowIndex][columnIndex] == lengthMatrix[rowIndex][columnIndex - 1]) {
                columnIndex--;
            } else if (lengthMatrix[rowIndex][columnIndex] == lengthMatrix[rowIndex - 1][columnIndex]) {
                rowIndex--;
            } else {
                matchedChars[writeIndex] = firstText.charAt(rowIndex - 1);
                writeIndex--;
                columnIndex--;
                rowIndex--;
            }
        }
        return new String(matchedChars);
    }

    /**
     * Calculates LCS similarity after applying {@link #filter(String)}. Comparison is case-sensitive. The method:
     * <ul>
     * <li>Retains only ASCII letters A-Z/a-z, digits 0-9 and characters in U+4E00-U+9FFF.</li>
     * <li>Divides the LCS length by the longer filtered string's length.</li>
     * <li>Returns 1 when both filtered strings are empty, even if the original inputs differ; returns 0 when only one
     * filtered string is empty.</li>
     * </ul>
     * The quotient is rounded to 10 decimal places using HALF_UP before conversion to double.
     *
     * @param firstText  The first non-null string.
     * @param secondText The second non-null string.
     * @return The similarity score, a double value between 0 and 1.
     * @throws NullPointerException if either input is null
     */
    public static double similar(final String firstText, final String secondText) {
        final String filteredRowText;
        final String filteredColumnText;
        // Choose DP row/column inputs by original length; filtering may change their relative lengths.
        if (firstText.length() < secondText.length()) {
            filteredRowText = filter(secondText);
            filteredColumnText = filter(firstText);
        } else {
            filteredRowText = filter(firstText);
            filteredColumnText = filter(secondText);
        }

        // Normalize the LCS length by the longer filtered input's length.
        final int maxFilteredLength = Math.max(filteredRowText.length(), filteredColumnText.length());
        if (0 == maxFilteredLength) {
            // Both filtered inputs are empty; this also covers non-empty inputs containing only removed characters.
            return 1;
        }

        final int rawScore = score(filteredRowText, filteredColumnText);
        return MathKit.div(rawScore, maxFilteredLength).doubleValue();
    }

    /**
     * Formats {@link #similar(String, String)} as a percentage using the default FORMAT locale. The same filtering,
     * case sensitivity, null rejection and score-rounding rules apply. Trailing zeroes are not forced.
     *
     * @param firstText         The first non-null string.
     * @param secondText        The second non-null string.
     * @param maxFractionDigits The maximum number of fractional digits in the formatted percentage; negative values act
     *                          as zero.
     * @return The similarity as a formatted percentage string.
     * @throws NullPointerException if either input is null
     */
    public static String similar(final String firstText, final String secondText, final int maxFractionDigits) {
        return MathKit.formatPercent(similar(firstText, secondText), maxFractionDigits);
    }

    /**
     * Builds the unfiltered UTF-16 LCS matrix. Rows index prefixes of firstText and columns index prefixes of
     * secondText; each entry is the LCS length for those prefixes. The first row and column are zero; subsequence
     * matches need not be contiguous.
     *
     * @param firstText  The first string.
     * @param secondText The second string.
     * @return An {@code (firstText.length() + 1) x (secondText.length() + 1)} prefix-length matrix.
     */
    private static int[][] matrix(final String firstText, final String secondText) {
        final int firstLength = firstText.length();
        final int secondLength = secondText.length();

        // Java zero-initializes the empty-prefix row and column; fill the remaining cells using the LCS recurrence.
        final int[][] lengthMatrix = new int[firstLength + 1][secondLength + 1];
        for (int rowIndex = 1; rowIndex <= firstLength; rowIndex++) {
            for (int columnIndex = 1; columnIndex <= secondLength; columnIndex++) {
                if (firstText.charAt(rowIndex - 1) == secondText.charAt(columnIndex - 1)) {
                    lengthMatrix[rowIndex][columnIndex] = lengthMatrix[rowIndex - 1][columnIndex - 1] + 1;
                } else {
                    lengthMatrix[rowIndex][columnIndex] = Math
                            .max(lengthMatrix[rowIndex][columnIndex - 1], lengthMatrix[rowIndex - 1][columnIndex]);
                }
            }
        }

        return lengthMatrix;
    }

}

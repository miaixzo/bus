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
package org.miaixz.bus.core.text.dfa;

import org.miaixz.bus.core.xyz.SetKit;

import java.util.Set;

/**
 * 过滤词及一些简单处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StopChar {

    /**
     * 不需要处理的词，如标点符号、空格等
     */
    public static final Set<Character> STOP_WORD = SetKit.of(' ', '\'', '、', '。', '·', 'ˉ', 'ˇ', '々', '—', '～', '‖',
            '…', '‘', '’', '“', '”', '〔', '〕', '〈', '〉', '《', '》', '「', '」', '『', //
            '』', '〖', '〗', '【', '】', '±', '＋', '－', '×', '÷', '∧', '∨', '∑', '∏', '∪', '∩', '∈', '√', '⊥', '⊙', '∫', //
            '∮', '≡', '≌', '≈', '∽', '∝', '≠', '≮', '≯', '≤', '≥', '∞', '∶', '∵', '∴', '∷', '♂', '♀', '°', '′', '〃', //
            '℃', '＄', '¤', '￠', '￡', '‰', '§', '☆', '★', '〇', '○', '●', '◎', '◇', '◆', '□', '■', '△', '▽', '⊿', '▲', //
            '▼', '◣', '◤', '◢', '◥', '▁', '▂', '▃', '▄', '▅', '▆', '▇', '█', '▉', '▊', '▋', '▌', '▍', '▎', '▏', '▓', //
            '※', '→', '←', '↑', '↓', '↖', '↗', '↘', '↙', '〓', 'ⅰ', 'ⅱ', 'ⅲ', 'ⅳ', 'ⅴ', 'ⅵ', 'ⅶ', 'ⅷ', 'ⅸ', 'ⅹ', '①', //
            '②', '③', '④', '⑤', '⑥', '⑦', '⑧', '⑨', '⑩', '⒈', '⒉', '⒊', '⒋', '⒌', '⒍', '⒎', '⒏', '⒐', '⒑', '⒒', '⒓', //
            '⒔', '⒕', '⒖', '⒗', '⒘', '⒙', '⒚', '⒛', '⑴', '⑵', '⑶', '⑷', '⑸', '⑹', '⑺', '⑻', '⑼', '⑽', '⑾', '⑿', '⒀', //
            '⒁', '⒂', '⒃', '⒄', '⒅', '⒆', '⒇', 'Ⅰ', 'Ⅱ', 'Ⅲ', 'Ⅳ', 'Ⅴ', 'Ⅵ', 'Ⅶ', 'Ⅷ', 'Ⅸ', 'Ⅹ', 'Ⅺ', 'Ⅻ', '！', '”', //
            '＃', '￥', '％', '＆', '’', '（', '）', '＊', '＋', '，', '－', '．', '／', '０', '１', '２', '３', '４', '５', '６', '７', //
            '８', '９', '：', '；', '＜', '＝', '＞', '？', '＠', '〔', '＼', '〕', '＾', '＿', '‘', '｛', '｜', '｝', '∏', 'Ρ', '∑', //
            'Υ', 'Φ', 'Χ', 'Ψ', 'Ω', 'α', 'β', 'γ', 'δ', 'ε', 'ζ', 'η', 'θ', 'ι', 'κ', 'λ', 'μ', 'ν', 'ξ', 'ο', 'π', //
            'ρ', 'σ', 'τ', 'υ', 'φ', 'χ', 'ψ', 'ω', '（', '）', '〔', '〕', '＾', '﹊', '﹍', '╭', '╮', '╰', '╯', '', '_', //
            '', '^', '（', '^', '：', '！', '/', '\\', '\"', '<', '>', '`', '·', '。', '{', '}', '~', '～', '(', ')', '-', //
            '√', '$', '@', '*', '&', '#', '卐', '㎎', '㎏', '㎜', '㎝', '㎞', '㎡', '㏄', '㏎', '㏑', '㏒', '㏕', '+', '=', '?',
            ':', '.', '!', ';', ']', '|', '%');

    /**
     * 判断指定的词是否是不处理的词。 如果参数为空，则返回true，因为空也属于不处理的字符。
     *
     * @param ch 指定的词
     * @return 是否是不处理的词
     */
    public static boolean isStopChar(final char ch) {
        return Character.isWhitespace(ch) || STOP_WORD.contains(ch);
    }

    /**
     * 是否为合法字符（待处理字符）
     *
     * @param ch 指定的词
     * @return 是否为合法字符（待处理字符）
     */
    public static boolean isNotStopChar(final char ch) {
        return !isStopChar(ch);
    }

}

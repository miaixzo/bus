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
package org.miaixz.bus.extra.nlp.provider.mynlp;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Sentence;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.extra.nlp.NLPProvider;
import org.miaixz.bus.extra.nlp.NLPResult;

/**
 * MYNLP 中文NLP工具包分词实现
 * 项目地址：https://github.com/mayabot/mynlp/
 * {@link Lexer} 线程安全
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MynlpProvider implements NLPProvider {

    private final Lexer lexer;

    /**
     * 构造
     */
    public MynlpProvider() {
        // CORE分词器构建器
        // 开启词性标注功能
        // 开启人名识别功能
        this.lexer = Mynlp.instance().bigramLexer();
    }

    /**
     * 构造
     *
     * @param lexer 分词器接口{@link Lexer}
     */
    public MynlpProvider(final Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public NLPResult parse(final CharSequence text) {
        final Sentence sentence = this.lexer.scan(StringKit.toString(text));
        return new MynlpResult(sentence);
    }

}

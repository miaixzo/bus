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
/**
 * bus.extra
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.extra {

    requires java.desktop;

    requires bus.core;
    requires bus.logger;
    requires bus.setting;

    requires static lombok;
    requires static com.alibaba.fastjson2;
    requires static com.fasterxml.jackson.core;
    requires static com.fasterxml.jackson.databind;
    requires static com.google.gson;
    requires static jpinyin;
    requires static pinyin4j;
    requires static TinyPinyin;
    requires static bopomofo4j;
    requires static pinyin;
    requires static com.jcraft.jsch;
    requires static com.hierynomus.sshj;
    requires static com.google.zxing;
    requires static org.apache.commons.net;
    requires static ftpserver.core;
    requires static ftplet.api;
    requires static emoji.java;
    requires static org.apache.commons.compress;
    requires static jakarta.mail;
    requires static beetl;
    requires static freemarker;
    requires static thymeleaf;
    requires static ansj.seg;
    requires static jieba.analysis;
    requires static jcseg.core;
    requires static mmseg4j.core;
    requires static hanlp.portable;
    requires static lucene.core;
    requires static lucene.analyzers.smartcn;
    requires static word;
    requires static mynlp;
    requires static org.apache.logging.log4j;
    requires static beetl.core;

    exports org.miaixz.bus.extra.captcha;
    exports org.miaixz.bus.extra.captcha.provider;
    exports org.miaixz.bus.extra.captcha.strategy;
    exports org.miaixz.bus.extra.compress;
    exports org.miaixz.bus.extra.compress.archiver;
    exports org.miaixz.bus.extra.compress.extractor;
    exports org.miaixz.bus.extra.emoji;
    exports org.miaixz.bus.extra.ftp;
    exports org.miaixz.bus.extra.image;
    exports org.miaixz.bus.extra.image.gif;
    exports org.miaixz.bus.extra.json;
    exports org.miaixz.bus.extra.json.provider;
    exports org.miaixz.bus.extra.mail;
    exports org.miaixz.bus.extra.nlp;
    exports org.miaixz.bus.extra.nlp.provider.analysis;
    exports org.miaixz.bus.extra.nlp.provider.ansj;
    exports org.miaixz.bus.extra.nlp.provider.hanlp;
    exports org.miaixz.bus.extra.nlp.provider.jcseg;
    exports org.miaixz.bus.extra.nlp.provider.jieba;
    exports org.miaixz.bus.extra.nlp.provider.mmseg;
    exports org.miaixz.bus.extra.nlp.provider.mynlp;
    exports org.miaixz.bus.extra.nlp.provider.word;
    exports org.miaixz.bus.extra.pinyin;
    exports org.miaixz.bus.extra.pinyin.provider.bopomofo4j;
    exports org.miaixz.bus.extra.pinyin.provider.houbb;
    exports org.miaixz.bus.extra.pinyin.provider.jpinyin;
    exports org.miaixz.bus.extra.pinyin.provider.pinyin4j;
    exports org.miaixz.bus.extra.pinyin.provider.tinypinyin;
    exports org.miaixz.bus.extra.qrcode;
    exports org.miaixz.bus.extra.qrcode.render;
    exports org.miaixz.bus.extra.ssh;
    exports org.miaixz.bus.extra.ssh.provider.jsch;
    exports org.miaixz.bus.extra.ssh.provider.sshj;
    exports org.miaixz.bus.extra.template;
    exports org.miaixz.bus.extra.template.provider.beetl;
    exports org.miaixz.bus.extra.template.provider.freemarker;
    exports org.miaixz.bus.extra.template.provider.thymeleaf;

}
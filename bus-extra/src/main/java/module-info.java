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
/**
 * Module: {@code bus.extra}
 *
 * <p>
 * Provides optional utilities and integrations that extend the core Bus libraries.
 *
 * <p>
 * Includes captchas, archive processing, emoji handling, FTP and SSH clients, image helpers, JSON adapters, mail,
 * message queues, natural-language processing, pinyin conversion, QR codes, and template engines.
 *
 * @author Kimi Liu
 */
module bus.extra {

    requires java.desktop;

    requires bus.core;
    requires bus.logger;
    requires bus.setting;

    requires static activemq.client;
    requires static ansj.seg;
    requires static beetl.core;
    requires static bopomofo4j;
    requires static com.alibaba.fastjson2;
    requires static com.fasterxml.jackson.annotation;
    requires static com.google.gson;
    requires static com.google.zxing;
    requires static com.hierynomus.sshj;
    requires static com.jcraft.jsch;
    requires static com.rabbitmq.client;
    requires static emoji.java;
    requires static freemarker;
    requires static ftplet.api;
    requires static ftpserver.core;
    requires static hanlp.portable;
    requires static jakarta.activation;
    requires static jakarta.mail;
    requires static jakarta.messaging;
    requires static jcseg.core;
    requires static jieba.analysis;
    requires static jpinyin;
    requires static kafka.clients;
    requires static lombok;
    requires static lucene.analyzers.smartcn;
    requires static lucene.core;
    requires static mmseg4j.core;
    requires static mynlp;
    requires static org.apache.commons.compress;
    requires static org.apache.commons.io;
    requires static org.apache.commons.net;
    requires static org.apache.logging.log4j;
    requires static pinyin;
    requires static pinyin4j;
    requires static rocketmq.client;
    requires static rocketmq.common;
    requires static thymeleaf;
    requires static TinyPinyin;
    requires static tools.jackson.core;
    requires static tools.jackson.databind;
    requires static word;

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
    exports org.miaixz.bus.extra.mq;
    exports org.miaixz.bus.extra.mq.provider.activemq;
    exports org.miaixz.bus.extra.mq.provider.jms;
    exports org.miaixz.bus.extra.mq.provider.kafka;
    exports org.miaixz.bus.extra.mq.provider.rabbitmq;
    exports org.miaixz.bus.extra.mq.provider.rocketmq;
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

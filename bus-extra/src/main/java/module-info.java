/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
/**
 * Defines the bus.extra module, providing extended utilities and integrations.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.extra {

    requires java.desktop;

    requires bus.core;
    requires bus.logger;
    requires bus.setting;

    requires lombok;
    requires com.alibaba.fastjson2;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires jpinyin;
    requires pinyin4j;
    requires TinyPinyin;
    requires bopomofo4j;
    requires pinyin;
    requires com.jcraft.jsch;
    requires com.hierynomus.sshj;
    requires com.google.zxing;
    requires org.apache.commons.net;
    requires ftpserver.core;
    requires ftplet.api;
    requires emoji.java;
    requires org.apache.commons.compress;
    requires jakarta.mail;
    requires beetl;
    requires freemarker;
    requires thymeleaf;
    requires ansj.seg;
    requires jieba.analysis;
    requires jcseg.core;
    requires mmseg4j.core;
    requires hanlp.portable;
    requires lucene.core;
    requires lucene.analyzers.smartcn;
    requires word;
    requires mynlp;
    requires org.apache.logging.log4j;
    requires beetl.core;
    requires jakarta.messaging;
    requires activemq.client;
    requires kafka.clients;
    requires com.rabbitmq.client;
    requires rocketmq.client;
    requires rocketmq.common;

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

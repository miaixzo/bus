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
 * bus.core
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.core {

    requires java.base;
    requires java.sql;
    requires java.naming;
    requires java.compiler;
    requires java.desktop;
    requires java.management;

    requires lombok;
    requires jakarta.persistence;

    exports org.miaixz.bus.core;
    exports org.miaixz.bus.core.basic.advice;
    exports org.miaixz.bus.core.basic.entity;
    exports org.miaixz.bus.core.basic.normal;
    exports org.miaixz.bus.core.basic.service;
    exports org.miaixz.bus.core.basic.spring;
    exports org.miaixz.bus.core.bean;
    exports org.miaixz.bus.core.bean.copier;
    exports org.miaixz.bus.core.bean.copier.provider;
    exports org.miaixz.bus.core.bean.desc;
    exports org.miaixz.bus.core.bean.path;
    exports org.miaixz.bus.core.bean.path.node;
    exports org.miaixz.bus.core.builder;
    exports org.miaixz.bus.core.cache;
    exports org.miaixz.bus.core.cache.file;
    exports org.miaixz.bus.core.cache.provider;
    exports org.miaixz.bus.core.center;
    exports org.miaixz.bus.core.center.array;
    exports org.miaixz.bus.core.center.date;
    exports org.miaixz.bus.core.center.date.builder;
    exports org.miaixz.bus.core.center.date.culture;
    exports org.miaixz.bus.core.center.date.culture.cn;
    exports org.miaixz.bus.core.center.date.culture.cn.climate;
    exports org.miaixz.bus.core.center.date.culture.cn.dog;
    exports org.miaixz.bus.core.center.date.culture.cn.eightchar;
    exports org.miaixz.bus.core.center.date.culture.cn.eightchar.provider;
    exports org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.impl;
    exports org.miaixz.bus.core.center.date.culture.cn.fetus;
    exports org.miaixz.bus.core.center.date.culture.cn.minor;
    exports org.miaixz.bus.core.center.date.culture.cn.nine;
    exports org.miaixz.bus.core.center.date.culture.cn.plumrain;
    exports org.miaixz.bus.core.center.date.culture.cn.ren;
    exports org.miaixz.bus.core.center.date.culture.cn.sixty;
    exports org.miaixz.bus.core.center.date.culture.cn.star.nine;
    exports org.miaixz.bus.core.center.date.culture.cn.star.seven;
    exports org.miaixz.bus.core.center.date.culture.cn.star.six;
    exports org.miaixz.bus.core.center.date.culture.cn.star.ten;
    exports org.miaixz.bus.core.center.date.culture.cn.star.twelve;
    exports org.miaixz.bus.core.center.date.culture.cn.star.twentyeight;
    exports org.miaixz.bus.core.center.date.culture.en;
    exports org.miaixz.bus.core.center.date.culture.lunar;
    exports org.miaixz.bus.core.center.date.culture.solar;
    exports org.miaixz.bus.core.center.date.format;
    exports org.miaixz.bus.core.center.date.format.parser;
    exports org.miaixz.bus.core.center.date.printer;
    exports org.miaixz.bus.core.center.function;
    exports org.miaixz.bus.core.center.iterator;
    exports org.miaixz.bus.core.center.list;
    exports org.miaixz.bus.core.center.map;
    exports org.miaixz.bus.core.center.map.concurrent;
    exports org.miaixz.bus.core.center.map.multi;
    exports org.miaixz.bus.core.center.map.reference;
    exports org.miaixz.bus.core.center.object;
    exports org.miaixz.bus.core.center.queue;
    exports org.miaixz.bus.core.center.regex;
    exports org.miaixz.bus.core.center.set;
    exports org.miaixz.bus.core.center.stream;
    exports org.miaixz.bus.core.center.stream.spliterators;
    exports org.miaixz.bus.core.codec;
    exports org.miaixz.bus.core.codec.binary;
    exports org.miaixz.bus.core.codec.binary.decoder;
    exports org.miaixz.bus.core.codec.binary.encoder;
    exports org.miaixz.bus.core.codec.binary.provider;
    exports org.miaixz.bus.core.codec.hash;
    exports org.miaixz.bus.core.codec.hash.metro;
    exports org.miaixz.bus.core.compare;
    exports org.miaixz.bus.core.convert;
    exports org.miaixz.bus.core.convert.stringer;
    exports org.miaixz.bus.core.data;
    exports org.miaixz.bus.core.data.id;
    exports org.miaixz.bus.core.data.masking;
    exports org.miaixz.bus.core.instance;
    exports org.miaixz.bus.core.io;
    exports org.miaixz.bus.core.io.buffer;
    exports org.miaixz.bus.core.io.check;
    exports org.miaixz.bus.core.io.check.crc16;
    exports org.miaixz.bus.core.io.compress;
    exports org.miaixz.bus.core.io.copier;
    exports org.miaixz.bus.core.io.file;
    exports org.miaixz.bus.core.io.file.visitor;
    exports org.miaixz.bus.core.io.resource;
    exports org.miaixz.bus.core.io.sink;
    exports org.miaixz.bus.core.io.source;
    exports org.miaixz.bus.core.io.stream;
    exports org.miaixz.bus.core.io.timout;
    exports org.miaixz.bus.core.io.unit;
    exports org.miaixz.bus.core.io.watch;
    exports org.miaixz.bus.core.lang;
    exports org.miaixz.bus.core.lang.annotation;
    exports org.miaixz.bus.core.lang.annotation.env;
    exports org.miaixz.bus.core.lang.annotation.resolve;
    exports org.miaixz.bus.core.lang.annotation.resolve.elements;
    exports org.miaixz.bus.core.lang.ansi;
    exports org.miaixz.bus.core.lang.caller;
    exports org.miaixz.bus.core.lang.copier;
    exports org.miaixz.bus.core.lang.event;
    exports org.miaixz.bus.core.lang.exception;
    exports org.miaixz.bus.core.lang.getter;
    exports org.miaixz.bus.core.lang.intern;
    exports org.miaixz.bus.core.lang.loader;
    exports org.miaixz.bus.core.lang.loader.classloader;
    exports org.miaixz.bus.core.lang.loader.spi;
    exports org.miaixz.bus.core.lang.mutable;
    exports org.miaixz.bus.core.lang.pool;
    exports org.miaixz.bus.core.lang.pool.partition;
    exports org.miaixz.bus.core.lang.range;
    exports org.miaixz.bus.core.lang.ref;
    exports org.miaixz.bus.core.lang.reflect;
    exports org.miaixz.bus.core.lang.reflect.creator;
    exports org.miaixz.bus.core.lang.reflect.field;
    exports org.miaixz.bus.core.lang.reflect.kotlin;
    exports org.miaixz.bus.core.lang.reflect.lookup;
    exports org.miaixz.bus.core.lang.reflect.method;
    exports org.miaixz.bus.core.lang.selector;
    exports org.miaixz.bus.core.lang.thread;
    exports org.miaixz.bus.core.lang.thread.lock;
    exports org.miaixz.bus.core.lang.thread.threadlocal;
    exports org.miaixz.bus.core.lang.tuple;
    exports org.miaixz.bus.core.lang.wrapper;
    exports org.miaixz.bus.core.math;
    exports org.miaixz.bus.core.net;
    exports org.miaixz.bus.core.net.ip;
    exports org.miaixz.bus.core.net.tls;
    exports org.miaixz.bus.core.net.url;
    exports org.miaixz.bus.core.text;
    exports org.miaixz.bus.core.text.bloom;
    exports org.miaixz.bus.core.text.dfa;
    exports org.miaixz.bus.core.text.escape;
    exports org.miaixz.bus.core.text.finder;
    exports org.miaixz.bus.core.text.placeholder;
    exports org.miaixz.bus.core.text.placeholder.segment;
    exports org.miaixz.bus.core.text.placeholder.template;
    exports org.miaixz.bus.core.text.replacer;
    exports org.miaixz.bus.core.tree;
    exports org.miaixz.bus.core.tree.parser;
    exports org.miaixz.bus.core.xml;
    exports org.miaixz.bus.core.xyz;

}

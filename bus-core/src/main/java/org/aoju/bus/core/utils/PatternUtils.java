/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.RegEx;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用正则表达式集合
 *
 * @author Kimi Liu
 * @version 3.5.3
 * @since JDK 1.8
 */
public class PatternUtils {

    private static final Map<RegexWithFlag, Pattern> cache = new WeakHashMap<>();
    private static final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock readLock = cacheLock.readLock();
    private static final ReentrantReadWriteLock.WriteLock writeLock = cacheLock.writeLock();

    /**
     * 先从Pattern池中查找正则对应的{@link Pattern}，找不到则编译正则表达式并入池。
     *
     * @param regex 正则表达式
     * @return {@link Pattern}
     */
    public static Pattern get(String regex) {
        return get(regex, 0);
    }

    /**
     * 先从Pattern池中查找正则对应的{@link Pattern}，找不到则编译正则表达式并入池。
     *
     * @param regex 正则表达式
     * @param flags 正则标识位集合 {@link Pattern}
     * @return {@link Pattern}
     */
    public static Pattern get(String regex, int flags) {
        final RegexWithFlag regexWithFlag = new RegexWithFlag(regex, flags);
        Pattern pattern = isGet(regexWithFlag);
        if (null == pattern) {
            pattern = Pattern.compile(regex, flags);
            isPut(regexWithFlag, pattern);
        }
        return pattern;
    }

    /**
     * 获得匹配的字符串，获得正则中分组0的内容
     *
     * @param regex   匹配的正则
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串，未匹配返回null
     * @since 3.1.9
     */
    public static String getGroup0(String regex, String content) {
        return get(regex, content, 0);
    }

    /**
     * 获得匹配的字符串，获得正则中分组1的内容
     *
     * @param regex   匹配的正则
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串，未匹配返回null
     * @since 3.1.9
     */
    public static String getGroup1(String regex, String content) {
        return get(regex, content, 1);
    }

    /**
     * 获得匹配的字符串
     *
     * @param regex      匹配的正则
     * @param content    被匹配的内容
     * @param groupIndex 匹配正则的分组序号
     * @return 匹配后得到的字符串，未匹配返回null
     */
    public static String get(String regex, String content, int groupIndex) {
        if (null == content || null == regex) {
            return null;
        }
        final Pattern pattern = get(regex, Pattern.DOTALL);
        return get(pattern, content, groupIndex);
    }

    /**
     * 获得匹配的字符串，，获得正则中分组0的内容
     *
     * @param pattern 编译后的正则模式
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串，未匹配返回null
     * @since 3.1.9
     */
    public static String getGroup0(Pattern pattern, String content) {
        return get(pattern, content, 0);
    }

    /**
     * 获得匹配的字符串，，获得正则中分组1的内容
     *
     * @param pattern 编译后的正则模式
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串，未匹配返回null
     * @since 3.1.9
     */
    public static String getGroup1(Pattern pattern, String content) {
        return get(pattern, content, 1);
    }

    /**
     * 获得匹配的字符串
     *
     * @param pattern    编译后的正则模式
     * @param content    被匹配的内容
     * @param groupIndex 匹配正则的分组序号
     * @return 匹配后得到的字符串，未匹配返回null
     */
    public static String get(Pattern pattern, String content, int groupIndex) {
        if (null == content || null == pattern) {
            return null;
        }

        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(groupIndex);
        }
        return null;
    }

    /**
     * 获得匹配的字符串匹配到的所有分组
     *
     * @param pattern 编译后的正则模式
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串数组，按照分组顺序依次列出，未匹配到返回空列表，任何一个参数为null返回null
     * @since 3.1.9
     */
    public static List<String> getAllGroups(Pattern pattern, String content) {
        if (null == content || null == pattern) {
            return null;
        }

        ArrayList<String> result = new ArrayList<>();
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            int groupCount = matcher.groupCount();
            for (int i = 0; i < groupCount; i++) {
                result.add(matcher.group(i));
            }
        }
        return result;
    }

    /**
     * 从content中匹配出多个值并根据template生成新的字符串
     * 例如：
     * content 2013年5月 pattern (.*?)年(.*?)月 template： $1-$2 return 2013-5
     *
     * @param pattern  匹配正则
     * @param content  被匹配的内容
     * @param template 生成内容模板，变量 $1 表示group1的内容，以此类推
     * @return 新字符串
     */
    public static String extractMulti(Pattern pattern, String content, String template) {
        if (null == content || null == pattern || null == template) {
            return null;
        }

        HashSet<String> varNums = findAll(RegEx.GROUP_VAR, template, 1, new HashSet<>());

        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (String var : varNums) {
                int group = Integer.parseInt(var);
                template = template.replace("$" + var, matcher.group(group));
            }
            return template;
        }
        return null;
    }

    /**
     * 从content中匹配出多个值并根据template生成新的字符串
     * 匹配结束后会删除匹配内容之前的内容（包括匹配内容）
     * 例如：
     * content 2013年5月 pattern (.*?)年(.*?)月 template： $1-$2 return 2013-5
     *
     * @param regex    匹配正则字符串
     * @param content  被匹配的内容
     * @param template 生成内容模板，变量 $1 表示group1的内容，以此类推
     * @return 按照template拼接后的字符串
     */
    public static String extractMulti(String regex, String content, String template) {
        if (null == content || null == regex || null == template) {
            return null;
        }
        final Pattern pattern = get(regex, Pattern.DOTALL);
        return extractMulti(pattern, content, template);
    }

    /**
     * 删除匹配的第一个内容
     *
     * @param pattern 正则
     * @param content 被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delFirst(Pattern pattern, String content) {
        if (null == pattern || StringUtils.isBlank(content)) {
            return content;
        }

        return pattern.matcher(content).replaceFirst("");
    }

    /**
     * 删除匹配的全部内容
     *
     * @param pattern 正则
     * @param content 被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delAll(Pattern pattern, String content) {
        if (null == pattern || StringUtils.isBlank(content)) {
            return content;
        }

        return pattern.matcher(content).replaceAll("");
    }

    /**
     * 删除匹配的全部内容
     *
     * @param regex   正则
     * @param content 被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delAll(String regex, String content) {
        if (StringUtils.hasBlank(regex, content)) {
            return content;
        }

        // RegEx pattern = RegEx.compile(regex, RegEx.DOTALL);
        final Pattern pattern = get(regex, Pattern.DOTALL);
        return delAll(pattern, content);
    }

    /**
     * 删除正则匹配到的内容之前的字符 如果没有找到，则返回原文
     *
     * @param regex   定位正则
     * @param content 被查找的内容
     * @return 删除前缀后的新内容
     */
    public static String delPre(String regex, String content) {
        if (null == content || null == regex) {
            return content;
        }
        final Pattern pattern = get(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return StringUtils.sub(content, matcher.end(), content.length());
        }
        return content;
    }

    /**
     * 取得内容中匹配的所有结果，获得匹配的所有结果中正则对应分组0的内容
     *
     * @param regex   正则
     * @param content 被查找的内容
     * @return 结果列表
     * @since 3.1.9
     */
    public static List<String> findAllGroup0(String regex, String content) {
        return findAll(regex, content, 0);
    }

    /**
     * 取得内容中匹配的所有结果，获得匹配的所有结果中正则对应分组1的内容
     *
     * @param regex   正则
     * @param content 被查找的内容
     * @return 结果列表
     * @since 3.1.9
     */
    public static List<String> findAllGroup1(String regex, String content) {
        return findAll(regex, content, 1);
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param regex   正则
     * @param content 被查找的内容
     * @param group   正则的分组
     * @return 结果列表
     * @since 3.1.9
     */
    public static List<String> findAll(String regex, String content, int group) {
        return findAll(regex, content, group, new ArrayList<>());
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param <T>        集合类型
     * @param regex      正则
     * @param content    被查找的内容
     * @param group      正则的分组
     * @param collection 返回的集合类型
     * @return 结果集
     */
    public static <T extends Collection<String>> T findAll(String regex, String content, int group, T collection) {
        if (null == regex) {
            return null;
        }

        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        return findAll(pattern, content, group, collection);
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param pattern 编译后的正则模式
     * @param content 被查找的内容
     * @param group   正则的分组
     * @return 结果列表
     * @since 3.1.9
     */
    public static List<String> findAll(Pattern pattern, String content, int group) {
        return findAll(pattern, content, group, new ArrayList<>());
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param <T>        集合类型
     * @param pattern    编译后的正则模式
     * @param content    被查找的内容
     * @param group      正则的分组
     * @param collection 返回的集合类型
     * @return 结果集
     */
    public static <T extends Collection<String>> T findAll(Pattern pattern, String content, int group, T collection) {
        if (null == pattern || null == content) {
            return null;
        }

        if (null == collection) {
            throw new NullPointerException("Null collection param provided!");
        }

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            collection.add(matcher.group(group));
        }
        return collection;
    }

    /**
     * 计算指定字符串中，匹配pattern的个数
     *
     * @param regex   正则表达式
     * @param content 被查找的内容
     * @return 匹配个数
     */
    public static int count(String regex, String content) {
        if (null == regex || null == content) {
            return 0;
        }
        final Pattern pattern = get(regex, Pattern.DOTALL);
        return count(pattern, content);
    }

    /**
     * 计算指定字符串中，匹配pattern的个数
     *
     * @param pattern 编译后的正则模式
     * @param content 被查找的内容
     * @return 匹配个数
     */
    public static int count(Pattern pattern, String content) {
        if (null == pattern || null == content) {
            return 0;
        }

        int count = 0;
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * 指定内容中是否有表达式匹配的内容
     *
     * @param regex   正则表达式
     * @param content 被查找的内容
     * @return 指定内容中是否有表达式匹配的内容
     * @since 3.3.1
     */
    public static boolean contains(String regex, String content) {
        if (null == regex || null == content) {
            return false;
        }
        final Pattern pattern = get(regex, Pattern.DOTALL);
        return contains(pattern, content);
    }

    /**
     * 指定内容中是否有表达式匹配的内容
     *
     * @param pattern 编译后的正则模式
     * @param content 被查找的内容
     * @return 指定内容中是否有表达式匹配的内容
     * @since 3.3.1
     */
    public static boolean contains(Pattern pattern, String content) {
        if (null == pattern || null == content) {
            return false;
        }
        return pattern.matcher(content).find();
    }

    /**
     * 给定内容是否匹配正则
     *
     * @param regex   正则
     * @param content 内容
     * @return 正则为null或者""则不检查，返回true，内容为null返回false
     */
    public static boolean isMatch(String regex, String content) {
        if (content == null) {
            // 提供null的字符串为不匹配
            return false;
        }

        if (StringUtils.isEmpty(regex)) {
            return true;
        }
        final Pattern pattern = get(regex, Pattern.DOTALL);
        return isMatch(pattern, content);
    }

    /**
     * 给定内容是否匹配正则
     *
     * @param pattern 模式
     * @param content 内容
     * @return 正则为null或者""则不检查，返回true，内容为null返回false
     */
    public static boolean isMatch(Pattern pattern, String content) {
        if (content == null || pattern == null) {
            // 提供null的字符串为不匹配
            return false;
        }
        return pattern.matcher(content).matches();
    }

    /**
     * 正则替换指定值
     * 通过正则查找到字符串，然后把匹配到的字符串加入到replacementTemplate中，$1表示分组1的字符串
     *
     * @param content             文本
     * @param regex               正则
     * @param replacementTemplate 替换的文本模板，可以使用$1类似的变量提取正则匹配出的内容
     * @return 处理后的文本
     */
    public static String replaceAll(String content, String regex, String replacementTemplate) {
        final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        return replaceAll(content, pattern, replacementTemplate);
    }

    /**
     * 正则替换指定值
     * 通过正则查找到字符串，然后把匹配到的字符串加入到replacementTemplate中，$1表示分组1的字符串
     *
     * @param content             文本
     * @param pattern             {@link Pattern}
     * @param replacementTemplate 替换的文本模板，可以使用$1类似的变量提取正则匹配出的内容
     * @return 处理后的文本
     * @since 3.0.4
     */
    public static String replaceAll(String content, Pattern pattern, String replacementTemplate) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }

        final Matcher matcher = pattern.matcher(content);
        boolean result = matcher.find();
        if (result) {
            final Set<String> varNums = findAll(RegEx.GROUP_VAR, replacementTemplate, 1, new HashSet<String>());
            final StringBuffer sb = new StringBuffer();
            do {
                String replacement = replacementTemplate;
                for (String var : varNums) {
                    int group = Integer.parseInt(var);
                    replacement = replacement.replace("$" + var, matcher.group(group));
                }
                matcher.appendReplacement(sb, escape(replacement));
                result = matcher.find();
            } while (result);
            matcher.appendTail(sb);
            return sb.toString();
        }
        return content;
    }

    /**
     * 转义字符，将正则的关键字转义
     *
     * @param c 字符
     * @return 转义后的文本
     */
    public static String escape(char c) {
        final StringBuilder builder = new StringBuilder();
        if (RegEx.RE_KEYS.contains(c)) {
            builder.append('\\');
        }
        builder.append(c);
        return builder.toString();
    }

    /**
     * 转义字符串，将正则的关键字转义
     *
     * @param content 文本
     * @return 转义后的文本
     */
    public static String escape(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        final StringBuilder builder = new StringBuilder();
        int len = content.length();
        char current;
        for (int i = 0; i < len; i++) {
            current = content.charAt(i);
            if (RegEx.RE_KEYS.contains(current)) {
                builder.append('\\');
            }
            builder.append(current);
        }
        return builder.toString();
    }

    /**
     * 从缓存池中查找值
     *
     * @param key 键
     * @return 值
     */
    private static Pattern isGet(RegexWithFlag key) {
        // 尝试读取缓存
        readLock.lock();
        Pattern value;
        try {
            value = cache.get(key);
        } finally {
            readLock.unlock();
        }
        return value;
    }

    /**
     * 放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 值
     */
    private static Object isPut(RegexWithFlag key, Pattern value) {
        writeLock.lock();
        try {
            cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
        return value;
    }

    /**
     * 移除缓存
     *
     * @param key 键
     * @return 移除的值
     */
    private static Object isRemove(Object key) {
        writeLock.lock();
        try {
            return cache.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 清空缓存池
     */
    public void clear() {
        writeLock.lock();
        try {
            cache.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 正则表达式和正则标识位的包装
     *
     * @author Kimi Liu
     */
    private static class RegexWithFlag {
        private String regex;
        private int flag;

        /**
         * 构造
         *
         * @param regex 正则
         * @param flag  标识
         */
        public RegexWithFlag(String regex, int flag) {
            this.regex = regex;
            this.flag = flag;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + flag;
            result = prime * result + ((regex == null) ? 0 : regex.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RegexWithFlag other = (RegexWithFlag) obj;
            if (flag != other.flag) {
                return false;
            }
            if (regex == null) {
                return other.regex == null;
            } else return regex.equals(other.regex);
        }

    }

}

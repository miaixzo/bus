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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.compare.LengthCompare;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.function.FuncX;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用正则表达式集合
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternKit {

    private static final Map<RegexWithFlag, Pattern> CACHE = new WeakHashMap<>();
    private static final ReentrantReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock READ_LOCK = CACHE_LOCK.readLock();
    private static final ReentrantReadWriteLock.WriteLock WRITE_LOCK = CACHE_LOCK.writeLock();

    /**
     * 先从Pattern池中查找正则对应的{@link Pattern},找不到则编译正则表达式并入池
     *
     * @param regex 正则表达式
     * @return {@link Pattern}
     */
    public static Pattern get(String regex) {
        return get(regex, 0);
    }

    /**
     * 先从Pattern池中查找正则对应的{@link Pattern},找不到则编译正则表达式并入池
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
     * 获得匹配的字符串
     *
     * @param regex      匹配的正则
     * @param content    被匹配的内容
     * @param groupIndex 匹配正则的分组序号
     * @return 匹配后得到的字符串, 未匹配返回null
     */
    public static String get(String regex, String content, int groupIndex) {
        if (null == content || null == regex) {
            return null;
        }
        final Pattern pattern = get(regex, Pattern.DOTALL);
        return get(pattern, content, groupIndex);
    }

    /**
     * 获得匹配的字符串
     *
     * @param pattern    编译后的正则模式
     * @param content    被匹配的内容
     * @param groupIndex 匹配正则的分组序号
     * @return 匹配后得到的字符串, 未匹配返回null
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
     * 获得匹配的字符串，对应分组0表示整个匹配内容，1表示第一个括号分组内容，依次类推
     *
     * @param pattern    编译后的正则模式
     * @param content    被匹配的内容
     * @param groupIndex 匹配正则的分组序号，0表示整个匹配内容，1表示第一个括号分组内容，依次类推
     * @return 匹配后得到的字符串，未匹配返回null
     */
    public static String get(Pattern pattern, CharSequence content, int groupIndex) {
        if (null == content || null == pattern) {
            return null;
        }

        final MutableObject<String> result = new MutableObject<>();
        get(pattern, content, matcher -> result.set(matcher.group(groupIndex)));
        return result.get();
    }

    /**
     * 获得匹配的字符串
     *
     * @param pattern   匹配的正则
     * @param content   被匹配的内容
     * @param groupName 匹配正则的分组名称
     * @return 匹配后得到的字符串，未匹配返回null
     */
    public static String get(Pattern pattern, CharSequence content, String groupName) {
        if (null == content || null == pattern || null == groupName) {
            return null;
        }
        final Matcher m = pattern.matcher(content);
        if (m.find()) {
            return m.group(groupName);
        }
        return null;
    }

    /**
     * 在给定字符串中查找给定规则的字符，如果找到则使用{@link Consumer}处理之
     * 如果内容中有多个匹配项，则只处理找到的第一个结果。
     *
     * @param pattern  匹配的正则
     * @param content  被匹配的内容
     * @param consumer 匹配到的内容处理器
     */
    public static void get(Pattern pattern, CharSequence content, Consumer<Matcher> consumer) {
        if (null == content || null == pattern || null == consumer) {
            return;
        }
        final Matcher m = pattern.matcher(content);
        if (m.find()) {
            consumer.accept(m);
        }
    }

    /**
     * 获得匹配的字符串,获得正则中分组0的内容
     *
     * @param regex   匹配的正则
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串, 未匹配返回null
     */
    public static String getGroup0(String regex, String content) {
        return get(regex, content, 0);
    }

    /**
     * 获得匹配的字符串,获得正则中分组1的内容
     *
     * @param regex   匹配的正则
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串, 未匹配返回null
     */
    public static String getGroup1(String regex, String content) {
        return get(regex, content, 1);
    }

    /**
     * 获得匹配的字符串,,获得正则中分组0的内容
     *
     * @param pattern 编译后的正则模式
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串, 未匹配返回null
     */
    public static String getGroup0(Pattern pattern, CharSequence content) {
        return get(pattern, content, 0);
    }

    /**
     * 获得匹配的字符串,,获得正则中分组1的内容
     *
     * @param pattern 编译后的正则模式
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串, 未匹配返回null
     */
    public static String getGroup1(Pattern pattern, CharSequence content) {
        return get(pattern, content, 1);
    }

    /**
     * 获得匹配的字符串匹配到的所有分组
     *
     * @param pattern 编译后的正则模式
     * @param content 被匹配的内容
     * @return 匹配后得到的字符串数组，按照分组顺序依次列出，未匹配到返回空列表，任何一个参数为null返回null
     */
    public static List<String> getAllGroups(Pattern pattern, CharSequence content) {
        return getAllGroups(pattern, content, true);
    }

    /**
     * 获得匹配的字符串匹配到的所有分组
     *
     * @param pattern    编译后的正则模式
     * @param content    被匹配的内容
     * @param withGroup0 是否包括分组0，此分组表示全匹配的信息
     * @return 匹配后得到的字符串数组，按照分组顺序依次列出，未匹配到返回空列表，任何一个参数为null返回null
     */
    public static List<String> getAllGroups(Pattern pattern, CharSequence content, boolean withGroup0) {
        return getAllGroups(pattern, content, withGroup0, false);
    }

    /**
     * 获得匹配的字符串匹配到的所有分组
     *
     * @param pattern    编译后的正则模式
     * @param content    被匹配的内容
     * @param withGroup0 是否包括分组0，此分组表示全匹配的信息
     * @param findAll    是否查找所有匹配到的内容，{@code false}表示只读取第一个匹配到的内容
     * @return 匹配后得到的字符串数组，按照分组顺序依次列出，未匹配到返回空列表，任何一个参数为null返回null
     */
    public static List<String> getAllGroups(Pattern pattern, CharSequence content, boolean withGroup0, boolean findAll) {
        if (null == content || null == pattern) {
            return null;
        }

        List<String> result = new ArrayList<>();
        final Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            final int startGroup = withGroup0 ? 0 : 1;
            final int groupCount = matcher.groupCount();
            for (int i = startGroup; i <= groupCount; i++) {
                result.add(matcher.group(i));
            }

            if (false == findAll) {
                break;
            }
        }
        return result;
    }

    /**
     * 获得匹配的字符串
     *
     * @param regex     匹配的正则
     * @param content   被匹配的内容
     * @param groupName 匹配正则的分组名称
     * @return 匹配后得到的字符串，未匹配返回null
     */
    public static String getByGroupName(String regex, CharSequence content, String groupName) {
        if (null == content || null == regex || null == groupName) {
            return null;
        }
        final Pattern pattern = PatternKit.get(regex, Pattern.DOTALL);
        Matcher m = pattern.matcher(content);
        if (m.find()) {
            return m.group(groupName);
        }
        return null;
    }

    /**
     * 获得匹配的字符串
     *
     * @param regex   匹配的正则
     * @param content 被匹配的内容
     * @return 命名捕获组
     */
    public static Map<String, String> getAllGroupNames(String regex, CharSequence content) {
        if (null == content || null == regex) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        try {
            final Pattern pattern = PatternKit.get(regex, Pattern.DOTALL);
            Matcher m = pattern.matcher(content);
            // 通过反射获取 namedGroups 方法
            Method method = ReflectKit.getMethod(Pattern.class, "namedGroups");
            ReflectKit.setAccessible(method);
            Map<String, Integer> map = (Map<String, Integer>) method.invoke(pattern);
            // 组合返回值
            if (m.matches()) {
                for (Map.Entry<String, Integer> e : map.entrySet()) {
                    result.put(e.getKey(), m.group(e.getValue()));
                }
            }
            return result;
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new InternalException("call getAllGroupNames(...) method error: " + ex.getMessage());
        }
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
    public static String extractMulti(Pattern pattern, CharSequence content, String template) {
        if (null == content || null == pattern || null == template) {
            return null;
        }

        // 提取模板中的编号
        final TreeSet<Integer> varNums = new TreeSet<>((o1, o2) -> ObjectKit.compare(o2, o1));
        final Matcher matcherForTemplate = RegEx.GROUP_VAR.matcher(template);
        while (matcherForTemplate.find()) {
            varNums.add(Integer.parseInt(matcherForTemplate.group(1)));
        }

        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (Integer group : varNums) {
                template = template.replace(Symbol.DOLLAR + group, matcher.group(group));
            }
            return template;
        }
        return null;
    }

    /**
     * 从content中匹配出多个值并根据template生成新的字符串
     * 匹配结束后会删除匹配内容之前的内容(包括匹配内容)
     * 例如：
     * content 2019年5月 pattern (.*?)年(.*?)月 template： $1-$2 return 2019-5
     *
     * @param regex    匹配正则字符串
     * @param content  被匹配的内容
     * @param template 生成内容模板，变量 $1 表示group1的内容，以此类推
     * @return 按照template拼接后的字符串
     */
    public static String extractMulti(String regex, CharSequence content, String template) {
        if (null == content || null == regex || null == template) {
            return null;
        }

        final Pattern pattern = get(regex, Pattern.DOTALL);
        return extractMulti(pattern, content, template);
    }

    /**
     * 从content中匹配出多个值并根据template生成新的字符串
     * 匹配结束后会删除匹配内容之前的内容(包括匹配内容)
     * 例如：
     * content 2019年5月 pattern (.*?)年(.*?)月 template： $1-$2 return 2019-5
     *
     * @param pattern       匹配正则
     * @param contentHolder 被匹配的内容的Holder，value为内容正文，经过这个方法的原文将被去掉匹配之前的内容
     * @param template      生成内容模板，变量 $1 表示group1的内容，以此类推
     * @return 新字符串
     */
    public static String extractMultiAndDelPre(Pattern pattern, Mutable<CharSequence> contentHolder, String template) {
        if (null == contentHolder || null == pattern || null == template) {
            return null;
        }

        HashSet<String> varNums = findAll(RegEx.GROUP_VAR, template, 1, new HashSet<>());

        final CharSequence content = contentHolder.get();
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (String var : varNums) {
                int group = Integer.parseInt(var);
                template = template.replace(Symbol.DOLLAR + var, matcher.group(group));
            }
            contentHolder.set(StringKit.sub(content, matcher.end(), content.length()));
            return template;
        }
        return null;
    }

    /**
     * 从content中匹配出多个值并根据template生成新的字符串
     * 例如：
     * content 2019年5月 pattern (.*?)年(.*?)月 template： $1-$2 return 2019-5
     *
     * @param regex         匹配正则字符串
     * @param contentHolder 被匹配的内容的Holder，value为内容正文，经过这个方法的原文将被去掉匹配之前的内容
     * @param template      生成内容模板，变量 $1 表示group1的内容，以此类推
     * @return 按照template拼接后的字符串
     */
    public static String extractMultiAndDelPre(String regex, Mutable<CharSequence> contentHolder, String template) {
        if (null == contentHolder || null == regex || null == template) {
            return null;
        }

        final Pattern pattern = get(regex, Pattern.DOTALL);
        return extractMultiAndDelPre(pattern, contentHolder, template);
    }

    /**
     * 删除匹配的第一个内容
     *
     * @param pattern 正则
     * @param content 被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delFirst(Pattern pattern, String content) {
        if (null == pattern || StringKit.isBlank(content)) {
            return content;
        }

        return pattern.matcher(content).replaceFirst(Normal.EMPTY);
    }

    /**
     * 删除匹配的最后一个内容
     *
     * @param regex 正则
     * @param text  被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delLast(String regex, CharSequence text) {
        if (StringKit.hasBlank(regex, text)) {
            return StringKit.toString(text);
        }

        return delLast(get(regex, Pattern.DOTALL), text);
    }

    /**
     * 删除匹配的最后一个内容
     *
     * @param pattern 正则
     * @param text    被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delLast(Pattern pattern, CharSequence text) {
        if (null != pattern && StringKit.isNotBlank(text)) {
            String last = "";
            for (Matcher matcher = pattern.matcher(text); matcher.find(); ) {
                last = matcher.group();
            }

            if (StringKit.isNotBlank(last)) {
                return StringKit.subBefore(text, last, Boolean.TRUE) + StringKit.subAfter(text, last, Boolean.TRUE);
            }
        }

        return StringKit.toString(text);
    }

    /**
     * 删除匹配的全部内容
     *
     * @param pattern 正则
     * @param content 被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delAll(Pattern pattern, String content) {
        if (null == pattern || StringKit.isBlank(content)) {
            return content;
        }

        return pattern.matcher(content).replaceAll(Normal.EMPTY);
    }

    /**
     * 删除匹配的全部内容
     *
     * @param regex   正则
     * @param content 被匹配的内容
     * @return 删除后剩余的内容
     */
    public static String delAll(String regex, String content) {
        if (StringKit.hasBlank(regex, content)) {
            return content;
        }

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
    public static String delPre(String regex, CharSequence content) {
        if (null == content || null == regex) {
            return StringKit.toString(content);
        }

        final Pattern pattern = PatternKit.get(regex, Pattern.DOTALL);
        return delPre(pattern, content);
    }

    /**
     * 删除正则匹配到的内容之前的字符 如果没有找到，则返回原文
     *
     * @param pattern 定位正则模式
     * @param content 被查找的内容
     * @return 删除前缀后的新内容
     */
    public static String delPre(Pattern pattern, CharSequence content) {
        if (null == content || null == pattern) {
            return StringKit.toString(content);
        }

        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return StringKit.sub(content, matcher.end(), content.length());
        }
        return StringKit.toString(content);
    }

    /**
     * 取得内容中匹配的所有结果,获得匹配的所有结果中正则对应分组0的内容
     *
     * @param regex   正则
     * @param content 被查找的内容
     * @return 结果列表
     */
    public static List<String> findAllGroup0(String regex, String content) {
        return findAll(regex, content, 0);
    }

    /**
     * 取得内容中匹配的所有结果,获得匹配的所有结果中正则对应分组1的内容
     *
     * @param regex   正则
     * @param content 被查找的内容
     * @return 结果列表
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
        return findAll(PatternKit.get(regex, Pattern.DOTALL), content, group, collection);
    }

    /**
     * 取得内容中匹配的所有结果
     *
     * @param pattern 编译后的正则模式
     * @param content 被查找的内容
     * @param group   正则的分组
     * @return 结果列表
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
    public static <T extends Collection<String>> T findAll(Pattern pattern, CharSequence content, int group, T collection) {
        if (null == pattern || null == content) {
            return null;
        }
        Assert.notNull(collection, "Collection must be not null !");

        findAll(pattern, content, (matcher) -> collection.add(matcher.group(group)));
        return collection;
    }

    /**
     * 取得内容中匹配的所有结果，使用{@link Consumer}完成匹配结果处理
     *
     * @param pattern  编译后的正则模式
     * @param content  被查找的内容
     * @param consumer 匹配结果处理函数
     */
    public static void findAll(Pattern pattern, CharSequence content, Consumer<Matcher> consumer) {
        if (null == pattern || null == content) {
            return;
        }

        final Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            consumer.accept(matcher);
        }
    }

    /**
     * 计算指定字符串中,匹配pattern的个数
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
     * 计算指定字符串中,匹配pattern的个数
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
     */
    public static boolean contains(String regex, CharSequence content) {
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
     */
    public static boolean contains(Pattern pattern, CharSequence content) {
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
     * @return 正则为null或者""则不检查,返回true,内容为null返回false
     */
    public static boolean isMatch(String regex, CharSequence content) {
        if (null == content) {
            // 提供null的字符串为不匹配
            return false;
        }

        if (StringKit.isEmpty(regex)) {
            // 正则不存在则为全匹配
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
     * @return 正则为null或者""则不检查,返回true,内容为null返回false
     */
    public static boolean isMatch(Pattern pattern, CharSequence content) {
        if (null == content || null == pattern) {
            // 提供null的字符串为不匹配
            return false;
        }
        return pattern.matcher(content).matches();
    }

    /**
     * 正则替换指定值
     * 通过正则查找到字符串，然后把匹配到的字符串加入到replacementTemplate中，$1表示分组1的字符串
     * 例如：原字符串是：中文1234，我想把1234换成(1234)，则可以：
     *
     * <pre>
     *      replaceAll("中文1234", "(\\d+)", "($1)"))
     *      结果：中文(1234)
     * </pre>
     *
     * @param content             文本
     * @param regex               正则
     * @param replacementTemplate 替换的文本模板，可以使用$1类似的变量提取正则匹配出的内容
     * @return 处理后的文本
     */
    public static String replaceAll(CharSequence content, String regex, String replacementTemplate) {
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
     */
    public static String replaceAll(CharSequence content, Pattern pattern, String replacementTemplate) {
        if (StringKit.isEmpty(content)) {
            return StringKit.toString(content);
        }

        Assert.notNull(replacementTemplate, "ReplacementTemplate must be not null !");

        final Matcher matcher = pattern.matcher(content);
        boolean result = matcher.find();
        if (result) {
            final Set<String> varNums = findAll(RegEx.GROUP_VAR, replacementTemplate, 1,
                    new TreeSet<>(LengthCompare.INSTANCE.reversed()));
            final StringBuffer sb = new StringBuffer();
            do {
                String replacement = replacementTemplate;
                for (String var : varNums) {
                    int group = Integer.parseInt(var);
                    replacement = replacement.replace(Symbol.DOLLAR + var, matcher.group(group));
                }
                matcher.appendReplacement(sb, escape(replacement));
                result = matcher.find();
            } while (result);
            matcher.appendTail(sb);
            return sb.toString();
        }
        return StringKit.toString(content);
    }

    /**
     * 替换所有正则匹配的文本，并使用自定义函数决定如何替换
     * replaceFun可以通过{@link Matcher}提取出匹配到的内容的不同部分，然后经过重新处理、组装变成新的内容放回原位。
     *
     * <pre class="code">
     *     replaceAll(this.content, "(\\d+)", parameters -&gt; "-" + parameters.group(1) + "-")
     *     结果："ZZZaaabbbccc中文-1234-"
     * </pre>
     *
     * @param text       要替换的字符串
     * @param regex      用于匹配的正则式
     * @param replaceFun 决定如何替换的函数
     * @return 替换后的文本
     */
    public static String replaceAll(CharSequence text, String regex, FuncX<Matcher, String> replaceFun) {
        return replaceAll(text, Pattern.compile(regex), replaceFun);
    }

    /**
     * 替换所有正则匹配的文本，并使用自定义函数决定如何替换
     * replaceFun可以通过{@link Matcher}提取出匹配到的内容的不同部分，然后经过重新处理、组装变成新的内容放回原位。
     *
     * <pre class="code">
     *     replaceAll(this.content, "(\\d+)", parameters -&gt; "-" + parameters.group(1) + "-")
     *     结果："ZZZaaabbbccc中文-1234-"
     * </pre>
     *
     * @param text       要替换的字符串
     * @param pattern    用于匹配的正则式
     * @param replaceFun 决定如何替换的函数,可能被多次调用（当有多个匹配时）
     * @return 替换后的字符串
     */
    public static String replaceAll(CharSequence text, Pattern pattern, FuncX<Matcher, String> replaceFun) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toString(text);
        }

        final Matcher matcher = pattern.matcher(text);
        final StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, replaceFun.apply(matcher));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 转义字符,将正则的关键字转义
     *
     * @param c 字符
     * @return 转义后的文本
     */
    public static String escape(char c) {
        final StringBuilder builder = new StringBuilder();
        if (RegEx.RE_KEYS.contains(c)) {
            builder.append(Symbol.C_BACKSLASH);
        }
        builder.append(c);
        return builder.toString();
    }

    /**
     * 转义字符串,将正则的关键字转义
     *
     * @param content 文本
     * @return 转义后的文本
     */
    public static String escape(String content) {
        if (StringKit.isBlank(content)) {
            return content;
        }

        final StringBuilder builder = new StringBuilder();
        int len = content.length();
        char current;
        for (int i = 0; i < len; i++) {
            current = content.charAt(i);
            if (RegEx.RE_KEYS.contains(current)) {
                builder.append(Symbol.C_BACKSLASH);
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
        READ_LOCK.lock();
        Pattern value;
        try {
            value = CACHE.get(key);
        } finally {
            READ_LOCK.unlock();
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
        WRITE_LOCK.lock();
        try {
            CACHE.put(key, value);
        } finally {
            WRITE_LOCK.unlock();
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
        WRITE_LOCK.lock();
        try {
            return CACHE.remove(key);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    /**
     * 从字符串中获得第一个整数
     *
     * @param stringWithNumber 带数字的字符串
     * @return 整数
     */
    public static Integer getFirstNumber(final CharSequence stringWithNumber) {
        return Convert.toInt(get(RegEx.NUMBERS, stringWithNumber, 0), null);
    }

    /**
     * 找到指定正则匹配到字符串的开始位置
     *
     * @param regex   正则
     * @param content 字符串
     * @return 位置，{@code null}表示未找到
     */
    public static MatchResult indexOf(String regex, CharSequence content) {
        if (null == regex || null == content) {
            return null;
        }

        final Pattern pattern = get(regex, Pattern.DOTALL);
        return indexOf(pattern, content);
    }

    /**
     * 找到指定模式匹配到字符串的开始位置
     *
     * @param pattern 模式
     * @param content 字符串
     * @return 位置，{@code null}表示未找到
     */
    public static MatchResult indexOf(Pattern pattern, CharSequence content) {
        if (null != pattern && null != content) {
            final Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.toMatchResult();
            }
        }

        return null;
    }

    /**
     * 找到指定正则匹配到第一个字符串的位置
     *
     * @param regex   正则
     * @param content 字符串
     * @return 位置，{@code null}表示未找到
     */
    public static MatchResult lastIndexOf(String regex, CharSequence content) {
        if (null == regex || null == content) {
            return null;
        }

        final Pattern pattern = get(regex, Pattern.DOTALL);
        return lastIndexOf(pattern, content);
    }

    /**
     * 找到指定模式匹配到最后一个字符串的位置
     *
     * @param pattern 模式
     * @param content 字符串
     * @return 位置，{@code null}表示未找到
     */
    public static MatchResult lastIndexOf(Pattern pattern, CharSequence content) {
        MatchResult result = null;
        if (null != pattern && null != content) {
            final Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                result = matcher.toMatchResult();
            }
        }

        return result;
    }

    /**
     * 清空缓存池
     */
    public void clear() {
        WRITE_LOCK.lock();
        try {
            CACHE.clear();
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    /**
     * 正则表达式和正则标识位的包装
     *
     * @author Kimi Liu
     */
    private static class RegexWithFlag {

        private final String regex;
        private final int flag;

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
            result = prime * result + ((null == regex) ? 0 : regex.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (null == object) {
                return false;
            }
            if (getClass() != object.getClass()) {
                return false;
            }
            RegexWithFlag other = (RegexWithFlag) object;
            if (flag != other.flag) {
                return false;
            }
            if (null == regex) {
                return null == other.regex;
            } else return regex.equals(other.regex);
        }

    }

}

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
package org.miaixz.bus.validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.magic.Checker;

import lombok.Getter;
import lombok.Setter;

/**
 * 当前校验的上下文信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Context {

    /**
     * 全局定义的错误码, 如果校验注解使用了-1 作为错误码,默认替换为该全局错误码
     */
    private String errcode = Builder.DEFAULT_ERRCODE;

    /**
     * 当前被激活的校验组
     */
    private List<String> group = new ArrayList<>();

    /**
     * 全局校验异常 当校验失败时,如果定义了全局校验异常,则抛出全局校验异常； 然后判断如果定义了字段异常,则抛出字段异常； 最后判断如果定义了校验器注解异常,则抛出校验器注解上定义的异常;
     * 如果都没定义,则抛出{@link ValidateException}
     */
    private Class<? extends ValidateException> exception;

    /**
     * 当前被激活的属性
     */
    private String[] field;

    /**
     * 当前需跳过的属性
     */
    private String[] skip;

    /**
     * 校验检查器
     */
    private Checker checker;

    /**
     * 快速失败, 默认：true true: 表示如果参数一旦校验,立刻抛出校验失败异常 false: 即使存在参数校验失败,也必须等到该参数所有的校验器执行后,才会抛出异常
     */
    private boolean fast = true;

    /**
     * 是否校验对象内部, 默认：false
     */
    private boolean inside = false;

    public Context() {
    }

    /**
     * 提供一个包含默认校验器注册中心的校验器上下文
     *
     * @return 校验器上下文对象
     */
    public static Context newInstance() {
        Context context = new Context();
        context.setChecker(new Checker());
        return context;
    }

    /**
     * 添加校验组
     *
     * @param groups 校验组
     */
    public void addGroups(String... groups) {
        if (ObjectKit.isEmpty(groups) || groups.length == 0) {
            return;
        }
        if (CollKit.isEmpty(this.group)) {
            this.group = new ArrayList<>();
        }
        this.group.addAll(Arrays.asList(groups));
    }

}

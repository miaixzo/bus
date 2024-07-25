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
package org.miaixz.bus.base.advice;

import org.miaixz.bus.core.basic.advice.ErrorAdvice;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 异常信息拦截
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ControllerAdvice
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class BaseAdvice extends Controller {

    /**
     * 应用到所有@RequestMapping注解方法,在其执行之前初始化数据绑定器
     *
     * @param binder 绑定器
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {

    }

    /**
     * 把值绑定到Model中,
     * 使全局@RequestMapping可以获取到该值
     *
     * @param model 对象
     */
    @ModelAttribute
    public void addAttributes(Model model) {

    }

    /**
     * 全局异常拦截
     * 处理全局异常
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object defaultException(Exception e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode.EM_FAILURE);
    }

    /**
     * 内部异常拦截
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = InternalException.class)
    public Object InternalException(InternalException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode.EM_100510);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * 拦截业务异常
     * 事务回滚处理
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Object businessException(BusinessException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode.EM_100513);
        }
        return write(e.getErrcode());
    }

    /**
     * 定时任务失败
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = CrontabException.class)
    public Object crontabException(CrontabException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode.EM_100600);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * 参数验证失败
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = ValidateException.class)
    public Object validateException(ValidateException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode.EM_100510);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * 请求方式拦截
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Object httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode.EM_100507);
    }

    /**
     * 媒体类型拦截
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Object httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode.EM_100508);
    }

    /**
     * 资源未找到
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public Object noHandlerFoundException(NoHandlerFoundException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode.EM_100509);
    }

    /**
     * 参数绑定异常
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleBodyValidException(MethodArgumentNotValidException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode.EM_100511);
    }

    /**
     * 业务处理器处理请求之前被调用,对用户的request进行处理,若返回值为true,
     * 则继续调用后续的拦截器和目标方法；若返回值为false, 则终止请求；
     * 这里可以加上登录校验,权限拦截、请求限流等
     *
     * @param ex 对象参数
     */
    public void defaultExceptionHandler(Exception ex) {
        try {
            Instances.singletion(ErrorAdvice.class).handler(ex);
        } catch (RuntimeException ignore) {
            // ignore
        }
    }

}

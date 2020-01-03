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
package org.aoju.bus.base.advice;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.spring.Controller;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.core.lang.exception.CrontabException;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.lang.exception.ValidateException;
import org.aoju.bus.core.utils.RuntimeUtils;
import org.aoju.bus.logger.Logger;
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
 * <p>
 * 异常信息拦截处理
 * </p>
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
@ControllerAdvice
@RestControllerAdvice
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
        Logger.error(RuntimeUtils.getStackTrace(e));
        return write(ErrorCode.EM_FAILURE);
    }

    /**
     * 内部异常拦截
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = InstrumentException.class)
    public Object instrumentException(InstrumentException e) {
        Logger.error(RuntimeUtils.getStackTrace(e));
        return write(ErrorCode.EM_100510);
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
        Logger.error(RuntimeUtils.getStackTrace(e));
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
        Logger.error(RuntimeUtils.getStackTrace(e));
        return write(ErrorCode.EM_100514);
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
        Logger.error(RuntimeUtils.getStackTrace(e));
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
        Logger.error(e.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        return write(ErrorCode.EM_100511);
    }

}

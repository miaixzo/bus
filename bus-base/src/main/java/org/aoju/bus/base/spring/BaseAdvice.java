/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.base.spring;

import org.aoju.bus.base.consts.Consts;
import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.core.lang.exception.*;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.logger.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * <p>
 * 异常信息拦截处理
 * </p>
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
@ControllerAdvice
@RestControllerAdvice
public class BaseAdvice extends Controller {

    /**
     * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
     *
     * @param binder 绑定器
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {

    }

    /**
     * 把值绑定到Model中，
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
    public String defaultException(Exception e) {
        Logger.error(getStackTraceMessage(e));
        return write(ErrorCode.EM_FAILURE);
    }

    /**
     * 通用异常信息
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = CommonException.class)
    public String commonException(CommonException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ErrorCode.EM_100506);
    }

    /**
     * 工具异常拦截
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = InstrumentException.class)
    public String instrumentException(InstrumentException e) {
        Logger.error(getStackTraceMessage(e));
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
    public String businessException(BusinessException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ErrorCode.EM_100513);
    }

    /**
     * 定时任务失败
     *
     * @param e 异常信息
     * @return 异常提示
     */
    @ResponseBody
    @ExceptionHandler(value = CrontabException.class)
    public String crontabException(CrontabException e) {
        Logger.error(getStackTraceMessage(e));
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
    public String ValidateException(ValidateException e) {
        Logger.error(getStackTraceMessage(e));
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
    public String httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        Logger.error(getStackTraceMessage(e));
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
    public String httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        Logger.error(getStackTraceMessage(e));
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
    public String noHandlerFoundException(NoHandlerFoundException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ErrorCode.EM_100509);
    }

    /**
     * 从当前堆栈中获取
     *
     * @param exception 当前堆栈异常对象
     * @return String 消息格式[className.methodName:lineNumber : message]
     */
    private String getStackTraceMessage(Exception exception) {
        if (ObjectUtils.isNull(exception)) {
            return " exception is null ";
        }

        StringBuilder stackMessage = new StringBuilder(128);
        stackMessage.append(exception.getMessage()).append("\n");

        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        if (null != stackTraceElements && stackTraceElements.length > 0) {
            int count = 0;
            for (StackTraceElement currentStackTrace : stackTraceElements) {
                if (isStack(currentStackTrace) && count < Consts.CODE_STACK_DEPTH) {
                    String message = String.format("        %s.%s : %s",
                            currentStackTrace.getClassName(),
                            currentStackTrace.getMethodName(),
                            currentStackTrace.getLineNumber());
                    stackMessage.append(message).append("\n");
                }
                count++;
            }
        }
        return stackMessage.toString();
    }

    /**
     * @param stackTraceElement 当前堆栈元素
     * @return true/false
     */
    private boolean isStack(StackTraceElement stackTraceElement) {
        return ObjectUtils.isNotNull(stackTraceElement)
                ? stackTraceElement.getClassName().startsWith(Consts.CLASS_NAME_PREFIX) : Boolean.FALSE;
    }

}

package org.aoju.bus.base.spring;

import org.aoju.bus.base.consts.Consts;
import org.aoju.bus.base.consts.ResultCode;
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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@ControllerAdvice
@RestControllerAdvice
public class BaseAdvice extends Controller {

    /**
     * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
     *
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {

    }

    /**
     * 把值绑定到Model中，
     * 使全局@RequestMapping可以获取到该值
     *
     * @param model
     */
    @ModelAttribute
    public void addAttributes(Model model) {

    }

    /**
     * 全局异常拦截
     * 处理全局异常
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public String defaultException(Exception e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_FAILURE);
    }

    /**
     * 通用异常信息
     */
    @ResponseBody
    @ExceptionHandler(value = CommonException.class)
    public String commonException(CommonException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_100506);
    }

    /**
     * 工具异常拦截
     */
    @ResponseBody
    @ExceptionHandler(value = InstrumentException.class)
    public String instrumentException(InstrumentException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_100510);
    }

    /**
     * 拦截业务异常
     * 事务回滚处理
     */
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public String businessException(BusinessException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_100513);
    }

    /**
     * 定时任务失败
     */
    @ResponseBody
    @ExceptionHandler(value = CrontabException.class)
    public String crontabException(CrontabException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_100514);
    }

    /**
     * 参数验证失败
     */
    @ResponseBody
    @ExceptionHandler(value = ValidateException.class)
    public String ValidateException(ValidateException e) {
        Logger.error(getStackTraceMessage(e));
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * 请求方式拦截
     */
    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public String httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_100507);
    }

    /**
     * 媒体类型拦截
     */
    @ResponseBody
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public String httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_100508);
    }

    /**
     * 资源未找到
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public String noHandlerFoundException(NoHandlerFoundException e) {
        Logger.error(getStackTraceMessage(e));
        return write(ResultCode.EM_100509);
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
                if (isAoju(currentStackTrace) && count < Consts.CODE_STACK_DEPTH) {
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
     * @return Boolean
     */
    private boolean isAoju(StackTraceElement stackTraceElement) {
        return ObjectUtils.isNotNull(stackTraceElement)
                ? stackTraceElement.getClassName().startsWith(Consts.CLASS_NAME_PREFIX) : Boolean.FALSE;
    }

}

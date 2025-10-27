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
package org.miaixz.bus.base.advice;

import org.miaixz.bus.core.basic.advice.ErrorAdvice;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Global exception handling advice class. This class unifies the handling of various exceptions thrown in the
 * application and returns a consistent error response.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ControllerAdvice
@RestControllerAdvice
@ConditionalOnWebApplication
public class BaseAdvice extends Controller {

    /**
     * Initializes the data binder for all methods annotated with @RequestMapping. This method is executed before
     * any @RequestMapping method.
     *
     * @param binder the data binder to initialize
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {

    }

    /**
     * Binds values to the Model, making them accessible to all @RequestMapping methods globally.
     *
     * @param model the Model object to add attributes to
     */
    @ModelAttribute
    public void addAttributes(Model model) {

    }

    /**
     * Global exception handler for all uncaught exceptions. This method processes any {@link Exception} that is not
     * specifically handled by other exception handlers.
     *
     * @param e the exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object defaultException(Exception e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._FAILURE);
    }

    /**
     * Internal exception handler for {@link InternalException}.
     *
     * @param e the internal exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = InternalException.class)
    public Object InternalException(InternalException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Business exception handler for {@link BusinessException}. This handler typically triggers transaction rollback.
     *
     * @param e the business exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Object businessException(BusinessException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100807);
        }
        return write(e.getErrcode());
    }

    /**
     * Crontab exception handler for {@link CrontabException}. This handles exceptions that occur during scheduled task
     * execution.
     *
     * @param e the crontab exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = CrontabException.class)
    public Object crontabException(CrontabException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100808);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Validation exception handler for {@link ValidateException}. This handles exceptions related to parameter
     * validation failures.
     *
     * @param e the validation exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = ValidateException.class)
    public Object validateException(ValidateException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Authorize exception handler for {@link ValidateException}. This handles exceptions related to parameter Authorize
     * failures.
     *
     * @param e the validation exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = AuthorizedException.class)
    public Object authorizedException(AuthorizedException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100806);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Global handler for unchecked (runtime) exceptions.
     *
     * @param e the exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = UncheckedException.class)
    public Object uncheckedException(UncheckedException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * Global handler for I/O or other relevant exceptions.
     *
     * @param e the exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = RelevantException.class)
    public Object relevantException(RelevantException e) {
        this.defaultExceptionHandler(e);
        if (StringKit.isBlank(e.getErrcode())) {
            return write(ErrorCode._100805);
        }
        return write(e.getErrcode(), e.getErrmsg());
    }

    /**
     * HTTP request method not supported exception handler for {@link HttpRequestMethodNotSupportedException}. This
     * handles cases where the client uses an unsupported HTTP method for a given endpoint.
     *
     * @param e the HTTP request method not supported exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Object httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100802);
    }

    /**
     * HTTP media type not supported exception handler for {@link HttpMediaTypeNotSupportedException}. This handles
     * cases where the client sends a request with an unsupported media type.
     *
     * @param e the HTTP media type not supported exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Object httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100803);
    }

    /**
     * No handler found exception handler for {@link NoHandlerFoundException}. This handles cases where no handler
     * (e.g., controller method) is found for a given request.
     *
     * @param e the no handler found exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public Object noHandlerFoundException(NoHandlerFoundException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100804);
    }

    /**
     * Parameter binding exception handler for {@link MethodArgumentNotValidException} and {@link BindException}. This
     * handles exceptions that occur during parameter binding and validation.
     *
     * @param e the parameter binding exception information
     * @return a unified error response object
     */
    @ResponseBody
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public Object handleBodyValidException(MethodArgumentNotValidException e) {
        this.defaultExceptionHandler(e);
        return write(ErrorCode._100809);
    }

    /**
     * Default exception handler for logging and delegating to an external error handling service. This method is called
     * before the business processor handles the request. It can be used for login verification, permission
     * interception, request throttling, etc.
     *
     * @param ex the exception object
     */
    public void defaultExceptionHandler(Exception ex) {
        try {
            Logger.error("<==     Errors: " + ex.getMessage());
            Instances.singletion(ErrorAdvice.class).handler(ex);
        } catch (RuntimeException ignore) {
            // ignore
        }
    }

}

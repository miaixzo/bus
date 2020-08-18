package org.aoju.bus.goalie.handler;

import org.aoju.bus.goalie.ApiConfig;
import org.aoju.bus.goalie.ApiContext;
import org.aoju.bus.goalie.manual.Invoker;
import org.aoju.bus.goalie.manual.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InvokeHandler extends AbstractHandler {

    protected ResponseHandler responseHandler;
    protected Invoker invoker;

    public InvokeHandler(ApiConfig apiConfig, ResponseHandler responseHandler) {
        super(apiConfig);
        this.responseHandler = responseHandler;
        this.invoker = apiConfig.getInvoker();
    }

    public Object processInvoke(HttpServletRequest request, HttpServletResponse response) {
        try {
            Object result = this.invoker.invoke(request, response);
            this.afterInvoke(request, response, result);
            return result;
        } catch (Throwable e) {
            return this.processError(request, response, e);
        } finally {
            ApiContext.clean();
        }
    }

    public Object processInvokeMock(HttpServletRequest request, HttpServletResponse response) {
        try {
            Object result = this.invoker.invokeMock(request, response);
            this.afterInvoke(request, response, result);
            return result;
        } catch (Throwable e) {
            return this.processError(request, response, e);
        } finally {
            ApiContext.clean();
        }
    }

    public Result processError(HttpServletRequest request, HttpServletResponse response, Throwable e) {
        Result result = responseHandler.caugthException(e);
        this.afterInvoke(request, response, result);
        return result;
    }

    protected void afterInvoke(HttpServletRequest request, HttpServletResponse response, Object result) {
        responseResult(request, response, result);
    }

    /**
     * 写数据到客户端
     *
     * @param request  请求
     * @param response 响应
     * @param result   结果
     */
    public void responseResult(HttpServletRequest request, HttpServletResponse response, Object result) {
        responseHandler.responseResult(request, response, result);
    }

}

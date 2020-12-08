/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.magic;

import org.aoju.bus.http.Headers;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.Results;
import org.aoju.bus.http.bodys.ResultBody;
import org.aoju.bus.http.metric.TaskExecutor;
import org.aoju.bus.http.metric.http.CoverHttp;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class RealResult implements Results {

    private State state;
    private Response response;
    private IOException error;
    private TaskExecutor taskExecutor;
    private CoverHttp<?> coverHttp;
    private Body body;

    public RealResult(CoverHttp<?> coverHttp, State state) {
        this.coverHttp = coverHttp;
        this.state = state;
    }

    public RealResult(CoverHttp<?> coverHttp, Response response, TaskExecutor taskExecutor) {
        this(coverHttp, taskExecutor);
        response(response);
    }

    public RealResult(CoverHttp<?> coverHttp, TaskExecutor taskExecutor) {
        this.coverHttp = coverHttp;
        this.taskExecutor = taskExecutor;
    }

    public RealResult(CoverHttp<?> coverHttp, State state, IOException error) {
        this.coverHttp = coverHttp;
        exception(state, error);
    }

    public void exception(State state, IOException error) {
        this.state = state;
        this.error = error;
    }

    public void response(Response response) {
        this.state = State.RESPONSED;
        this.response = response;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public int getStatus() {
        if (response != null) {
            return response.code();
        }
        return 0;
    }

    @Override
    public boolean isSuccessful() {
        if (response != null) {
            return response.isSuccessful();
        }
        return false;
    }

    @Override
    public Headers getHeaders() {
        if (response != null) {
            return response.headers();
        }
        return null;
    }

    @Override
    public List<String> getHeaders(String name) {
        if (response != null) {
            return response.headers(name);
        }
        return Collections.emptyList();
    }

    @Override
    public String getHeader(String name) {
        if (response != null) {
            return response.header(name);
        }
        return null;
    }

    @Override
    public long getContentLength() {
        String length = getHeader("Content-Length");
        if (length != null) {
            try {
                return Long.parseLong(length);
            } catch (Exception ignore) {
            }
        }
        return 0;
    }

    @Override
    public synchronized Body getBody() {
        if (body == null && response != null) {
            body = new ResultBody(coverHttp, response, taskExecutor);
        }
        return body;
    }

    @Override
    public IOException getError() {
        return error;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public String toString() {
        Body body = getBody();
        String str = "RealResult [\n  state: " + state + ",\n  status: " + getStatus()
                + ",\n  headers: " + getHeaders();
        if (body != null) {
            str += ",\n  contentType: " + body.getType();
        }
        return str + ",\n  error: " + error + "\n]";
    }

    @Override
    public Results close() {
        if (response != null) {
            response.close();
        }
        return this;
    }

}

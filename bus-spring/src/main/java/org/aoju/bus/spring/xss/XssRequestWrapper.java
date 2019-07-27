package org.aoju.bus.spring.xss;


import org.aoju.bus.core.utils.EscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null || values.length <= 0) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = EscapeUtils.escapeHtml4(values[i]);
        }
        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return EscapeUtils.escapeHtml4(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return EscapeUtils.escapeHtml4(value);
    }

}


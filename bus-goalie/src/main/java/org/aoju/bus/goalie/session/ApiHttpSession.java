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
 ********************************************************************************/
package org.aoju.bus.goalie.session;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.springframework.util.Assert;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import java.io.Serializable;
import java.util.*;

/**
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public class ApiHttpSession implements HttpSession, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final long creationTime = System.currentTimeMillis();
    private final ServletContext servletContext;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private int maxInactiveInterval;
    private long lastAccessedTime = System.currentTimeMillis();
    private boolean invalid = false;

    private boolean isNew = true;

    /**
     * Create a new ApiHttpSession
     */
    public ApiHttpSession() {
        this(null);
    }

    /**
     * Create a new ApiHttpSession.
     *
     * @param servletContext the ServletContext that the session runs in
     */
    public ApiHttpSession(ServletContext servletContext) {
        this(servletContext, null);
    }

    /**
     * Create a new ApiHttpSession.
     *
     * @param servletContext the ServletContext that the session runs in
     * @param id             a unique identifier for this session
     */
    public ApiHttpSession(ServletContext servletContext, String id) {
        this.servletContext = servletContext;
        this.id = this.buildId(id);
    }

    protected String buildId(String id) {
        return (id != null ? id : UUID.randomUUID().toString().replace(Symbol.HYPHEN, Normal.EMPTY).toUpperCase());
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    @Override
    public Object getAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new Vector<>(this.attributes.keySet()).elements();
    }

    @Override
    public String[] getValueNames() {
        return this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
            }
        } else {
            removeAttribute(name);
        }
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        Object value = this.attributes.remove(name);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    /**
     * Clear all of this session's attributes.
     */
    public void clearAttributes() {
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
        }
    }

    @Override
    public void invalidate() {
        this.invalid = true;
        clearAttributes();
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    public void setNew(boolean value) {
        this.isNew = value;
    }

    /**
     * Serialize the attributes of this session into an object that can be
     * turned into a byte array with standard Java serialization.
     *
     * @return a representation of this session's serialized state
     */
    public Serializable serializeState() {
        HashMap<String, Serializable> state = new HashMap<>();
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof Serializable) {
                state.put(name, (Serializable) value);
            } else {
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }
        return state;
    }

    /**
     * Deserialize the attributes of this session from a state object created by
     * {@link #serializeState()}.
     *
     * @param state a representation of this session's serialized state
     */
    public void deserializeState(Serializable state) {
        Assert.isTrue(state instanceof Map, "Serialized state needs to be of type [java.util.Map]");
        this.attributes.putAll((Map<String, Object>) state);
    }

}
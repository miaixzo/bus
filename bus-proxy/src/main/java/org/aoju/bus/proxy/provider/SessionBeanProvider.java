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
package org.aoju.bus.proxy.provider;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.proxy.Builder;
import org.aoju.bus.proxy.Provider;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * 通过查找home对象并(通过反射)调用无参数create()方法来提供对会话bean的引用
 * 这对本地和远程会话bean都有效
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
public class SessionBeanProvider implements Provider {

    private final String jndiName;
    private final Class clazz;
    private final Properties properties;

    public SessionBeanProvider(String jndiName, Class clazz) {
        this.jndiName = jndiName;
        this.clazz = clazz;
        this.properties = null;
    }

    public SessionBeanProvider(String jndiName, Class clazz, Properties properties) {
        this.jndiName = jndiName;
        this.clazz = clazz;
        this.properties = properties;
    }

    public Object getObject() {
        try {
            final InitialContext initialContext = properties == null ? new InitialContext() :
                    new InitialContext(properties);
            Object homeObject = PortableRemoteObject.narrow(initialContext.lookup(jndiName), clazz);
            final Method createMethod = homeObject.getClass().getMethod("create", Builder.EMPTY_ARGUMENT_TYPES);
            return createMethod.invoke(homeObject, Builder.EMPTY_ARGUMENTS);
        } catch (NoSuchMethodException e) {
            throw new InstrumentException(
                    "Unable to find no-arg create() method on home interface " + clazz.getName() + Symbol.DOT, e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(
                    "No-arg create() method on home interface " + clazz.getName() + " is not accessible.",
                    e);
        } catch (NamingException e) {
            throw new InstrumentException("Unable to lookup EJB home object in JNDI.", e);
        } catch (InvocationTargetException e) {
            throw new InstrumentException(
                    "No-arg create() method on home interface " + clazz.getName() + " threw an exception.", e);
        }
    }

}


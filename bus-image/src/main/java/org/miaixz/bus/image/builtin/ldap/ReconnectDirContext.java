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
package org.miaixz.bus.image.builtin.ldap;

import java.io.Closeable;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.directory.*;

import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
class ReconnectDirContext implements Closeable {

    private final Hashtable env;

    private volatile DirContext ctx;

    public ReconnectDirContext(Hashtable<?, ?> env) throws NamingException {
        this.env = (Hashtable) env.clone();
        this.ctx = new InitialDirContext(env);
    }

    private static boolean isLdap_connection_has_been_closed(NamingException e) {
        return e instanceof CommunicationException || e instanceof ServiceUnavailableException
                || e instanceof NotContextException || e.getMessage().startsWith("LDAP connection has been closed");
    }

    public DirContext getDirCtx() {
        return ctx;
    }

    private void reconnect() throws NamingException {
        Logger.info("Connection to {} broken - reconnect", env.get(Context.PROVIDER_URL));
        close();
        ctx = new InitialDirContext(env);
    }

    @Override
    public void close() {
        try {
            ctx.close();
        } catch (NamingException ignore) {
        }
    }

    public Attributes getAttributes(String name) throws NamingException {
        try {
            return ctx.getAttributes(name);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            return ctx.getAttributes(name);
        }
    }

    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
        try {
            return ctx.getAttributes(name, attrIds);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            return ctx.getAttributes(name, attrIds);
        }
    }

    public void destroySubcontext(String name) throws NamingException {
        try {
            ctx.destroySubcontext(name);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            ctx.destroySubcontext(name);
        }
    }

    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
            throws NamingException {
        try {
            return ctx.search(name, filter, cons);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            return ctx.search(name, filter, cons);
        }
    }

    public void createSubcontextAndClose(String name, Attributes attrs) throws NamingException {
        try {
            ctx.createSubcontext(name, attrs).close();
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            ctx.createSubcontext(name, attrs).close();
        }
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        try {
            return ctx.list(name);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            return ctx.list(name);
        }
    }

    public void modifyAttributes(String name, ModificationItem... mods) throws NamingException {
        try {
            ctx.modifyAttributes(name, mods);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            ctx.modifyAttributes(name, mods);
        }
    }

    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
        try {
            ctx.modifyAttributes(name, mod_op, attrs);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            reconnect();
            ctx.modifyAttributes(name, mod_op, attrs);
        }
    }

}

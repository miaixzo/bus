/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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

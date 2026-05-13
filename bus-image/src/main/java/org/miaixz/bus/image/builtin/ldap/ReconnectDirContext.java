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
 * Represents the ReconnectDirContext type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
class ReconnectDirContext implements Closeable {

    /**
     * The env value.
     */
    private final Hashtable env;

    /**
     * The ctx value.
     */
    private volatile DirContext ctx;

    /**
     * Creates a new instance.
     *
     * @param env the env.
     * @throws NamingException if the operation cannot be completed.
     */
    public ReconnectDirContext(Hashtable<?, ?> env) throws NamingException {
        this.env = (Hashtable) env.clone();
        this.ctx = new InitialDirContext(env);
    }

    /**
     * Determines whether ldap connection has been closed.
     *
     * @param e the e.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean isLdap_connection_has_been_closed(NamingException e) {
        return e instanceof CommunicationException || e instanceof ServiceUnavailableException
                || e instanceof NotContextException || e.getMessage().startsWith("LDAP connection has been closed");
    }

    /**
     * Gets the dir ctx.
     *
     * @return the dir ctx.
     */
    public DirContext getDirCtx() {
        return ctx;
    }

    /**
     * Executes the reconnect operation.
     *
     * @throws NamingException if the operation cannot be completed.
     */
    private void reconnect() throws NamingException {
        Logger.info(
                true,
                "Image",
                "Image LDAP reconnect started: operation={}, protocol={}, status={}, recoverable={}",
                "reconnect",
                "LDAP",
                "started",
                true);
        close();
        ctx = new InitialDirContext(env);
    }

    /**
     * Executes the close operation.
     */
    @Override
    public void close() {
        try {
            ctx.close();
        } catch (NamingException ignore) {
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP close skipped: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "close",
                    "LDAP",
                    "ignored",
                    true,
                    ignore.getClass().getSimpleName());
        }
    }

    /**
     * Gets the attributes.
     *
     * @param name the name.
     * @return the attributes.
     * @throws NamingException if the operation cannot be completed.
     */
    public Attributes getAttributes(String name) throws NamingException {
        try {
            return ctx.getAttributes(name);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "getAttributes",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            return ctx.getAttributes(name);
        }
    }

    /**
     * Gets the attributes.
     *
     * @param name    the name.
     * @param attrIds the attr ids.
     * @return the attributes.
     * @throws NamingException if the operation cannot be completed.
     */
    public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
        try {
            return ctx.getAttributes(name, attrIds);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "getAttributesByIds",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            return ctx.getAttributes(name, attrIds);
        }
    }

    /**
     * Executes the destroy subcontext operation.
     *
     * @param name the name.
     * @throws NamingException if the operation cannot be completed.
     */
    public void destroySubcontext(String name) throws NamingException {
        try {
            ctx.destroySubcontext(name);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "destroySubcontext",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            ctx.destroySubcontext(name);
        }
    }

    /**
     * Executes the search operation.
     *
     * @param name   the name.
     * @param filter the filter.
     * @param cons   the cons.
     * @return the operation result.
     * @throws NamingException if the operation cannot be completed.
     */
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
            throws NamingException {
        try {
            return ctx.search(name, filter, cons);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "search",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            return ctx.search(name, filter, cons);
        }
    }

    /**
     * Creates the subcontext and close.
     *
     * @param name  the name.
     * @param attrs the attrs.
     * @throws NamingException if the operation cannot be completed.
     */
    public void createSubcontextAndClose(String name, Attributes attrs) throws NamingException {
        try {
            ctx.createSubcontext(name, attrs).close();
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "createSubcontext",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            ctx.createSubcontext(name, attrs).close();
        }
    }

    /**
     * Executes the list operation.
     *
     * @param name the name.
     * @return the operation result.
     * @throws NamingException if the operation cannot be completed.
     */
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        try {
            return ctx.list(name);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "list",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            return ctx.list(name);
        }
    }

    /**
     * Executes the modify attributes operation.
     *
     * @param name the name.
     * @param mods the mods.
     * @throws NamingException if the operation cannot be completed.
     */
    public void modifyAttributes(String name, ModificationItem... mods) throws NamingException {
        try {
            ctx.modifyAttributes(name, mods);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "modifyAttributes",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            ctx.modifyAttributes(name, mods);
        }
    }

    /**
     * Executes the modify attributes operation.
     *
     * @param name   the name.
     * @param mod_op the mod op.
     * @param attrs  the attrs.
     * @throws NamingException if the operation cannot be completed.
     */
    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
        try {
            ctx.modifyAttributes(name, mod_op, attrs);
        } catch (NamingException e) {
            if (!isLdap_connection_has_been_closed(e))
                throw e;
            Logger.debug(
                    false,
                    "Image",
                    "Image LDAP retry scheduled: operation={}, protocol={}, status={}, recoverable={}, exception={}",
                    "modifyAttributesByMode",
                    "LDAP",
                    "reconnect",
                    true,
                    e.getClass().getSimpleName());
            reconnect();
            ctx.modifyAttributes(name, mod_op, attrs);
        }
    }

}

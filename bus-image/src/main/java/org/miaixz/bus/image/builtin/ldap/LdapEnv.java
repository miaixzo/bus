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

import java.io.Serial;
import java.util.Hashtable;

import javax.naming.Context;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class LdapEnv extends Hashtable<String, Object> {

    @Serial
    private static final long serialVersionUID = 2852257380532L;

    public LdapEnv() {
        put(
                Context.INITIAL_CONTEXT_FACTORY,
                System.getProperty("org.miaixz.bus.image.builtin.ldap", "com.sun.jndi.ldap.LdapCtxFactory"));
        put("java.naming.ldap.attributes.binary", "dicomVendorData");
    }

    public void setUrl(String ldapURL) {
        put(Context.PROVIDER_URL, ldapURL);
    }

    public void setUserDN(String dn) {
        put(Context.SECURITY_PRINCIPAL, dn);
    }

    public void setPassword(String password) {
        put(Context.SECURITY_AUTHENTICATION, "simple");
        put(Context.SECURITY_CREDENTIALS, password);
    }

}

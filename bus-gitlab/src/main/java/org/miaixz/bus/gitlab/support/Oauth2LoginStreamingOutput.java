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
package org.miaixz.bus.gitlab.support;

import java.io.*;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;
import org.miaixz.bus.core.lang.Charset;

/**
 * This StreamingOutput implementation is utilized to send a OAuth2 token request in a secure manner. The password is
 * never copied to a String, instead it is contained in a SecretString that is cleared when an instance of this class is
 * finalized.
 */
public class Oauth2LoginStreamingOutput implements StreamingOutput, AutoCloseable {

    private final String username;
    private final SecretString password;

    public Oauth2LoginStreamingOutput(String username, CharSequence password) {
        this.username = username;
        this.password = new SecretString(password);
    }

    public Oauth2LoginStreamingOutput(String username, char[] password) {
        this.username = username;
        this.password = new SecretString(password);
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {

        Writer writer = new BufferedWriter(new OutputStreamWriter(output, Charset.UTF_8));
        writer.write("{ ");
        writer.write("\"grant_type\": \"password\", ");
        writer.write("\"username\": \"" + username + "\", ");
        writer.write("\"password\": ");

        // Output the quoted password
        writer.write('"');
        for (int i = 0, length = password.length(); i < length; i++) {

            char c = password.charAt(i);
            if (c == '"' || c == '\\') {
                writer.write('\\');
            }

            writer.write(c);
        }

        writer.write('"');

        writer.write(" }");
        writer.flush();
        writer.close();
    }

    /**
     * Clears the contained password's data.
     */
    public void clearPassword() {
        password.clear();
    }

    @Override
    public void close() {
        clearPassword();
    }

    @Override
    public void finalize() throws Throwable {
        clearPassword();
        super.finalize();
    }

}

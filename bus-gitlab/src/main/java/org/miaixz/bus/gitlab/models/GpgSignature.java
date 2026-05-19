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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The gpg signature class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GpgSignature implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852255356008L;

    private Long gpgKeyId;
    private String gpgKeyPrimaryKeyid;
    private String gpgKeyUserName;
    private String gpgKeyUserEmail;
    private String verificationStatus;
    private String gpgKeySubkeyId;

    /**
     * Returns the gpg key id.
     *
     * @return the result
     */

    public Long getGpgKeyId() {
        return gpgKeyId;
    }

    /**
     * Sets the gpg key id.
     *
     * @param gpgKeyId the gpg key id value
     */

    public void setGpgKeyId(Long gpgKeyId) {
        this.gpgKeyId = gpgKeyId;
    }

    /**
     * Returns the gpg key primary keyid.
     *
     * @return the result
     */

    public String getGpgKeyPrimaryKeyid() {
        return gpgKeyPrimaryKeyid;
    }

    /**
     * Sets the gpg key primary keyid.
     *
     * @param gpgKeyPrimaryKeyid the gpg key primary keyid value
     */

    public void setGpgKeyPrimaryKeyid(String gpgKeyPrimaryKeyid) {
        this.gpgKeyPrimaryKeyid = gpgKeyPrimaryKeyid;
    }

    /**
     * Returns the gpg key user name.
     *
     * @return the result
     */

    public String getGpgKeyUserName() {
        return gpgKeyUserName;
    }

    /**
     * Sets the gpg key user name.
     *
     * @param gpgKeyUserName the gpg key user name value
     */

    public void setGpgKeyUserName(String gpgKeyUserName) {
        this.gpgKeyUserName = gpgKeyUserName;
    }

    /**
     * Returns the gpg key user email.
     *
     * @return the result
     */

    public String getGpgKeyUserEmail() {
        return gpgKeyUserEmail;
    }

    /**
     * Sets the gpg key user email.
     *
     * @param gpgKeyUserEmail the gpg key user email value
     */

    public void setGpgKeyUserEmail(String gpgKeyUserEmail) {
        this.gpgKeyUserEmail = gpgKeyUserEmail;
    }

    /**
     * Returns the verification status.
     *
     * @return the result
     */

    public String getVerificationStatus() {
        return verificationStatus;
    }

    /**
     * Sets the verification status.
     *
     * @param verificationStatus the verification status value
     */

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    /**
     * Returns the gpg key subkey id.
     *
     * @return the result
     */

    public String getGpgKeySubkeyId() {
        return gpgKeySubkeyId;
    }

    /**
     * Sets the gpg key subkey id.
     *
     * @param gpgKeySubkeyId the gpg key subkey id value
     */

    public void setGpgKeySubkeyId(String gpgKeySubkeyId) {
        this.gpgKeySubkeyId = gpgKeySubkeyId;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}

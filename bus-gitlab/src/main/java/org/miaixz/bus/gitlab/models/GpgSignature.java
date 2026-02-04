/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;
import java.io.Serial;

public class GpgSignature implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852255356008L;

    private Long gpgKeyId;
    private String gpgKeyPrimaryKeyid;
    private String gpgKeyUserName;
    private String gpgKeyUserEmail;
    private String verificationStatus;
    private String gpgKeySubkeyId;

    public Long getGpgKeyId() {
        return gpgKeyId;
    }

    public void setGpgKeyId(Long gpgKeyId) {
        this.gpgKeyId = gpgKeyId;
    }

    public String getGpgKeyPrimaryKeyid() {
        return gpgKeyPrimaryKeyid;
    }

    public void setGpgKeyPrimaryKeyid(String gpgKeyPrimaryKeyid) {
        this.gpgKeyPrimaryKeyid = gpgKeyPrimaryKeyid;
    }

    public String getGpgKeyUserName() {
        return gpgKeyUserName;
    }

    public void setGpgKeyUserName(String gpgKeyUserName) {
        this.gpgKeyUserName = gpgKeyUserName;
    }

    public String getGpgKeyUserEmail() {
        return gpgKeyUserEmail;
    }

    public void setGpgKeyUserEmail(String gpgKeyUserEmail) {
        this.gpgKeyUserEmail = gpgKeyUserEmail;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getGpgKeySubkeyId() {
        return gpgKeySubkeyId;
    }

    public void setGpgKeySubkeyId(String gpgKeySubkeyId) {
        this.gpgKeySubkeyId = gpgKeySubkeyId;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       ~
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
package org.miaixz.bus.gitlab.models;

import org.miaixz.bus.gitlab.support.JacksonJson;

import java.io.Serializable;

public class GpgSignature implements Serializable {
    private static final long serialVersionUID = -1L;

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

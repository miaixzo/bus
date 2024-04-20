/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.miaixz.bus.gitlab.support.JacksonJson;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractGroup<G extends AbstractGroup<G>> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String avatarUrl;
    private String webUrl;
    private String fullName;
    private String fullPath;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @SuppressWarnings("unchecked")
    public G withId(Long id) {
        this.id = id;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G withName(String name) {
        this.name = name;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G withAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G withWebUrl(String url) {
        this.webUrl = url;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G withFullName(String fullName) {
        this.fullName = fullName;
        return (G) this;
    }

    @SuppressWarnings("unchecked")
    public G withFullPath(String fullPath) {
        this.fullPath = fullPath;
        return (G) this;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}

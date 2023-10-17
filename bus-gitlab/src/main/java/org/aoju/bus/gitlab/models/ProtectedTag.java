/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org Greg Messner and other contributors.         *
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
package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.support.JacksonJson;

import java.util.List;

public class ProtectedTag {

    private String name;
    private List<CreateAccessLevel> createAccessLevels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CreateAccessLevel> getCreateAccessLevels() {
        return createAccessLevels;
    }

    public void setCreateAccessLevels(List<CreateAccessLevel> createAccessLevels) {
        this.createAccessLevels = createAccessLevels;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    public static class CreateAccessLevel {

        private AccessLevel access_level;
        private String accessLevelDescription;

        public AccessLevel getAccess_level() {
            return access_level;
        }

        public void setAccess_level(AccessLevel access_level) {
            this.access_level = access_level;
        }

        public String getAccessLevelDescription() {
            return accessLevelDescription;
        }

        public void setAccessLevelDescription(String accessLevelDescription) {
            this.accessLevelDescription = accessLevelDescription;
        }
    }
}

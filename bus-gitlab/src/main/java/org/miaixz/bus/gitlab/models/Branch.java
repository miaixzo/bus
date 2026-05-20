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
 * The branch class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Branch implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238379559L;

    private Commit commit;
    private Boolean developersCanMerge;
    private Boolean developersCanPush;
    private Boolean merged;
    private String name;
    private Boolean isProtected;
    private Boolean isDefault;
    private Boolean canPush;
    private String webUrl;

    /**
     * Returns the commit.
     *
     * @return the result
     */

    public Commit getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit the commit value
     */

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    /**
     * Returns the developers can merge.
     *
     * @return the result
     */

    public Boolean getDevelopersCanMerge() {
        return developersCanMerge;
    }

    /**
     * Sets the developers can merge.
     *
     * @param developersCanMerge the developers can merge value
     */

    public void setDevelopersCanMerge(Boolean developersCanMerge) {
        this.developersCanMerge = developersCanMerge;
    }

    /**
     * Returns the developers can push.
     *
     * @return the result
     */

    public Boolean getDevelopersCanPush() {
        return developersCanPush;
    }

    /**
     * Sets the developers can push.
     *
     * @param developersCanPush the developers can push value
     */

    public void setDevelopersCanPush(Boolean developersCanPush) {
        this.developersCanPush = developersCanPush;
    }

    /**
     * Returns the merged.
     *
     * @return the result
     */

    public Boolean getMerged() {
        return merged;
    }

    /**
     * Sets the merged.
     *
     * @param merged the merged value
     */

    public void setMerged(Boolean merged) {
        this.merged = merged;
    }

    /**
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the protected.
     *
     * @return the result
     */

    public Boolean getProtected() {
        return isProtected;
    }

    /**
     * Sets the protected.
     *
     * @param isProtected the is protected value
     */

    public void setProtected(Boolean isProtected) {
        this.isProtected = isProtected;
    }

    /**
     * Returns the default.
     *
     * @return the result
     */

    public Boolean getDefault() {
        return isDefault;
    }

    /**
     * Sets the default.
     *
     * @param isDefault the is default value
     */

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Returns the can push.
     *
     * @return the result
     */

    public Boolean getCanPush() {
        return canPush;
    }

    /**
     * Sets the can push.
     *
     * @param canPush the can push value
     */

    public void setCanPush(Boolean canPush) {
        this.canPush = canPush;
    }

    /**
     * Returns the web url.
     *
     * @return the result
     */

    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Sets the web url.
     *
     * @param webUrl the web url value
     */

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    /**
     * Returns whether the valid is enabled.
     *
     * @param branch the branch value
     * @return the result
     */

    public static final boolean isValid(Branch branch) {
        return (branch != null && branch.getName() != null);
    }

    /**
     * Sets the commit and returns this instance.
     *
     * @param commit the commit value
     * @return the result
     */

    public Branch withCommit(Commit commit) {
        this.commit = commit;
        return this;
    }

    /**
     * Sets the developers can merge and returns this instance.
     *
     * @param developersCanMerge the developers can merge value
     * @return the result
     */

    public Branch withDevelopersCanMerge(Boolean developersCanMerge) {
        this.developersCanMerge = developersCanMerge;
        return this;
    }

    /**
     * Sets the developers can push and returns this instance.
     *
     * @param developersCanPush the developers can push value
     * @return the result
     */

    public Branch withDevelopersCanPush(Boolean developersCanPush) {
        this.developersCanPush = developersCanPush;
        return this;
    }

    /**
     * Sets the merged and returns this instance.
     *
     * @param merged the merged value
     * @return the result
     */

    public Branch withMerged(Boolean merged) {
        this.merged = merged;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public Branch withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the is protected and returns this instance.
     *
     * @param isProtected the is protected value
     * @return the result
     */

    public Branch withIsProtected(Boolean isProtected) {
        this.isProtected = isProtected;
        return this;
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

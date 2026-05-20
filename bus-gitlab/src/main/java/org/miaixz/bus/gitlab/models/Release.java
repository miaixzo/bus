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
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The release class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Release implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852279301328L;

    private String name;
    private String tagName;
    private String description;
    private String descriptionHtml;
    private Date createdAt;
    private Date releasedAt;
    private Author author;
    private Commit commit;
    private List<Milestone> milestones;
    private String commitPath;
    private String tagPath;
    private String evidenceSha;
    private Assets assets;

    @JsonProperty("_links")
    private Map<String, String> links;

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
     * Returns the tag name.
     *
     * @return the result
     */

    public String getTagName() {
        return tagName;
    }

    /**
     * Sets the tag name.
     *
     * @param tagName the tag name value
     */

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the description html.
     *
     * @return the result
     */

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    /**
     * Sets the description html.
     *
     * @param descriptionHtml the description html value
     */

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the released at.
     *
     * @return the result
     */

    public Date getReleasedAt() {
        return releasedAt;
    }

    /**
     * Sets the released at.
     *
     * @param releasedAt the released at value
     */

    public void setReleasedAt(Date releasedAt) {
        this.releasedAt = releasedAt;
    }

    /**
     * Returns the author.
     *
     * @return the result
     */

    public Author getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author the author value
     */

    public void setAuthor(Author author) {
        this.author = author;
    }

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
     * Returns the milestones.
     *
     * @return the result
     */

    public List<Milestone> getMilestones() {
        return milestones;
    }

    /**
     * Sets the milestones.
     *
     * @param milestones the milestones value
     */

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    /**
     * Returns the commit path.
     *
     * @return the result
     */

    public String getCommitPath() {
        return commitPath;
    }

    /**
     * Sets the commit path.
     *
     * @param commitPath the commit path value
     */

    public void setCommitPath(String commitPath) {
        this.commitPath = commitPath;
    }

    /**
     * Returns the tag path.
     *
     * @return the result
     */

    public String getTagPath() {
        return tagPath;
    }

    /**
     * Sets the tag path.
     *
     * @param tagPath the tag path value
     */

    public void setTagPath(String tagPath) {
        this.tagPath = tagPath;
    }

    /**
     * Returns the evidence sha.
     *
     * @return the result
     */

    public String getEvidenceSha() {
        return evidenceSha;
    }

    /**
     * Sets the evidence sha.
     *
     * @param evidenceSha the evidence sha value
     */

    public void setEvidenceSha(String evidenceSha) {
        this.evidenceSha = evidenceSha;
    }

    /**
     * Returns the assets.
     *
     * @return the result
     */

    public Assets getAssets() {
        return assets;
    }

    /**
     * Sets the assets.
     *
     * @param assets the assets value
     */

    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    /**
     * Returns the links.
     *
     * @return the result
     */

    public Map<String, String> getLinks() {
        return links;
    }

    /**
     * Sets the links.
     *
     * @param links the links value
     */

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    /**
     * Returns the link by name.
     *
     * @param name the name value
     * @return the result
     */

    @JsonIgnore
    public String getLinkByName(String name) {
        if (links == null || links.isEmpty()) {
            return (null);
        }

        return (links.get(name));
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

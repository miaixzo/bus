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

/**
 * Params for getting file archive of the repository.
 */
public class RepositoryArchiveParams {

    private String sha;
    private String path;

    /**
     * Add param "The commit SHA to download".
     *
     * @param sha the commit SHA to download
     * @return current params with sha
     */
    public RepositoryArchiveParams withSha(String sha) {
        this.sha = sha;
        return this;
    }

    /**
     * Add param "The subpath of the repository to download".
     *
     * @param path the subpath of the repository to download
     * @return current params with path
     */
    public RepositoryArchiveParams withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Get form with params.
     *
     * @return form with params
     */
    public GitLabForm getForm() {
        return new GitLabForm().withParam("sha", sha).withParam("path", path);
    }

}

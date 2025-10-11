/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.auth.nimble.github;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Github authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum GithubScope implements AuthorizeScope {

    /**
     * Grants read/write access to public and private repository commit statuses. This scope is only necessary to grant
     * other users or services access to private repository commit statuses <em>without</em> granting access to the
     * code. The meaning of {@code scope} is subject to {@code description}.
     */
    REPO_STATUS("repo:status",
            "Grants read/write access to public and private repository commit statuses. This scope is only necessary to grant other users or services access to private repository commit statuses <em>without</em> granting access to the code.",
            false),
    /**
     * Grants access to deployment statuses for public and private repositories. This scope is only necessary to grant
     * other users or services access to deployment statuses, <em>without</em> granting access to the code.
     */
    REPO_DEPLOYMENT("repo_deployment",
            "Grants access to deployment statuses for public and private repositories. This scope is only necessary to grant other users or services access to deployment statuses, <em>without</em> granting access to the code.",
            false),
    /**
     * Limits access to public repositories. That includes read/write access to code, commit statuses, repository
     * projects, collaborators, and deployment statuses for public repositories and organizations. Also required for
     * starring public repositories.
     */
    PUBLIC_REPO("public_repo",
            "Limits access to public repositories. That includes read/write access to code, commit statuses, repository projects, collaborators, and deployment statuses for public repositories and organizations. Also required for starring public repositories.",
            false),
    /**
     * Grants accept/decline abilities for invitations to collaborate on a repository. This scope is only necessary to
     * grant other users or services access to invites <em>without</em> granting access to the code.
     */
    REPO_INVITE("repo:invite",
            "Grants accept/decline abilities for invitations to collaborate on a repository. This scope is only necessary to grant other users or services access to invites <em>without</em> granting access to the code.",
            false),
    /**
     * Grants read and write access to security events in the code scanning API.
     */
    SECURITY_EVENTS("security_events", "Grants read and write access to security events in the code scanning API.",
            false),
    /**
     * Grants read, write, and ping access to hooks in public or private repositories.
     */
    WRITE_REPO_HOOK("write:repo_hook",
            "Grants read, write, and ping access to hooks in public or private repositories.", false),
    /**
     * Grants read and ping access to hooks in public or private repositories.
     */
    READ_REPO_HOOK("read:repo_hook", "Grants read and ping access to hooks in public or private repositories.", false),
    /**
     * Fully manage the organization and its teams, projects, and memberships.
     */
    ADMIN_ORG("admin:org", "Fully manage the organization and its teams, projects, and memberships.", false),
    /**
     * Read and write access to organization membership, organization projects, and team membership.
     */
    WRITE_ORG("write:org",
            "Read and write access to organization membership, organization projects, and team membership.", false),
    /**
     * Read-only access to organization membership, organization projects, and team membership.
     */
    READ_ORG("read:org", "Read-only access to organization membership, organization projects, and team membership.",
            false),
    /**
     * Fully manage public keys.
     */
    ADMIN_PUBLIC_KEY("admin:public_key", "Fully manage public keys.", false),
    /**
     * Create, list, and view details for public keys.
     */
    WRITE_PUBLIC_KEY("write:public_key", "Create, list, and view details for public keys.", false),
    /**
     * List and view details for public keys.
     */
    READ_PUBLIC_KEY("read:public_key", "List and view details for public keys.", false),
    /**
     * Grants write access to gists.
     */
    GIST("gist", "Grants write access to gists.", false),
    /**
     * Grants: <br>
     * * read access to a user's notifications <br>
     * * mark as read access to threads <br>
     * * watch and unwatch access to a repository, and <br>
     * * read, write, and delete access to thread subscriptions.
     */
    NOTIFICATIONS("notifications",
            "Grants: <br>* read access to a user's notifications <br>* mark as read access to threads <br>* watch and unwatch access to a repository, and <br>* read, write, and delete access to thread subscriptions.",
            false),
    /**
     * Grants read/write access to profile info only. Note that this scope includes <code>user:email</code> and
     * <code>user:follow</code>.
     */
    USER("user",
            "Grants read/write access to profile info only.  Note that this scope includes <code>user:email</code> and <code>user:follow</code>.",
            false),
    /**
     * Grants access to read a user's profile data.
     */
    READ_USER("read:user", "Grants access to read a user's profile data.", false),
    /**
     * Grants read access to a user's email addresses.
     */
    USER_EMAIL("user:email", "Grants read access to a user's email addresses.", false),
    /**
     * Grants access to follow or unfollow other users.
     */
    USER_FOLLOW("user:follow", "Grants access to follow or unfollow other users.", false),
    /**
     * Grants access to delete adminable repositories.
     */
    DELETE_REPO("delete_repo", "Grants access to delete adminable repositories.", false),
    /**
     * Allows read and write access for team discussions.
     */
    WRITE_DISCUSSION("write:discussion", "Allows read and write access for team discussions.", false),
    /**
     * Allows read access for team discussions.
     */
    READ_DISCUSSION("read:discussion", "Allows read access for team discussions.", false),
    /**
     * Grants access to upload or publish a package in GitHub Packages. For more information, see
     * "<a href="https://help.github.com/github/managing-packages-with-github-packages/publishing-a-package">Publishing
     * a package</a>" in the GitHub Help documentation.
     */
    WRITE_PACKAGES("write:packages",
            "Grants access to upload or publish a package in GitHub Packages. For more information, see \"<a href=\"https://help.github.com/github/managing-packages-with-github-packages/publishing-a-package\">Publishing a package</a>\" in the GitHub Help documentation.",
            false),
    /**
     * Grants access to download or install packages from GitHub Packages. For more information, see
     * "<a href="https://help.github.com/github/managing-packages-with-github-packages/installing-a-package">Installing
     * a package</a>" in the GitHub Help documentation.
     */
    READ_PACKAGES("read:packages",
            "Grants access to download or install packages from GitHub Packages. For more information, see \"<a href=\"https://help.github.com/github/managing-packages-with-github-packages/installing-a-package\">Installing a package</a>\" in the GitHub Help documentation.",
            false),
    /**
     * Grants access to delete packages from GitHub Packages. For more information, see
     * "<a href="https://help.github.com/github/managing-packages-with-github-packages/deleting-a-package">Deleting
     * packages</a>" in the GitHub Help documentation.
     */
    DELETE_PACKAGES("delete:packages",
            "Grants access to delete packages from GitHub Packages. For more information, see \"<a href=\"https://help.github.com/github/managing-packages-with-github-packages/deleting-a-package\">Deleting packages</a>\" in the GitHub Help documentation.",
            false),
    /**
     * Fully manage GPG keys.
     */
    ADMIN_GPG_KEY("admin:gpg_key", "Fully manage GPG keys.", false),
    /**
     * Create, list, and view details for GPG keys.
     */
    WRITE_GPG_KEY("write:gpg_key", "Create, list, and view details for GPG keys.", false),
    /**
     * List and view details for GPG keys.
     */
    READ_GPG_KEY("read:gpg_key", "List and view details for GPG keys.", false),
    /**
     * Grants the ability to add and update GitHub Actions workflow files. Workflow files can be committed without this
     * scope if the same file (with both the same path and contents) exists on another branch in the same repository.
     */
    WORKFLOW("workflow",
            "Grants the ability to add and update GitHub Actions workflow files. Workflow files can be committed without this scope if the same file (with both the same path and contents) exists on another branch in the same repository.",
            false);

    /**
     * The scope string as defined by Github.
     */
    private final String scope;
    /**
     * A description of what the scope grants access to.
     */
    private final String description;
    /**
     * Indicates if this scope is enabled by default.
     */
    private final boolean isDefault;

}

/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.oauth.metric;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 各个平台 scope 类的统一接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OauthScope {
    /**
     * 百度 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Baidu implements Scope {

        BASIC("basic", "用户基本权限，可以获取用户的基本信息 。", true),
        SUPER_MSG("super_msg", "往用户的百度首页上发送消息提醒，相关API任何应用都能使用，但要想将消息提醒在百度首页显示，需要第三方在注册应用时额外填写相关信息。", false),
        NETDISK("netdisk", "获取用户在个人云存储中存放的数据。", false),
        PUBLIC("public", "可以访问公共的开放API。", false),
        HAO123("hao123", "可以访问Hao123 提供的开放API接口。该权限需要申请开通，请将具体的理由和用途发邮件给tuangou@baidu.com。", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Amazon 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Amazon implements Scope {

        R_LITEPROFILE("profile", "The profile scope includes a user's name and email address", true),
        R_EMAILADDRESS("profile:user_id", "The profile:user_id scope only includes the user_id field of the profile", true),
        W_MEMBER_SOCIAL("postal_code", "This includes the user's zip/postal code number from their primary shipping address", true);

        private final String scope;
        private final String description;
        private final boolean isDefault;

    }

    /**
     * Coding 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Coding implements Scope {

        USER("user", "读取用户的基本信息", false),
        USER_EMAIL("user:email", "读取用户的邮件", false),
        USER_PHONE("user:phone", "读取用户的手机号", false),
        PROJECT("project", "授权项目信息、项目列表，仓库信息，公钥列表、成员", false),
        PROJECT_DEPOT("project:depot", "完整的仓库控制权限", false),
        PROJECT_WIKI("project:wiki", "授权读取与操作 wiki", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 抖音平台 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Douyin implements Scope {

        /**
         * 无需申请	默认开启
         */
        USER_INFO("user_info", "返回抖音用户公开信息", true),
        /**
         * 无需申请	默认开启
         */
        AWEME_SHARE("aweme.share", "抖音分享", false),
        /**
         * 普通权限,管理中心申请
         */
        IM_SHARE("im.share", "分享给抖音好友", false),
        RENEW_REFRESH_TOKEN("renew_refresh_token", "授权有效期动态续期", false),
        FOLLOWING_LIST("following.list", "获取该用户的关注列表", false),
        FANS_LIST("fans.list", "获取该用户的粉丝列表", false),
        VIDEO_CREATE("video.create", "视频发布及管理", false),
        VIDEO_DELETE("video.delete", "删除内容", false),
        VIDEO_DATA("video.data", "查询授权用户的抖音视频数据", false),
        VIDEO_LIST("video.list", "查询特定抖音视频的视频数据", false),
        /**
         * 特殊权限	默认关闭	管理中心申请
         */
        SHARE_WITH_SOURCE("share_with_source", "分享携带来源标签，用户可点击标签进入转化页", false),
        MOBILE("mobile", "用抖音帐号登录第三方平台，获得用户在抖音上的手机号码", false),
        MOBILE_ALERT("mobile_alert", "用抖音帐号登录第三方平台，获得用户在抖音上的手机号码", false),
        VIDEO_SEARCH("video.search", "关键词视频管理", false),
        POI_SEARCH("poi.search", "查询POI信息", false),
        LOGIN_ID("login_id", "静默授权直接获取该用户的open id", false),
        /**
         * 抖音数据权限, 默认关闭, 管理中心申请
         */
        DATA_EXTERNAL_USER("data.external.user", "查询用户的获赞、评论、分享，主页访问等相关数据", false),
        DATA_EXTERNAL_ITEM("data.external.item", "查询作品的获赞，评论，分享等相关数据", false),
        FANS_DATA("fans.data", "获取用户粉丝画像数据", false),
        HOTSEARCH("hotsearch", "获取抖音热门内容", false),
        STAR_TOP_SCORE_DISPLAY("star_top_score_display", "星图达人与达人对应各指数评估分，以及星图6大热门维度下的达人榜单", false),
        STAR_TOPS("star_tops", "星图达人与达人对应各指数评估分，以及星图6大热门维度下的达人榜单", false),
        STAR_AUTHOR_SCORE_DISPLAY("star_author_score_display", "星图达人与达人对应各指数评估分，以及星图6大热门维度下的达人榜单", false),
        notes("data.external.sdk_share", "获取用户通过分享SDK分享视频数据", false),
        /**
         * 定向开通	默认关闭	定向开通
         */
        DISCOVERY_ENT("discovery.ent", "查询抖音电影榜、抖音剧集榜、抖音综艺榜数据", false);

        private final String scope;
        private final String description;
        private final boolean isDefault;

    }

    /**
     * Facebook 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Facebook implements Scope {

        EMAIL("email", "获取用户的邮箱", true),
        USER_AGE_RANGE("user_age_range", "允许应用程序访问用户的年龄范围", true),
        USER_BIRTHDAY("user_birthday", "获取用户的生日", true),
        USER_FRIENDS("user_friends", "获取用户的好友列表", true),
        USER_GENDER("user_gender", "获取用户的性别", true),
        USER_HOMETOWN("user_hometown", "获取用户的家乡信息", true),
        USER_LIKES("user_likes", "获取用户的喜欢列表", true),
        USER_LINK("user_link", "获取用户的个人链接", true),
        USER_LOCATION("user_location", "获取用户的位置信息", true),
        USER_PHOTOS("user_photos", "获取用户的相册信息", true),
        USER_POSTS("user_posts", "获取用户发布的内容", true),
        USER_VIDEOS("user_videos", "获取用户上传的视频信息", true),
        GROUPS_ACCESS_MEMBER_INFO("groups_access_member_info", "获取公开的群组成员信息", false),
        PUBLISH_TO_GROUPS("publish_to_groups", "授权您的应用程序代表某人将内容发布到组中，前提是他们已经授予您的应用程序访问权限", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Gitee 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Gitee implements Scope {

        USER_INFO("user_info", "访问用户的个人信息、最新动态等", true),
        PROJECTS("projects", "查看、创建、更新用户的项目", false),
        PULL_REQUESTS("pull_requests", "查看、发布、更新用户的 Pull Request", false),
        ISSUES("issues", "查看、发布、更新用户的 Issue", false),
        NOTES("notes", "查看、发布、管理用户在项目、代码片段中的评论", false),
        KEYS("keys", "查看、部署、删除用户的公钥", false),
        HOOK("hook", "查看、部署、更新用户的 Webhook", false),
        GROUPS("groups", "查看、管理用户的组织以及成员", false),
        GISTS("gists", "查看、删除、更新用户的代码片段", false),
        ENTERPRISES("enterprises", "查看、管理用户的企业以及成员", false),
        EMAILS("emails", "查看用户的个人邮箱信息", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Github 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Github implements Scope {

        REPO_STATUS("repo:status", "Grants read/write access to public and private repository commit statuses. This scope is only necessary to grant other users or services access to private repository commit statuses <em>without</em> granting access to the code.", false),
        REPO_DEPLOYMENT("repo_deployment", "Grants access to deployment statuses for public and private repositories. This scope is only necessary to grant other users or services access to deployment statuses, <em>without</em> granting access to the code.", false),
        PUBLIC_REPO("public_repo", "Limits access to public repositories. That includes read/write access to code, commit statuses, repository projects, collaborators, and deployment statuses for public repositories and organizations. Also required for starring public repositories.", false),
        REPO_INVITE("repo:invite", "Grants accept/decline abilities for invitations to collaborate on a repository. This scope is only necessary to grant other users or services access to invites <em>without</em> granting access to the code.", false),
        SECURITY_EVENTS("security_events", "Grants read and write access to security events in the code scanning API.", false),
        WRITE_REPO_HOOK("write:repo_hook", "Grants read, write, and ping access to hooks in public or private repositories.", false),
        READ_REPO_HOOK("read:repo_hook", "Grants read and ping access to hooks in public or private repositories.", false),
        ADMIN_ORG("admin:org", "Fully manage the organization and its teams, projects, and memberships.", false),
        WRITE_ORG("write:org", "Read and write access to organization membership, organization projects, and team membership.", false),
        READ_ORG("read:org", "Read-only access to organization membership, organization projects, and team membership.", false),
        ADMIN_PUBLIC_KEY("admin:public_key", "Fully manage public keys.", false),
        WRITE_PUBLIC_KEY("write:public_key", "Create, list, and view details for public keys.", false),
        READ_PUBLIC_KEY("read:public_key", "List and view details for public keys.", false),
        GIST("gist", "Grants write access to gists.", false),
        NOTIFICATIONS("notifications", "Grants: <br>* read access to a user's notifications <br>* mark as read access to threads <br>* watch and unwatch access to a repository, and <br>* read, write, and delete access to thread subscriptions.", false),
        USER("user", "Grants read/write access to profile info only.  Note that this scope includes <code>user:email</code> and <code>user:follow</code>.", false),
        READ_USER("read:user", "Grants access to read a user's profile data.", false),
        USER_EMAIL("user:email", "Grants read access to a user's email addresses.", false),
        USER_FOLLOW("user:follow", "Grants access to follow or unfollow other users.", false),
        DELETE_REPO("delete_repo", "Grants access to delete adminable repositories.", false),
        WRITE_DISCUSSION("write:discussion", "Allows read and write access for team discussions.", false),
        READ_DISCUSSION("read:discussion", "Allows read access for team discussions.", false),
        WRITE_PACKAGES("write:packages", "Grants access to upload or publish a package in GitHub Packages. For more information, see \"<a href=\"https://help.github.com/github/managing-packages-with-github-packages/publishing-a-package\">Publishing a package</a>\" in the GitHub Help documentation.", false),
        READ_PACKAGES("read:packages", "Grants access to download or install packages from GitHub Packages. For more information, see \"<a href=\"https://help.github.com/github/managing-packages-with-github-packages/installing-a-package\">Installing a package</a>\" in the GitHub Help documentation.", false),
        DELETE_PACKAGES("delete:packages", "Grants access to delete packages from GitHub Packages. For more information, see \"<a href=\"https://help.github.com/github/managing-packages-with-github-packages/deleting-a-package\">Deleting packages</a>\" in the GitHub Help documentation.", false),
        ADMIN_GPG_KEY("admin:gpg_key", "Fully manage GPG keys.", false),
        WRITE_GPG_KEY("write:gpg_key", "Create, list, and view details for GPG keys.", false),
        READ_GPG_KEY("read:gpg_key", "List and view details for GPG keys.", false),
        WORKFLOW("workflow", "Grants the ability to add and update GitHub Actions workflow files. Workflow files can be committed without this scope if the same file (with both the same path and contents) exists on another branch in the same repository.", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Gitlab 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Gitlab implements Scope {

        READ_USER("read_user", "Grants read-only access to the authenticated user's profile through the /user API endpoint, which includes username, public email, and full name. Also grants access to read-only API endpoints under /users.", true),
        OPENID("openid", "Grants permission to authenticate with GitLab using OpenID Connect. Also gives read-only access to the user's profile and group memberships.", true),
        PROFILE("profile", "Grants read-only access to the user's profile data using OpenID Connect.", true),
        EMAIL("email", "Grants read-only access to the user's primary email address using OpenID Connect.", true),
        READ_API("read_api", "Grants read access to the API, including all groups and projects, the container registry, and the package registry.", false),
        READ_REPOSITORY("read_repository", "Grants read-only access to repositories on private projects using Git-over-HTTP or the Repository Files API.", false),
        WRITE_REPOSITORY("write_repository", "Grants read-write access to repositories on private projects using Git-over-HTTP (not using the API).", false),
        READ_REGISTRY("read_registry", "Grants read-only access to container registry images on private projects.", false),
        WRITE_REGISTRY("write_registry", "<span title=\"translation missing: en.doorkeeper.scope_desc.write_registry\">Write Registry</span>", false),
        SUDO("sudo", "Grants permission to perform API actions as any user in the system, when authenticated as an admin user.", false),
        API("api", "Grants complete read/write access to the API, including all groups and projects, the container registry, and the package registry.", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Google 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Google implements Scope {
        USER_OPENID("openid", "Associate you with your personal info on Google", true),
        USER_EMAIL("email", "View your email address", true),
        USER_PROFILE("profile", "View your basic profile info", true),
        USER_PHONENUMBERS_READ("https://www.googleapis.com/auth/user.phonenumbers.read", "View your phone numbers", false),
        USER_ORGANIZATION_READ("https://www.googleapis.com/auth/user.organization.read", "See your education, work history and org info", false),
        USER_GENDER_READ("https://www.googleapis.com/auth/user.gender.read", "See your gender", false),
        USER_EMAILS_READ("https://www.googleapis.com/auth/user.emails.read", "View your email addresses", false),
        USER_BIRTHDAY_READ("https://www.googleapis.com/auth/user.birthday.read", "View your complete date of birth", false),
        USER_ADDRESSES_READ("https://www.googleapis.com/auth/user.addresses.read", "View your street addresses", false),
        USERINFO_PROFILE("https://www.googleapis.com/auth/userinfo.profile", "See your personal info, including any personal info you've made publicly available", false),
        USERINFO_EMAIL("https://www.googleapis.com/auth/userinfo.email", "View your email address", false),
        YT_ANALYTICS_READONLY("https://www.googleapis.com/auth/yt-analytics.readonly", "View YouTube Analytics reports for your YouTube content", false),
        YT_ANALYTICS_MONETARY_READONLY("https://www.googleapis.com/auth/yt-analytics-monetary.readonly", "View monetary and non-monetary YouTube Analytics reports for your YouTube content", false),
        YOUTUBEPARTNER_CHANNEL_AUDIT("https://www.googleapis.com/auth/youtubepartner-channel-audit", "View private information of your YouTube channel relevant during the audit process with a YouTube partner", false),
        YOUTUBEPARTNER("https://www.googleapis.com/auth/youtubepartner", "View and manage your assets and associated content on YouTube", false),
        YOUTUBE_UPLOAD("https://www.googleapis.com/auth/youtube.upload", "Manage your YouTube videos", false),
        YOUTUBE_READONLY("https://www.googleapis.com/auth/youtube.readonly", "View your YouTube account", false),
        YOUTUBE_FORCE_SSL("https://www.googleapis.com/auth/youtube.force-ssl", "See, edit, and permanently delete your YouTube videos, ratings, comments and captions", false),
        YOUTUBE_CHANNEL_MEMBERSHIPS_CREATOR("https://www.googleapis.com/auth/youtube.channel-memberships.creator", "See a list of your current active channel members, their current level, and when they became a member", false),
        YOUTUBE("https://www.googleapis.com/auth/youtube", "Manage your YouTube account", false),
        WEBMASTERS_READONLY("https://www.googleapis.com/auth/webmasters.readonly", "View Search Console data for your verified sites", false),
        WEBMASTERS("https://www.googleapis.com/auth/webmasters", "View and manage Search Console data for your verified sites", false),
        VERIFIEDACCESS("https://www.googleapis.com/auth/verifiedaccess", "Verify your enterprise credentials", false),
        TRACE_APPEND("https://www.googleapis.com/auth/trace.append", "Write Trace data for a project or application", false),
        TASKS_READONLY("https://www.googleapis.com/auth/tasks.readonly", "View your tasks", false),
        TASKS("https://www.googleapis.com/auth/tasks", "Create, edit, organize, and delete all your tasks", false),
        TAGMANAGER_READONLY("https://www.googleapis.com/auth/tagmanager.readonly", "View your Google Tag Manager container and its subcomponents", false),
        TAGMANAGER_PUBLISH("https://www.googleapis.com/auth/tagmanager.publish", "Publish your Google Tag Manager container versions", false),
        TAGMANAGER_MANAGE_USERS("https://www.googleapis.com/auth/tagmanager.manage.users", "Manage user permissions of your Google Tag Manager account and container", false),
        TAGMANAGER_MANAGE_ACCOUNTS("https://www.googleapis.com/auth/tagmanager.manage.accounts", "View and manage your Google Tag Manager accounts", false),
        TAGMANAGER_EDIT_CONTAINERVERSIONS("https://www.googleapis.com/auth/tagmanager.edit.containerversions", "Manage your Google Tag Manager container versions", false),
        TAGMANAGER_EDIT_CONTAINERS("https://www.googleapis.com/auth/tagmanager.edit.containers", "Manage your Google Tag Manager container and its subcomponents, excluding versioning and publishing", false),
        TAGMANAGER_DELETE_CONTAINERS("https://www.googleapis.com/auth/tagmanager.delete.containers", "Delete your Google Tag Manager containers", false),
        STREETVIEWPUBLISH("https://www.googleapis.com/auth/streetviewpublish", "Publish and manage your 360 photos on Google Street View", false),
        SQLSERVICE_ADMIN("https://www.googleapis.com/auth/sqlservice.admin", "Manage your Google SQL Service instances", false),
        SPREADSHEETS_READONLY("https://www.googleapis.com/auth/spreadsheets.readonly", "View your Google Spreadsheets", false),
        SPREADSHEETS("https://www.googleapis.com/auth/spreadsheets", "See, edit, create, and delete your spreadsheets in Google Drive", false),
        SPANNER_DATA("https://www.googleapis.com/auth/spanner.data", "View and manage the contents of your Spanner databases", false),
        SPANNER_ADMIN("https://www.googleapis.com/auth/spanner.admin", "Administer your Spanner databases", false),
        SOURCE_READ_WRITE("https://www.googleapis.com/auth/source.read_write", "Manage the contents of your source code repositories", false),
        SOURCE_READ_ONLY("https://www.googleapis.com/auth/source.read_only", "View the contents of your source code repositories", false),
        SOURCE_FULL_CONTROL("https://www.googleapis.com/auth/source.full_control", "Manage your source code repositories", false),
        SITEVERIFICATION_VERIFY_ONLY("https://www.googleapis.com/auth/siteverification.verify_only", "Manage your new site verifications with Google", false),
        SITEVERIFICATION("https://www.googleapis.com/auth/siteverification", "Manage the list of sites and domains you control", false),
        SERVICECONTROL("https://www.googleapis.com/auth/servicecontrol", "Manage your Google Service Control data", false),
        SERVICE_MANAGEMENT_READONLY("https://www.googleapis.com/auth/service.management.readonly", "View your Google API service configuration", false),
        SERVICE_MANAGEMENT("https://www.googleapis.com/auth/service.management", "Manage your Google API service configuration", false),
        SCRIPT_PROJECTS_READONLY("https://www.googleapis.com/auth/script.projects.readonly", "View Google Apps Script projects", false),
        SCRIPT_PROJECTS("https://www.googleapis.com/auth/script.projects", "Create and update Google Apps Script projects", false),
        SCRIPT_PROCESSES("https://www.googleapis.com/auth/script.processes", "View Google Apps Script processes", false),
        SCRIPT_METRICS("https://www.googleapis.com/auth/script.metrics", "View Google Apps Script project's metrics", false),
        SCRIPT_DEPLOYMENTS_READONLY("https://www.googleapis.com/auth/script.deployments.readonly", "View Google Apps Script deployments", false),
        SCRIPT_DEPLOYMENTS("https://www.googleapis.com/auth/script.deployments", "Create and update Google Apps Script deployments", false),
        PUBSUB("https://www.googleapis.com/auth/pubsub", "View and manage Pub/Sub topics and subscriptions", false),
        PRESENTATIONS_READONLY("https://www.googleapis.com/auth/presentations.readonly", "View your Google Slides presentations", false),
        PRESENTATIONS("https://www.googleapis.com/auth/presentations", "View and manage your Google Slides presentations", false),
        PHOTOSLIBRARY_SHARING("https://www.googleapis.com/auth/photoslibrary.sharing", "Manage and add to shared albums on your behalf", false),
        PHOTOSLIBRARY_READONLY_APPCREATEDDATA("https://www.googleapis.com/auth/photoslibrary.readonly.appcreateddata", "Manage photos added by this app", false),
        PHOTOSLIBRARY_READONLY("https://www.googleapis.com/auth/photoslibrary.readonly", "View your Google Photos library", false),
        PHOTOSLIBRARY_APPENDONLY("https://www.googleapis.com/auth/photoslibrary.appendonly", "Add to your Google Photos library", false),
        PHOTOSLIBRARY("https://www.googleapis.com/auth/photoslibrary", "View and manage your Google Photos library", false),
        NDEV_CLOUDMAN_READONLY("https://www.googleapis.com/auth/ndev.cloudman.readonly", "View your Google Cloud Platform management resources and deployment status information", false),
        NDEV_CLOUDMAN("https://www.googleapis.com/auth/ndev.cloudman", "View and manage your Google Cloud Platform management resources and deployment status information", false),
        NDEV_CLOUDDNS_READWRITE("https://www.googleapis.com/auth/ndev.clouddns.readwrite", "View and manage your DNS records hosted by Google Cloud DNS", false),
        NDEV_CLOUDDNS_READONLY("https://www.googleapis.com/auth/ndev.clouddns.readonly", "View your DNS records hosted by Google Cloud DNS", false),
        MONITORING_WRITE("https://www.googleapis.com/auth/monitoring.write", "Publish metric data to your Google Cloud projects", false),
        MONITORING_READ("https://www.googleapis.com/auth/monitoring.read", "View monitoring data for all of your Google Cloud and third-party projects", false),
        MONITORING("https://www.googleapis.com/auth/monitoring", "View and write monitoring data for all of your Google and third-party Cloud and API projects", false),
        MANUFACTURERCENTER("https://www.googleapis.com/auth/manufacturercenter", "Manage your product listings for Google Manufacturer Center", false),
        LOGGING_WRITE("https://www.googleapis.com/auth/logging.write", "Submit log data for your projects", false),
        LOGGING_READ("https://www.googleapis.com/auth/logging.read", "View log data for your projects", false),
        LOGGING_ADMIN("https://www.googleapis.com/auth/logging.admin", "Administrate log data for your projects", false),
        JOBS("https://www.googleapis.com/auth/jobs", "Manage job postings", false),
        INDEXING("https://www.googleapis.com/auth/indexing", "Submit data to Google for indexing", false),
        GROUPS("https://www.googleapis.com/auth/groups", "View and manage your Google Groups", false),
        GMAIL("https://mail.google.com/", "Read, compose, send, and permanently delete all your email from Gmail", false),
        GMAIL_SETTINGS_SHARING("https://www.googleapis.com/auth/gmail.settings.sharing", "Manage your sensitive mail settings, including who can manage your mail", false),
        GMAIL_SETTINGS_BASIC("https://www.googleapis.com/auth/gmail.settings.basic", "Manage your basic mail settings", false),
        GMAIL_SEND("https://www.googleapis.com/auth/gmail.send", "Send email on your behalf", false),
        GMAIL_READONLY("https://www.googleapis.com/auth/gmail.readonly", "View your email messages and settings", false),
        GMAIL_MODIFY("https://www.googleapis.com/auth/gmail.modify", "View and modify but not delete your email", false),
        GMAIL_METADATA("https://www.googleapis.com/auth/gmail.metadata", "View your email message metadata such as labels and headers, but not the email body", false),
        GMAIL_LABELS("https://www.googleapis.com/auth/gmail.labels", "Manage mailbox labels", false),
        GMAIL_INSERT("https://www.googleapis.com/auth/gmail.insert", "Insert mail into your mailbox", false),
        GMAIL_COMPOSE("https://www.googleapis.com/auth/gmail.compose", "Manage drafts and send emails", false),
        GMAIL_ADDONS_CURRENT_MESSAGE_READONLY("https://www.googleapis.com/auth/gmail.addons.current.message.readonly", "View your email messages when the add-on is running", false),
        GMAIL_ADDONS_CURRENT_MESSAGE_METADATA("https://www.googleapis.com/auth/gmail.addons.current.message.metadata", "View your email message metadata when the add-on is running", false),
        GMAIL_ADDONS_CURRENT_MESSAGE_ACTION("https://www.googleapis.com/auth/gmail.addons.current.message.action", "View your email messages when you interact with the add-on", false),
        GMAIL_ADDONS_CURRENT_ACTION_COMPOSE("https://www.googleapis.com/auth/gmail.addons.current.action.compose", "Manage drafts and send emails when you interact with the add-on", false),
        GENOMICS("https://www.googleapis.com/auth/genomics", "View and manage Genomics data", false),
        GAMES("https://www.googleapis.com/auth/games", "Create, edit, and delete your Google Play Games activity", false),
        FORMS_CURRENTONLY("https://www.googleapis.com/auth/forms.currentonly", "View and manage forms that this application has been installed in", false),
        FORMS("https://www.googleapis.com/auth/forms", "View and manage your forms in Google Drive", false),
        FITNESS_REPRODUCTIVE_HEALTH_WRITE("https://www.googleapis.com/auth/fitness.reproductive_health.write", "See and add info about your reproductive health in Google Fit. I consent to Google sharing my reporductive health information with this app.", false),
        FITNESS_REPRODUCTIVE_HEALTH_READ("https://www.googleapis.com/auth/fitness.reproductive_health.read", "See info about your reproductive health in Google Fit. I consent to Google sharing my reporductive health information with this app.", false),
        FITNESS_OXYGEN_SATURATION_WRITE("https://www.googleapis.com/auth/fitness.oxygen_saturation.write", "See and add info about your oxygen saturation in Google Fit. I consent to Google sharing my oxygen saturation information with this app.", false),
        FITNESS_OXYGEN_SATURATION_READ("https://www.googleapis.com/auth/fitness.oxygen_saturation.read", "See info about your oxygen saturation in Google Fit. I consent to Google sharing my oxygen saturation information with this app.", false),
        FITNESS_NUTRITION_WRITE("https://www.googleapis.com/auth/fitness.nutrition.write", "See and add to info about your nutrition in Google Fit", false),
        FITNESS_NUTRITION_READ("https://www.googleapis.com/auth/fitness.nutrition.read", "See info about your nutrition in Google Fit", false),
        FITNESS_LOCATION_WRITE("https://www.googleapis.com/auth/fitness.location.write", "See and add to your Google Fit location data", false),
        FITNESS_LOCATION_READ("https://www.googleapis.com/auth/fitness.location.read", "See your Google Fit speed and distance data", false),
        FITNESS_BODY_TEMPERATURE_WRITE("https://www.googleapis.com/auth/fitness.body_temperature.write", "See and add to info about your body temperature in Google Fit. I consent to Google sharing my body temperature information with this app.", false),
        FITNESS_BODY_TEMPERATURE_READ("https://www.googleapis.com/auth/fitness.body_temperature.read", "See info about your body temperature in Google Fit. I consent to Google sharing my body temperature information with this app.", false),
        FITNESS_BODY_WRITE("https://www.googleapis.com/auth/fitness.body.write", "See and add info about your body measurements and heart rate to Google Fit", false),
        FITNESS_BODY_READ("https://www.googleapis.com/auth/fitness.body.read", "See info about your body measurements and heart rate in Google Fit", false),
        FITNESS_BLOOD_PRESSURE_WRITE("https://www.googleapis.com/auth/fitness.blood_pressure.write", "See and add info about your blood pressure in Google Fit. I consent to Google sharing my blood pressure information with this app.", false),
        FITNESS_BLOOD_PRESSURE_READ("https://www.googleapis.com/auth/fitness.blood_pressure.read", "See info about your blood pressure in Google Fit. I consent to Google sharing my blood pressure information with this app.", false),
        FITNESS_BLOOD_GLUCOSE_WRITE("https://www.googleapis.com/auth/fitness.blood_glucose.write", "See and add info about your blood glucose to Google Fit. I consent to Google sharing my blood glucose information with this app.", false),
        FITNESS_BLOOD_GLUCOSE_READ("https://www.googleapis.com/auth/fitness.blood_glucose.read", "See info about your blood glucose in Google Fit. I consent to Google sharing my blood glucose information with this app.", false),
        FITNESS_ACTIVITY_WRITE("https://www.googleapis.com/auth/fitness.activity.write", "See and add to your Google Fit physical activity data", false),
        FITNESS_ACTIVITY_READ("https://www.googleapis.com/auth/fitness.activity.read", "Use Google Fit to see and store your physical activity data", false),
        FIREBASE_READONLY("https://www.googleapis.com/auth/firebase.readonly", "View all your Firebase data and settings", false),
        FIREBASE("https://www.googleapis.com/auth/firebase", "View and administer all your Firebase data and settings", false),
        EDISCOVERY_READONLY("https://www.googleapis.com/auth/ediscovery.readonly", "View your eDiscovery data", false),
        EDISCOVERY("https://www.googleapis.com/auth/ediscovery", "Manage your eDiscovery data", false),
        DRIVE_SCRIPTS("https://www.googleapis.com/auth/drive.scripts", "Modify your Google Apps Script scripts' behavior", false),
        DRIVE_READONLY("https://www.googleapis.com/auth/drive.readonly", "See and download all your Google Drive files", false),
        DRIVE_PHOTOS_READONLY("https://www.googleapis.com/auth/drive.photos.readonly", "View the photos, videos and albums in your Google Photos", false),
        DRIVE_METADATA_READONLY("https://www.googleapis.com/auth/drive.metadata.readonly", "View metadata for files in your Google Drive", false),
        DRIVE_METADATA("https://www.googleapis.com/auth/drive.metadata", "View and manage metadata of files in your Google Drive", false),
        DRIVE_FILE("https://www.googleapis.com/auth/drive.file", "View and manage Google Drive files and folders that you have opened or created with this app", false),
        DRIVE_APPDATA("https://www.googleapis.com/auth/drive.appdata", "View and manage its own configuration data in your Google Drive", false),
        DRIVE_ACTIVITY_READONLY("https://www.googleapis.com/auth/drive.activity.readonly", "View the activity record of files in your Google Drive", false),
        DRIVE_ACTIVITY("https://www.googleapis.com/auth/drive.activity", "View and add to the activity record of files in your Google Drive", false),
        DRIVE("https://www.googleapis.com/auth/drive", "See, edit, create, and delete all of your Google Drive files", false),
        ACTIVITY("https://www.googleapis.com/auth/activity", "View the activity history of your Google apps", false),
        DOUBLECLICKSEARCH("https://www.googleapis.com/auth/doubleclicksearch", "View and manage your advertising data in DoubleClick Search", false),
        DOUBLECLICKBIDMANAGER("https://www.googleapis.com/auth/doubleclickbidmanager", "View and manage your reports in DoubleClick Bid Manager", false),
        DOCUMENTS_READONLY("https://www.googleapis.com/auth/documents.readonly", "View your Google Docs documents", false),
        DOCUMENTS("https://www.googleapis.com/auth/documents", "View and manage your Google Docs documents", false),
        DISPLAY_VIDEO("https://www.googleapis.com/auth/display-video", "Create, see, edit, and permanently delete your Display & Video 360 entities and reports", false),
        DIRECTORY_READONLY("https://www.googleapis.com/auth/directory.readonly", "See and download your organization's GSuite directory", false),
        DIALOGFLOW("https://www.googleapis.com/auth/dialogflow", "View, manage and query your Dialogflow agents", false),
        DFATRAFFICKING("https://www.googleapis.com/auth/dfatrafficking", "View and manage your DoubleClick Campaign Manager's (DCM) display ad campaigns", false),
        DFAREPORTING("https://www.googleapis.com/auth/dfareporting", "View and manage DoubleClick for Advertisers reports", false),
        DEVSTORAGE_READ_WRITE("https://www.googleapis.com/auth/devstorage.read_write", "Manage your data in Google Cloud Storage", false),
        DEVSTORAGE_READ_ONLY("https://www.googleapis.com/auth/devstorage.read_only", "View your data in Google Cloud Storage", false),
        DEVSTORAGE_FULL_CONTROL("https://www.googleapis.com/auth/devstorage.full_control", "Manage your data and permissions in Google Cloud Storage", false),
        DDMCONVERSIONS("https://www.googleapis.com/auth/ddmconversions", "Manage DoubleClick Digital Marketing conversions", false),
        DATASTORE("https://www.googleapis.com/auth/datastore", "View and manage your Google Cloud Datastore data", false),
        CONTENT("https://www.googleapis.com/auth/content", "Manage your product listings and accounts for Google Shopping", false),
        CONTACTS_READONLY("https://www.googleapis.com/auth/contacts.readonly", "See and download your contacts", false),
        CONTACTS_OTHER_READONLY("https://www.googleapis.com/auth/contacts.other.readonly", "See and download contact info automatically saved in your \"Other contacts\"", false),
        CONTACTS("https://www.googleapis.com/auth/contacts", "See, edit, download, and permanently delete your contacts", false),
        CONTACTS_FEEDS("https://www.google.com/m8/feeds", "See, edit, download, and permanently delete your contacts", false),
        COMPUTE_READONLY("https://www.googleapis.com/auth/compute.readonly", "View your Google Compute Engine resources", false),
        COMPUTE("https://www.googleapis.com/auth/compute", "View and manage your Google Compute Engine resources", false),
        CLOUDRUNTIMECONFIG("https://www.googleapis.com/auth/cloudruntimeconfig", "Manage your Google Cloud Platform services' runtime configuration", false),
        CLOUDKMS("https://www.googleapis.com/auth/cloudkms", "View and manage your keys and secrets stored in Cloud Key Management Service", false),
        CLOUDIOT("https://www.googleapis.com/auth/cloudiot", "Register and manage devices in the Google Cloud IoT service", false),
        CLOUD_SEARCH_STATS_INDEXING("https://www.googleapis.com/auth/cloud_search.stats.indexing", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_SEARCH_STATS("https://www.googleapis.com/auth/cloud_search.stats", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_SEARCH_SETTINGS_QUERY("https://www.googleapis.com/auth/cloud_search.settings.query", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_SEARCH_SETTINGS_INDEXING("https://www.googleapis.com/auth/cloud_search.settings.indexing", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_SEARCH_SETTINGS("https://www.googleapis.com/auth/cloud_search.settings", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_SEARCH_QUERY("https://www.googleapis.com/auth/cloud_search.query", "Search your organization's data in the Cloud Search index", false),
        CLOUD_SEARCH_INDEXING("https://www.googleapis.com/auth/cloud_search.indexing", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_SEARCH_DEBUG("https://www.googleapis.com/auth/cloud_search.debug", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_SEARCH("https://www.googleapis.com/auth/cloud_search", "Index and serve your organization's data with Cloud Search", false),
        CLOUD_DEBUGGER("https://www.googleapis.com/auth/cloud_debugger", "Use Stackdriver Debugger", false),
        CLOUD_VISION("https://www.googleapis.com/auth/cloud-vision", "Apply machine learning models to understand and label images", false),
        CLOUD_TRANSLATION("https://www.googleapis.com/auth/cloud-translation", "Translate text from one language to another using Google Translate", false),
        CLOUD_PLATFORM_READ_ONLY("https://www.googleapis.com/auth/cloud-platform.read-only", "View your data across Google Cloud Platform services", false),
        CLOUD_PLATFORM("https://www.googleapis.com/auth/cloud-platform", "View and manage your data across Google Cloud Platform services", false),
        CLOUD_LANGUAGE("https://www.googleapis.com/auth/cloud-language", "Apply machine learning models to reveal the structure and meaning of text", false),
        CLOUD_IDENTITY_GROUPS_READONLY("https://www.googleapis.com/auth/cloud-identity.groups.readonly", "See any Cloud Identity Groups that you can access, including group members and their emails", false),
        CLOUD_IDENTITY_GROUPS("https://www.googleapis.com/auth/cloud-identity.groups", "See, change, create, and delete any of the Cloud Identity Groups that you can access, including the members of each group", false),
        CLOUD_BIGTABLE_ADMIN_TABLE("https://www.googleapis.com/auth/cloud-bigtable.admin.table", "Administer your Cloud Bigtable tables", false),
        CLOUD_BIGTABLE_ADMIN_CLUSTER("https://www.googleapis.com/auth/cloud-bigtable.admin.cluster", "Administer your Cloud Bigtable clusters", false),
        CLOUD_BIGTABLE_ADMIN("https://www.googleapis.com/auth/cloud-bigtable.admin", "Administer your Cloud Bigtable tables and clusters", false),
        CLASSROOM_TOPICS_READONLY("https://www.googleapis.com/auth/classroom.topics.readonly", "View topics in Google Classroom", false),
        CLASSROOM_TOPICS("https://www.googleapis.com/auth/classroom.topics", "See, create, and edit topics in Google Classroom", false),
        CLASSROOM_STUDENT_SUBMISSIONS_STUDENTS_READONLY("https://www.googleapis.com/auth/classroom.student-submissions.students.readonly", "View course work and grades for students in the Google Classroom classes you teach or administer", false),
        CLASSROOM_STUDENT_SUBMISSIONS_ME_READONLY("https://www.googleapis.com/auth/classroom.student-submissions.me.readonly", "View your course work and grades in Google Classroom", false),
        CLASSROOM_ROSTERS_READONLY("https://www.googleapis.com/auth/classroom.rosters.readonly", "View your Google Classroom class rosters", false),
        CLASSROOM_ROSTERS("https://www.googleapis.com/auth/classroom.rosters", "Manage your Google Classroom class rosters", false),
        CLASSROOM_PUSH_NOTIFICATIONS("https://www.googleapis.com/auth/classroom.push-notifications", "Receive notifications about your Google Classroom data", false),
        CLASSROOM_PROFILE_PHOTOS("https://www.googleapis.com/auth/classroom.profile.photos", "View the profile photos of people in your classes", false),
        CLASSROOM_PROFILE_EMAILS("https://www.googleapis.com/auth/classroom.profile.emails", "View the email addresses of people in your classes", false),
        CLASSROOM_GUARDIANLINKS_STUDENTS_READONLY("https://www.googleapis.com/auth/classroom.guardianlinks.students.readonly", "View guardians for students in your Google Classroom classes", false),
        CLASSROOM_GUARDIANLINKS_STUDENTS("https://www.googleapis.com/auth/classroom.guardianlinks.students", "View and manage guardians for students in your Google Classroom classes", false),
        CLASSROOM_GUARDIANLINKS_ME_READONLY("https://www.googleapis.com/auth/classroom.guardianlinks.me.readonly", "View your Google Classroom guardians", false),
        CLASSROOM_COURSEWORK_STUDENTS_READONLY("https://www.googleapis.com/auth/classroom.coursework.students.readonly", "View course work and grades for students in the Google Classroom classes you teach or administer", false),
        CLASSROOM_COURSEWORK_STUDENTS("https://www.googleapis.com/auth/classroom.coursework.students", "Manage course work and grades for students in the Google Classroom classes you teach and view the course work and grades for classes you administer", false),
        CLASSROOM_COURSEWORK_ME_READONLY("https://www.googleapis.com/auth/classroom.coursework.me.readonly", "View your course work and grades in Google Classroom", false),
        CLASSROOM_COURSEWORK_ME("https://www.googleapis.com/auth/classroom.coursework.me", "Manage your course work and view your grades in Google Classroom", false),
        CLASSROOM_COURSES_READONLY("https://www.googleapis.com/auth/classroom.courses.readonly", "View your Google Classroom classes", false),
        CLASSROOM_COURSES("https://www.googleapis.com/auth/classroom.courses", "Manage your Google Classroom classes", false),
        CLASSROOM_ANNOUNCEMENTS_READONLY("https://www.googleapis.com/auth/classroom.announcements.readonly", "View announcements in Google Classroom", false),
        CLASSROOM_ANNOUNCEMENTS("https://www.googleapis.com/auth/classroom.announcements", "View and manage announcements in Google Classroom", false),
        CALENDAR_SETTINGS_READONLY("https://www.googleapis.com/auth/calendar.settings.readonly", "View your Calendar settings", false),
        CALENDAR_READONLY("https://www.googleapis.com/auth/calendar.readonly", "View your calendars", false),
        CALENDAR_EVENTS_READONLY("https://www.googleapis.com/auth/calendar.events.readonly", "View events on all your calendars", false),
        CALENDAR_EVENTS("https://www.googleapis.com/auth/calendar.events", "View and edit events on all your calendars", false),
        CALENDAR("https://www.googleapis.com/auth/calendar", "See, edit, share, and permanently delete all the calendars you can access using Google Calendar", false),
        CALENDAR_FEEDS("https://www.google.com/calendar/feeds", "See, edit, share, and permanently delete all the calendars you can access using Google Calendar", false),
        BOOKS("https://www.googleapis.com/auth/books", "Manage your books", false),
        BLOGGER_READONLY("https://www.googleapis.com/auth/blogger.readonly", "View your Blogger account", false),
        BLOGGER("https://www.googleapis.com/auth/blogger", "Manage your Blogger account", false),
        BIGTABLE_ADMIN_TABLE("https://www.googleapis.com/auth/bigtable.admin.table", "Administer your Cloud Bigtable tables", false),
        BIGTABLE_ADMIN_INSTANCE("https://www.googleapis.com/auth/bigtable.admin.instance", "Administer your Cloud Bigtable clusters", false),
        BIGTABLE_ADMIN_CLUSTER("https://www.googleapis.com/auth/bigtable.admin.cluster", "Administer your Cloud Bigtable clusters", false),
        BIGTABLE_ADMIN("https://www.googleapis.com/auth/bigtable.admin", "Administer your Cloud Bigtable tables and clusters", false),
        BIGQUERY_READONLY("https://www.googleapis.com/auth/bigquery.readonly", "View your data in Google BigQuery", false),
        BIGQUERY_INSERTDATA("https://www.googleapis.com/auth/bigquery.insertdata", "Insert data into Google BigQuery", false),
        BIGQUERY("https://www.googleapis.com/auth/bigquery", "View and manage your data in Google BigQuery", false),
        APPS_ORDER_READONLY("https://www.googleapis.com/auth/apps.order.readonly", "Manage users on your domain", false),
        APPS_ORDER("https://www.googleapis.com/auth/apps.order", "Manage users on your domain", false),
        APPS_LICENSING("https://www.googleapis.com/auth/apps.licensing", "View and manage G Suite licenses for your domain", false),
        APPS_GROUPS_SETTINGS("https://www.googleapis.com/auth/apps.groups.settings", "View and manage the settings of a G Suite group", false),
        APPS_GROUPS_MIGRATION("https://www.googleapis.com/auth/apps.groups.migration", "Manage messages in groups on your domain", false),
        APPS_ALERTS("https://www.googleapis.com/auth/apps.alerts", "See and delete your domain's G Suite alerts, and send alert feedback", false),
        APPENGINE_ADMIN("https://www.googleapis.com/auth/appengine.admin", "View and manage your applications deployed on Google App Engine", false),
        ANDROIDPUBLISHER("https://www.googleapis.com/auth/androidpublisher", "View and manage your Google Play Developer account", false),
        ANDROIDMANAGEMENT("https://www.googleapis.com/auth/androidmanagement", "Manage Android devices and apps for your customers", false),
        ANDROIDENTERPRISE("https://www.googleapis.com/auth/androidenterprise", "Manage corporate Android devices", false),
        ANALYTICS_USER_DELETION("https://www.googleapis.com/auth/analytics.user.deletion", "Manage Google Analytics user deletion requests", false),
        ANALYTICS_READONLY("https://www.googleapis.com/auth/analytics.readonly", "View your Google Analytics data", false),
        ANALYTICS_PROVISION("https://www.googleapis.com/auth/analytics.provision", "Create a new Google Analytics account along with its default property and view", false),
        ANALYTICS_MANAGE_USERS_READONLY("https://www.googleapis.com/auth/analytics.manage.users.readonly", "View Google Analytics user permissions", false),
        ANALYTICS_MANAGE_USERS("https://www.googleapis.com/auth/analytics.manage.users", "Manage Google Analytics Account users by email address", false),
        ANALYTICS_EDIT("https://www.googleapis.com/auth/analytics.edit", "Edit Google Analytics management entities", false),
        ANALYTICS("https://www.googleapis.com/auth/analytics", "View and manage your Google Analytics data", false),
        ADSENSEHOST("https://www.googleapis.com/auth/adsensehost", "View and manage your AdSense host data and associated accounts", false),
        ADSENSE_READONLY("https://www.googleapis.com/auth/adsense.readonly", "View your AdSense data", false),
        ADSENSE("https://www.googleapis.com/auth/adsense", "View and manage your AdSense data", false),
        ADMIN_REPORTS_USAGE_READONLY("https://www.googleapis.com/auth/admin.reports.usage.readonly", "View usage reports for your G Suite domain", false),
        ADMIN_REPORTS_AUDIT_READONLY("https://www.googleapis.com/auth/admin.reports.audit.readonly", "View audit reports for your G Suite domain", false),
        ADMIN_DIRECTORY_USERSCHEMA_READONLY("https://www.googleapis.com/auth/admin.directory.userschema.readonly", "View user schemas on your domain", false),
        ADMIN_DIRECTORY_USERSCHEMA("https://www.googleapis.com/auth/admin.directory.userschema", "View and manage the provisioning of user schemas on your domain", false),
        ADMIN_DIRECTORY_USER_SECURITY("https://www.googleapis.com/auth/admin.directory.user.security", "Manage data access permissions for users on your domain", false),
        ADMIN_DIRECTORY_USER_READONLY("https://www.googleapis.com/auth/admin.directory.user.readonly", "View users on your domain", false),
        ADMIN_DIRECTORY_USER_ALIAS_READONLY("https://www.googleapis.com/auth/admin.directory.user.alias.readonly", "View user aliases on your domain", false),
        ADMIN_DIRECTORY_USER_ALIAS("https://www.googleapis.com/auth/admin.directory.user.alias", "View and manage user aliases on your domain", false),
        ADMIN_DIRECTORY_USER("https://www.googleapis.com/auth/admin.directory.user", "View and manage the provisioning of users on your domain", false),
        ADMIN_DIRECTORY_ROLEMANAGEMENT_READONLY("https://www.googleapis.com/auth/admin.directory.rolemanagement.readonly", "View delegated admin roles for your domain", false),
        ADMIN_DIRECTORY_ROLEMANAGEMENT("https://www.googleapis.com/auth/admin.directory.rolemanagement", "Manage delegated admin roles for your domain", false),
        ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY("https://www.googleapis.com/auth/admin.directory.resource.calendar.readonly", "View calendar resources on your domain", false),
        ADMIN_DIRECTORY_RESOURCE_CALENDAR("https://www.googleapis.com/auth/admin.directory.resource.calendar", "View and manage the provisioning of calendar resources on your domain", false),
        ADMIN_DIRECTORY_ORGUNIT_READONLY("https://www.googleapis.com/auth/admin.directory.orgunit.readonly", "View organization units on your domain", false),
        ADMIN_DIRECTORY_ORGUNIT("https://www.googleapis.com/auth/admin.directory.orgunit", "View and manage organization units on your domain", false),
        ADMIN_DIRECTORY_NOTIFICATIONS("https://www.googleapis.com/auth/admin.directory.notifications", "View and manage notifications received on your domain", false),
        ADMIN_DIRECTORY_GROUP_READONLY("https://www.googleapis.com/auth/admin.directory.group.readonly", "View groups on your domain", false),
        ADMIN_DIRECTORY_GROUP_MEMBER_READONLY("https://www.googleapis.com/auth/admin.directory.group.member.readonly", "View group subscriptions on your domain", false),
        ADMIN_DIRECTORY_GROUP_MEMBER("https://www.googleapis.com/auth/admin.directory.group.member", "View and manage group subscriptions on your domain", false),
        ADMIN_DIRECTORY_GROUP("https://www.googleapis.com/auth/admin.directory.group", "View and manage the provisioning of groups on your domain", false),
        ADMIN_DIRECTORY_DOMAIN_READONLY("https://www.googleapis.com/auth/admin.directory.domain.readonly", "View domains related to your customers", false),
        ADMIN_DIRECTORY_DOMAIN("https://www.googleapis.com/auth/admin.directory.domain", "View and manage the provisioning of domains for your customers", false),
        ADMIN_DIRECTORY_DEVICE_MOBILE_READONLY("https://www.googleapis.com/auth/admin.directory.device.mobile.readonly", "View your mobile devices' metadata", false),
        ADMIN_DIRECTORY_DEVICE_MOBILE_ACTION("https://www.googleapis.com/auth/admin.directory.device.mobile.action", "Manage your mobile devices by performing administrative tasks", false),
        ADMIN_DIRECTORY_DEVICE_MOBILE("https://www.googleapis.com/auth/admin.directory.device.mobile", "View and manage your mobile devices' metadata", false),
        ADMIN_DIRECTORY_DEVICE_CHROMEOS_READONLY("https://www.googleapis.com/auth/admin.directory.device.chromeos.readonly", "View your Chrome OS devices' metadata", false),
        ADMIN_DIRECTORY_DEVICE_CHROMEOS("https://www.googleapis.com/auth/admin.directory.device.chromeos", "View and manage your Chrome OS devices' metadata", false),
        ADMIN_DIRECTORY_CUSTOMER_READONLY("https://www.googleapis.com/auth/admin.directory.customer.readonly", "View customer related information", false),
        ADMIN_DIRECTORY_CUSTOMER("https://www.googleapis.com/auth/admin.directory.customer", "View and manage customer related information", false),
        ADMIN_DATATRANSFER_READONLY("https://www.googleapis.com/auth/admin.datatransfer.readonly", "View data transfers between users in your organization", false),
        ADMIN_DATATRANSFER("https://www.googleapis.com/auth/admin.datatransfer", "View and manage data transfers between users in your organization", false),
        ADEXCHANGE_BUYER("https://www.googleapis.com/auth/adexchange.buyer", "Manage your Ad Exchange buyer account configuration", false);

        private String scope;
        private String description;
        private boolean isDefault;

        public static List<String> getAdminDirectoryScopes() {
            return Arrays.stream(new Google[]{
                    ADMIN_DIRECTORY_USERSCHEMA_READONLY,
                    ADMIN_DIRECTORY_USERSCHEMA,
                    ADMIN_DIRECTORY_USER_SECURITY,
                    ADMIN_DIRECTORY_USER_READONLY,
                    ADMIN_DIRECTORY_USER_ALIAS_READONLY,
                    ADMIN_DIRECTORY_USER_ALIAS,
                    ADMIN_DIRECTORY_USER,
                    ADMIN_DIRECTORY_ROLEMANAGEMENT_READONLY,
                    ADMIN_DIRECTORY_ROLEMANAGEMENT,
                    ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY,
                    ADMIN_DIRECTORY_RESOURCE_CALENDAR,
                    ADMIN_DIRECTORY_ORGUNIT_READONLY,
                    ADMIN_DIRECTORY_ORGUNIT,
                    ADMIN_DIRECTORY_NOTIFICATIONS,
                    ADMIN_DIRECTORY_GROUP_READONLY,
                    ADMIN_DIRECTORY_GROUP_MEMBER_READONLY,
                    ADMIN_DIRECTORY_GROUP_MEMBER,
                    ADMIN_DIRECTORY_GROUP,
                    ADMIN_DIRECTORY_DOMAIN_READONLY,
                    ADMIN_DIRECTORY_DOMAIN,
                    ADMIN_DIRECTORY_DEVICE_MOBILE_READONLY,
                    ADMIN_DIRECTORY_DEVICE_MOBILE_ACTION,
                    ADMIN_DIRECTORY_DEVICE_MOBILE,
                    ADMIN_DIRECTORY_DEVICE_CHROMEOS_READONLY,
                    ADMIN_DIRECTORY_DEVICE_CHROMEOS,
                    ADMIN_DIRECTORY_CUSTOMER_READONLY,
                    ADMIN_DIRECTORY_CUSTOMER
            }).map(Google::getScope).collect(Collectors.toList());
        }

        /**
         * View And manage user's mail in Gmail.
         *
         * @return List
         */
        public static List<String> getGmailScopes() {
            return Arrays.stream(new Google[]{
                    GMAIL,
                    GMAIL_SETTINGS_SHARING,
                    GMAIL_SETTINGS_BASIC,
                    GMAIL_SEND,
                    GMAIL_READONLY,
                    GMAIL_MODIFY,
                    GMAIL_METADATA,
                    GMAIL_LABELS,
                    GMAIL_INSERT,
                    GMAIL_COMPOSE,
                    GMAIL_ADDONS_CURRENT_MESSAGE_READONLY,
                    GMAIL_ADDONS_CURRENT_MESSAGE_METADATA,
                    GMAIL_ADDONS_CURRENT_MESSAGE_ACTION,
                    GMAIL_ADDONS_CURRENT_ACTION_COMPOSE
            }).map(Google::getScope).collect(Collectors.toList());
        }


        /**
         * Used for OIDC authorization and certification
         *
         * @return List
         */
        public static List<String> getOidcScopes() {
            return Arrays.stream(new Google[]{
                    USER_OPENID,
                    USER_EMAIL,
                    USER_PROFILE
            }).map(Google::getScope).collect(Collectors.toList());
        }

        /**
         * View And manage user's detail and Google Contacts.
         *
         * @return List
         */
        public static List<String> getPeopleScopes() {
            return Arrays.stream(new Google[]{
                    CONTACTS_READONLY,
                    CONTACTS_OTHER_READONLY,
                    CONTACTS,
                    CONTACTS_FEEDS,
                    DIRECTORY_READONLY,
                    USER_PHONENUMBERS_READ,
                    USER_ORGANIZATION_READ,
                    USER_GENDER_READ,
                    USER_EMAILS_READ,
                    USER_BIRTHDAY_READ,
                    USER_ADDRESSES_READ,
                    USERINFO_PROFILE,
                    USERINFO_EMAIL
            }).map(Google::getScope).collect(Collectors.toList());
        }

        /**
         * View and manage user's photo library.
         *
         * @return List
         */
        public static List<String> getPhotosLibraryScopes() {
            return Arrays.stream(new Google[]{
                    PHOTOSLIBRARY_SHARING,
                    PHOTOSLIBRARY_READONLY_APPCREATEDDATA,
                    PHOTOSLIBRARY_READONLY,
                    PHOTOSLIBRARY_APPENDONLY,
                    PHOTOSLIBRARY
            }).map(Google::getScope).collect(Collectors.toList());
        }

        /**
         * View And manage user's videos, activity and playlists.
         *
         * @return List
         */
        public static List<String> getYouTubeScopes() {
            return Arrays.stream(new Google[]{
                    YT_ANALYTICS_READONLY,
                    YT_ANALYTICS_MONETARY_READONLY,
                    YOUTUBEPARTNER_CHANNEL_AUDIT,
                    YOUTUBEPARTNER,
                    YOUTUBE_UPLOAD,
                    YOUTUBE_READONLY,
                    YOUTUBE_FORCE_SSL,
                    YOUTUBE_CHANNEL_MEMBERSHIPS_CREATOR,
                    YOUTUBE
            }).map(Google::getScope).collect(Collectors.toList());
        }

        /**
         * View And manage user's Google Analytics.
         *
         * @return List
         */
        public static List<String> getGoogleAnalyticsScopes() {
            return Arrays.stream(new Google[]{
                    ANALYTICS_USER_DELETION,
                    ANALYTICS_READONLY,
                    ANALYTICS_PROVISION,
                    ANALYTICS_MANAGE_USERS_READONLY,
                    ANALYTICS_MANAGE_USERS,
                    ANALYTICS_EDIT,
                    ANALYTICS
            }).map(Google::getScope).collect(Collectors.toList());
        }

        /**
         * View And manage user's calendars in Google Calendar.
         *
         * @return List
         */
        public static List<String> getCalendarScopes() {
            return Arrays.stream(new Google[]{
                    CALENDAR_SETTINGS_READONLY,
                    CALENDAR_READONLY,
                    CALENDAR_EVENTS_READONLY,
                    CALENDAR_EVENTS,
                    CALENDAR,
                    CALENDAR_FEEDS
            }).map(Google::getScope).collect(Collectors.toList());
        }

        /**
         * List, download, create, move, edit, share and search all of user's documents and files in Google Drive.
         *
         * @return List
         */
        public static List<String> getDriveScopes() {
            return Arrays.stream(new Google[]{
                    DRIVE_SCRIPTS,
                    DRIVE_READONLY,
                    DRIVE_PHOTOS_READONLY,
                    DRIVE_METADATA_READONLY,
                    DRIVE_METADATA,
                    DRIVE_FILE,
                    DRIVE_APPDATA,
                    DRIVE_ACTIVITY_READONLY,
                    DRIVE_ACTIVITY,
                    DRIVE,
                    ACTIVITY
            }).map(Google::getScope).collect(Collectors.toList());
        }

    }

    /**
     * 华为 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Huawei implements Scope {
        BASE_PROFILE("https://www.huawei.com/auth/account/base.profile", "获取用户的基本信息", true),
        MOBILE_NUMBER("https://www.huawei.com/auth/account/mobile.number", "获取用户的手机号", false),
        ACCOUNTLIST("https://www.huawei.com/auth/account/accountlist", "获取用户的账单列表", false),

        /**
         * 以下两个 scope 不需要经过华为评估和验证
         */
        SCOPE_DRIVE_FILE("https://www.huawei.com/auth/drive.file", "只允许访问由应用程序创建或打开的文件", false),
        SCOPE_DRIVE_APPDATA("https://www.huawei.com/auth/drive.appdata", "只允许访问由应用程序创建或打开的文件", false),
        /**
         * 以下四个 scope 使用前需要向drivekit@huawei.com提交申请
         * <p>
         * 参考：https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides-V5/server-dev-0000001050039664-V5#ZH-CN_TOPIC_0000001050039664__section1618418855716
         */
        SCOPE_DRIVE("https://www.huawei.com/auth/drive", "只允许访问由应用程序创建或打开的文件", false),
        SCOPE_DRIVE_READONLY("https://www.huawei.com/auth/drive.readonly", "只允许访问由应用程序创建或打开的文件", false),
        SCOPE_DRIVE_METADATA("https://www.huawei.com/auth/drive.metadata", "只允许访问由应用程序创建或打开的文件", false),
        SCOPE_DRIVE_METADATA_READONLY("https://www.huawei.com/auth/drive.metadata.readonly", "只允许访问由应用程序创建或打开的文件", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 京东 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Jd implements Scope {

        SNSAPI_BASE("snsapi_base", "基础授权", true);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 酷家乐 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Kujiale implements Scope {

        GET_USER_INFO("get_user_info", "获取用户的基本信息", true),
        GET_DESIGN("get_design", "获取指定方案详情", false),
        GET_BUDGET_LIST("get_budget_list", "获取清单预算概览数据", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Line 平台 OAuth 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Line implements Scope {

        PROFILE("profile", "Get profile details", true),
        OPENID("openid", "Get id token", true),
        EMAIL("email", "Get email (separate authorization required)", false);

        private final String scope;
        private final String description;
        private final boolean isDefault;

    }

    /**
     * 领英 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Linkedin implements Scope {

        R_LITEPROFILE("r_liteprofile", "Use your name, headline, and photo", true),
        R_EMAILADDRESS("r_emailaddress", "Use the primary email address associated with your LinkedIn account", true),
        W_MEMBER_SOCIAL("w_member_social", "Post, comment and like posts on your behalf", true),
        R_MEMBER_SOCIAL("r_member_social", "Retrieve your posts, comments, likes, and other engagement data", false),
        R_AD_CAMPAIGNS("r_ad_campaigns", "View advertising campaigns you manage", false),
        R_ADS("r_ads", "Retrieve your advertising accounts", false),
        R_ADS_LEADGEN_AUTOMATION("r_ads_leadgen_automation", "Access your Lead Gen Forms and retrieve leads", false),
        R_ADS_REPORTING("r_ads_reporting", "Retrieve reporting for your advertising accounts", false),
        R_BASICPROFILE("r_basicprofile", "Use your basic profile including your name, photo, headline, and current positions", false),
        R_ORGANIZATION_SOCIAL("r_organization_social", "Retrieve your organizations' posts, including any comments, likes and other engagement data", false),
        RW_AD_CAMPAIGNS("rw_ad_campaigns", "Manage your advertising campaigns", false),
        RW_ADS("rw_ads", "Manage your advertising accounts", false),
        RW_COMPANY_ADMIN("rw_company_admin", "For V1 callsManage your organization's page and post updates", false),
        RW_DMP_SEGMENTS("rw_dmp_segments", "Create and manage your matched audiences", false),
        RW_ORGANIZATION_ADMIN("rw_organization_admin", "Manage your organizations' pages and retrieve reporting data", false),
        RW_ORGANIZATION("rw_organization", "For V2 callsManage your organization's page and post updates", false),
        W_ORGANIZATION_SOCIAL("w_organization_social", "Post, comment and like posts on your organization's behalf", false),
        W_SHARE("w_share", "Post updates to LinkedIn as you", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 微软 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Microsoft implements Scope {

        PROFILE("profile", "允许应用查看用户的基本个人资料（名称、图片、用户名称）", true),
        EMAIL("email", "允许应用读取用户的主电子邮件地址", true),
        OPENID("openid", "允许用户以其工作或学校帐户登录应用，并允许应用查看用户的基本个人资料信息", true),
        OFFLINE_ACCESS("offline_access", "允许应用读取和更新用户数据，即使用户当前没有在使用此应用，也不例外", true),

        USER_READ("User.Read", "登录并读取用户个人资料", false),
        USER_READWRITE("User.ReadWrite", "对用户个人资料的读写权限", false),
        USER_READBASIC_ALL("User.ReadBasic.All", "读取所有用户的基本个人资料", false),
        USER_READ_ALL("User.Read.All", "读取所有用户的完整个人资料", false),
        USER_READWRITE_ALL("User.ReadWrite.All", "读取和写入所有用户的完整个人资料", false),
        USER_INVITE_ALL("User.Invite.All", "将来宾用户邀请到组织", false),
        USER_EXPORT_ALL("User.Export.All", "导出用户数据", false),
        USER_MANAGEIDENTITIES_ALL("User.ManageIdentities.All", "管理所有用户标识", false),

        USERACTIVITY_READWRITE_CREATEDBYAPP("UserActivity.ReadWrite.CreatedByApp", "将应用活动读取和写入到用户的活动源", false),

        FILES_READ("Files.Read", "允许应用读取登录用户的文件", false),
        FILES_READ_ALL("Files.Read.All", "允许应用读取登录用户可以访问的所有文件", false),
        FILES_READWRITE("Files.ReadWrite", "允许应用读取、创建、更新和删除登录用户的文件", false),
        FILES_READWRITE_ALL("Files.ReadWrite.All", "允许应用读取、创建、更新和删除登录用户可以访问的所有文件", false),
        FILES_READWRITE_APPFOLDER("Files.ReadWrite.AppFolder", "允许应用读取、创建、更新和删除应用程序文件夹中的文件", false),
        FILES_READ_SELECTED("Files.Read.Selected", "允许应用读取用户选择的文件。在用户选择文件后，应用有几个小时的访问权限", false),
        FILES_READWRITE_SELECTED("Files.ReadWrite.Selected", "允许应用读取和写入用户选择的文件。在用户选择文件后，应用有几个小时的访问权限", false),

        ORGCONTACT_READ_ALL("OrgContact.Read.All", "允许应用代表已登录用户读取所有组织联系人。 这些联系人由组织管理，不同于用户的个人联系人", false),

        MAIL_READ("Mail.Read", "允许应用读取用户邮箱中的电子邮件", false),
        MAIL_READBASIC("Mail.ReadBasic", "允许应用读取已登录用户的邮箱，但不读取 body、bodyPreview、uniqueBody、attachments、extensions 和任何扩展属性。 不包含邮件搜索权限", false),
        MAIL_READWRITE("Mail.ReadWrite", "允许应用创建、读取、更新和删除用户邮箱中的电子邮件。不包括发送电子邮件的权限", false),
        MAIL_READ_SHARED("Mail.Read.Shared", "允许应用读取用户可以访问的邮件，包括用户个人邮件和共享邮件", false),
        MAIL_READWRITE_SHARED("Mail.ReadWrite.Shared", "允许应用创建、读取、更新和删除用户有权访问的邮件，包括用户个人邮件和共享邮件。不包括邮件发送权限", false),
        MAIL_SEND("Mail.Send", "允许应用以组织用户身份发送邮件", false),
        MAIL_SEND_SHARED("Mail.Send.Shared", "允许应用以登录用户身份发送邮件，包括代表他人发送邮件", false),
        MAILBOXSETTINGS_READ("MailboxSettings.Read", "允许应用读取用户的邮箱设置。不包括邮件发送权限", false),
        MAILBOXSETTINGS_READWRITE("MailboxSettings.ReadWrite", "允许应用创建、读取、更新和删除用户邮箱设置。 不包含直接发送邮件的权限，但允许应用创建能够转发或重定向邮件的规则", false),

        NOTES_READ("Notes.Read", "允许应用代表已登录用户读取 OneNote 笔记本和分区标题并创建新的页面、笔记本和分区", false),
        NOTES_CREATE("Notes.Create", "允许应用代创建用户 OneNote 笔记本", false),
        NOTES_READWRITE("Notes.ReadWrite", "允许应用代表已登录用户读取、共享和修改 OneNote 笔记本", false),
        NOTES_READ_ALL("Notes.Read.All", "允许应用读取登录用户在组织中有权访问的 OneNote 笔记本", false),
        NOTES_READWRITE_ALL("Notes.ReadWrite.All", "允许应用读取、共享和修改已登录用户在组织中有权访问的 OneNote 笔记本", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 小米 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Mi implements Scope {

        profile("user/profile", "获取用户的基本信息", true),
        OPENID("user/openIdV2", "获取用户的OpenID", true),
        PHONE_EMAIL("user/phoneAndEmail", "获取用户的手机号和邮箱", true);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Pinterest 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Pinterest implements Scope {

        READ_PUBLIC("read_public", "Use GET method on a user’s Pins, boards.", true),
        WRITE_PUBLIC("write_public", "Use PATCH, POST and DELETE methods on a user’s Pins and boards.", false),
        READ_RELATIONSHIPS("read_relationships", "Use GET method on a user’s follows and followers (on boards, users and interests).", false),
        WRITE_RELATIONSHIPS("write_relationships", "Use PATCH, POST and DELETE methods on a user’s follows and followers (on boards, users and interests).", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * QQ 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Qq implements Scope {

        GET_USER_INFO("get_user_info", "获取登录用户的昵称、头像、性别", true),
        /**
         * 以下 scope 需要申请：http://wiki.connect.qq.com/openapi%e6%9d%83%e9%99%90%e7%94%b3%e8%af%b7
         */
        GET_VIP_INFO("get_vip_info", "获取QQ会员的基本信息", false),
        GET_VIP_RICH_INFO("get_vip_rich_info", "获取QQ会员的高级信息", false),
        LIST_ALBUM("list_album", "获取用户QQ空间相册列表", false),
        UPLOAD_PIC("upload_pic", "上传一张照片到QQ空间相册", false),
        ADD_ALBUM("add_album", "在用户的空间相册里，创建一个新的个人相册", false),
        LIST_PHOTO("list_photo", "获取用户QQ空间相册中的照片列表", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 人人 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Renren implements Scope {

        READ_USER_BLOG("read_user_blog", "获取用户日志时需要用户授予的权限。", false),
        READ_USER_CHECKIN("read_user_checkin", "获取用户报到信息时需要用户授予的权限。", false),
        READ_USER_FEED("read_user_feed", "获取用户新鲜事时需要用户授予的权限。", false),
        READ_USER_GUESTBOOK("read_user_guestbook", "获取用户留言板时需要用户授予的权限。", false),
        READ_USER_INVITATION("read_user_invitation", "获取用户被邀请的状况时需要用户授予的权限。", false),
        READ_USER_LIKE_HISTORY("read_user_like_history", "获取用户喜欢的历史信息时需要用户授予的权限。", false),
        READ_USER_MESSAGE("read_user_message", "获取用户站内信时需要用户授予的权限。", false),
        READ_USER_NOTIFICATION("read_user_notification", "获取用户已收到的通知时需要用户授予的权限。", false),
        READ_USER_PHOTO("read_user_photo", "获取用户相册相关信息时需要用户授予的权限。", false),
        READ_USER_STATUS("read_user_status", "获取用户状态相关信息时需要用户授予的权限。", false),
        READ_USER_ALBUM("read_user_album", "获取用户相册相关信息时需要用户授予的权限。", false),
        READ_USER_COMMENT("read_user_comment", "获取用户评论相关信息时需要用户授予的权限。", false),
        READ_USER_SHARE("read_user_share", "获取用户分享相关信息时需要用户授予的权限。", false),
        READ_USER_REQUEST("read_user_request", "获取用户好友请求、圈人请求等信息时需要用户授予的权限。", false),
        PUBLISH_BLOG("publish_blog", "以用户身份发布日志时需要用户授予的权限。", false),
        PUBLISH_CHECKIN("publish_checkin", "以用户身份发布报到时需要用户授予的权限。", false),
        PUBLISH_FEED("publish_feed", "以用户身份发送新鲜事时需要用户授予的权限。", false),
        PUBLISH_SHARE("publish_share", "以用户身份发送分享时需要用户授予的权限。", false),
        WRITE_GUESTBOOK("write_guestbook", "以用户身份进行留言时需要用户授予的权限。", false),
        SEND_INVITATION("send_invitation", "以用户身份发送邀请时需要用户授予的权限。", false),
        SEND_REQUEST("send_request", "以用户身份发送好友申请、圈人请求等时需要用户授予的权限。", false),
        SEND_MESSAGE("send_message", "以用户身份发送站内信时需要用户授予的权限。", false),
        SEND_NOTIFICATION("send_notification", "以用户身份发送通知（user_to_user）时需要用户授予的权限。", false),
        PHOTO_UPLOAD("photo_upload", "以用户身份上传照片时需要用户授予的权限。", false),
        STATUS_UPDATE("status_update", "以用户身份发布状态时需要用户授予的权限。", false),
        CREATE_ALBUM("create_album", "以用户身份发布相册时需要用户授予的权限。", false),
        PUBLISH_COMMENT("publish_comment", "以用户身份发布评论时需要用户授予的权限。", false),
        OPERATE_LIKE("operate_like", "以用户身份执行喜欢操作时需要用户授予的权限。", false),
        ADMIN_PAGE("admin_page", "以用户的身份，管理其可以管理的公共主页的权限。", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Stackoverflow 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum StackOverflow implements Scope {

        read_inbox("read_inbox", "access a user's global inbox", true),
        NO_EXPIRY("no_expiry", "access_token's with this scope do not expire", false),
        WRITE_ACCESS("write_access", "perform write operations as a user", false),
        PRIVATE_INFO("private_info", "access full history of a user's private actions on the site", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * Slack 平台 OAuth 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Slack implements Scope {

        USERS_PROFILE_READ("users.profile:read", "View profile details about people in a workspace", true),
        USERS_READ("users:read", "View people in a workspace", true),
        USERS_READ_EMAIL("users:read.email", "View email addresses of people in a workspace", true),
        USERS_PROFILE_WRITE("users.profile:write", "Edit a user’s profile information and status", false),
        USERS_PROFILE_WRITE_USER("users.profile:write:user", "Change the user's profile fields", false),
        USERS_WRITE("users:write", "Set presence for your slack app", false),
        ADMIN("admin", "Administer a workspace", false),
        ADMIN_ANALYTICS_READ("admin.analytics:read", "Access analytics data about the organization", false),
        ADMIN_APPS_READ("admin.apps:read", "View apps and app requests in a workspace", false),
        ADMIN_APPS_WRITE("admin.apps:write", "Manage apps in a workspace", false),
        ADMIN_BARRIERS_READ("admin.barriers:read", "Read information barriers in the organization", false),
        ADMIN_BARRIERS_WRITE("admin.barriers:write", "Manage information barriers in the organization", false),
        ADMIN_CONVERSATIONS_READ("admin.conversations:read", "View the channel’s member list, topic, purpose and channel name", false),
        ADMIN_CONVERSATIONS_WRITE("admin.conversations:write", "Start a new conversation, modify a conversation and modify channel details", false),
        ADMIN_INVITES_READ("admin.invites:read", "Gain information about invite requests in a Grid organization.", false),
        ADMIN_INVITES_WRITE("admin.invites:write", "Approve or deny invite requests in a Grid organization.", false),
        ADMIN_TEAMS_READ("admin.teams:read", "Access information about a workspace", false),
        ADMIN_TEAMS_WRITE("admin.teams:write", "Make changes to a workspace", false),
        ADMIN_USERGROUPS_READ("admin.usergroups:read", "Access information about user groups", false),
        ADMIN_USERGROUPS_WRITE("admin.usergroups:write", "Make changes to your usergroups", false),
        ADMIN_USERS_READ("admin.users:read", "Access a workspace’s profile information", false),
        ADMIN_USERS_WRITE("admin.users:write", "Modify account information", false),
        APP_MENTIONS_READ("app_mentions:read", "View messages that directly mention @your_slack_app in conversations that the app is in", false),
        AUDITLOGS_READ("auditlogs:read", "View events from all workspaces, channels and users (Enterprise Grid only)", false),
        BOT("bot", "Add the ability for people to direct message or mention @your_slack_app", false),
        CALLS_READ("calls:read", "View information about ongoing and past calls", false),
        CALLS_WRITE("calls:write", "Start and manage calls in a workspace", false),
        CHANNELS_HISTORY("channels:history", "View messages and other content in public channels that your slack app has been added to", false),
        CHANNELS_JOIN("channels:join", "Join public channels in a workspace", false),
        CHANNELS_MANAGE("channels:manage", "Manage public channels that your slack app has been added to and create new ones", false),
        CHANNELS_READ("channels:read", "View basic information about public channels in a workspace", false),
        CHANNELS_WRITE("channels:write", "Manage a user’s public channels and create new ones on a user’s behalf", false),
        CHAT_WRITE("chat:write", "Post messages in approved channels & conversations", false),
        CHAT_WRITE_CUSTOMIZE("chat:write.customize", "Send messages as @your_slack_app with a customized username and avatar", false),
        CHAT_WRITE_PUBLIC("chat:write.public", "Send messages to channels @your_slack_app isn't a member of", false),
        CHAT_WRITE_BOT("chat:write:bot", "Send messages as your slack app", false),
        CHAT_WRITE_USER("chat:write:user", "Send messages on a user’s behalf", false),
        CLIENT("client", "Receive all events from a workspace in real time", false),
        COMMANDS("commands", "Add shortcuts and/or slash commands that people can use", false),
        CONVERSATIONS_HISTORY("conversations:history", "Deprecated: Retrieve conversation history for legacy workspace apps", false),
        CONVERSATIONS_READ("conversations:read", "Deprecated: Retrieve information on conversations for legacy workspace apps", false),
        CONVERSATIONS_WRITE("conversations:write", "Deprecated: Edit conversation attributes for legacy workspace apps", false),
        DND_READ("dnd:read", "View Do Not Disturb settings for people in a workspace", false),
        DND_WRITE("dnd:write", "Edit a user’s Do Not Disturb settings", false),
        DND_WRITE_USER("dnd:write:user", "Change the user's Do Not Disturb settings", false),
        EMOJI_READ("emoji:read", "View custom emoji in a workspace", false),
        FILES_READ("files:read", "View files shared in channels and conversations that your slack app has been added to", false),
        FILES_WRITE("files:write", "Upload, edit, and delete files as your slack app", false),
        FILES_WRITE_USER("files:write:user", "Upload, edit, and delete files as your slack app", false),
        GROUPS_HISTORY("groups:history", "View messages and other content in private channels that your slack app has been added to", false),
        GROUPS_READ("groups:read", "View basic information about private channels that your slack app has been added to", false),
        GROUPS_WRITE("groups:write", "Manage private channels that your slack app has been added to and create new ones", false),
        IDENTIFY("identify", "View information about a user’s identity", false),
        IDENTITY_AVATAR("identity.avatar", "View a user’s Slack avatar", false),
        IDENTITY_AVATAR_READ_USER("identity.avatar:read:user", "View the user's profile picture", false),
        IDENTITY_BASIC("identity.basic", "View information about a user’s identity", false),
        IDENTITY_EMAIL("identity.email", "View a user’s email address", false),
        IDENTITY_EMAIL_READ_USER("identity.email:read:user", "This scope is not yet described.", false),
        IDENTITY_TEAM("identity.team", "View a user’s Slack workspace name", false),
        IDENTITY_TEAM_READ_USER("identity.team:read:user", "View the workspace's name, domain, and icon", false),
        IDENTITY_READ_USER("identity:read:user", "This scope is not yet described.", false),
        IM_HISTORY("im:history", "View messages and other content in direct messages that your slack app has been added to", false),
        IM_READ("im:read", "View basic information about direct messages that your slack app has been added to", false),
        IM_WRITE("im:write", "Start direct messages with people", false),
        INCOMING_WEBHOOK("incoming-webhook", "Create one-way webhooks to post messages to a specific channel", false),
        LINKS_READ("links:read", "View  URLs in messages", false),
        LINKS_WRITE("links:write", "Show previews of  URLs in messages", false),
        MPIM_HISTORY("mpim:history", "View messages and other content in group direct messages that your slack app has been added to", false),
        MPIM_READ("mpim:read", "View basic information about group direct messages that your slack app has been added to", false),
        MPIM_WRITE("mpim:write", "Start group direct messages with people", false),
        NONE("none", "Execute methods without needing a scope", false),
        PINS_READ("pins:read", "View pinned content in channels and conversations that your slack app has been added to", false),
        PINS_WRITE("pins:write", "Add and remove pinned messages and files", false),
        POST("post", "Post messages to a workspace", false),
        REACTIONS_READ("reactions:read", "View emoji reactions and their associated content in channels and conversations that your slack app has been added to", false),
        REACTIONS_WRITE("reactions:write", "Add and edit emoji reactions", false),
        READ("read", "View all content in a workspace", false),
        REMINDERS_READ("reminders:read", "View reminders created by your slack app", false),
        REMINDERS_READ_USER("reminders:read:user", "Access reminders created by a user or for a user", false),
        REMINDERS_WRITE("reminders:write", "Add, remove, or mark reminders as complete", false),
        REMINDERS_WRITE_USER("reminders:write:user", "Add, remove, or complete reminders for the user", false),
        REMOTE_FILES_READ("remote_files:read", "View remote files added by the app in a workspace", false),
        REMOTE_FILES_SHARE("remote_files:share", "Share remote files on a user’s behalf", false),
        REMOTE_FILES_WRITE("remote_files:write", "Add, edit, and delete remote files on a user’s behalf", false),
        SEARCH_READ("search:read", "Search a workspace’s content", false),
        STARS_READ("stars:read", "View messages and files that your slack app has starred", false),
        STARS_WRITE("stars:write", "Add or remove stars", false),
        TEAM_READ("team:read", "View the name, email domain, and icon for workspaces your slack app is connected to", false),
        TOKENS_BASIC("tokens.basic", "Execute methods without needing a scope", false),
        USERGROUPS_READ("usergroups:read", "View user groups in a workspace", false),
        USERGROUPS_WRITE("usergroups:write", "Create and manage user groups", false),
        WORKFLOW_STEPS_EXECUTE("workflow.steps:execute", "Add steps that people can use in Workflow Builder", false);

        private final String scope;
        private final String description;
        private final boolean isDefault;

    }

    /**
     * 微信公众平台 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum WechatMp implements Scope {

        SNSAPI_USERINFO("snsapi_userinfo", "弹出授权页面，可通过openid拿到昵称、性别、所在地。并且， 即使在未关注的情况下，只要用户授权，也能获取其信息", true),
        SNSAPI_BASE("snsapi_base", "不弹出授权页面，直接跳转，只能获取用户openid", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 微博 授权范围
     */
    @Getter
    @AllArgsConstructor
    public enum Weibo implements Scope {

        ALL("all", "获取所有权限", true),
        EMAIL("email", "用户的联系邮箱，<a rel=\"nofollow\" href=\"http://open.weibo.com/wiki/2/account/profile/email\">接口文档</a>", false),
        DIRECT_MESSAGES_WRITE("direct_messages_write", "私信发送接口，<a rel=\"nofollow\" href=\"http://open.weibo.com/wiki/C/2/direct_messages/send\">接口文档</a>", false),
        DIRECT_MESSAGES_READ("direct_messages_read", "私信读取接口，<a rel=\"nofollow\" href=\"http://open.weibo.com/wiki/C/2/direct_messages\">接口文档</a>", false),
        INVITATION_WRITE("invitation_write", "邀请发送接口，<a rel=\"nofollow\" href=\"http://open.weibo.com/wiki/Messages#.E5.A5.BD.E5.8F.8B.E9.82.80.E8.AF.B7\">接口文档</a>", false),
        FRIENDSHIPS_GROUPS_READ("friendships_groups_read", "好友分组读取接口组，<a rel=\"nofollow\" href=\"http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.A5.BD.E5.8F.8B.E5.88.86.E7.BB.84\">接口文档</a>", false),
        FRIENDSHIPS_GROUPS_WRITE("friendships_groups_write", "好友分组写入接口组，<a rel=\"nofollow\" href=\"http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.A5.BD.E5.8F.8B.E5.88.86.E7.BB.84\">接口文档</a>", false),
        STATUSES_TO_ME_READ("statuses_to_me_read", "定向微博读取接口组，<a rel=\"nofollow\" href=\"http://open.weibo.com/wiki/API%E6%96%87%E6%A1%A3_V2#.E5.BE.AE.E5.8D.9A\">接口文档</a>", false),
        FOLLOW_APP_OFFICIAL_MICROBLOG("follow_app_official_microblog", "关注应用官方微博，该参数不对应具体接口，只需在应用控制台填写官方帐号即可。填写的路径：我的应用-选择自己的应用-应用信息-基本信息-官方运营账号（默认值是应用开发者帐号）", false);

        private String scope;
        private String description;
        private boolean isDefault;

    }

    /**
     * 各个平台 scope 类的统一接口
     */
    public interface Scope {
        /**
         * 获取字符串 {@code scope}，对应为各平台实际使用的 {@code scope}
         *
         * @return String
         */
        String getScope();

        /**
         * 判断当前 {@code scope} 是否为各平台默认启用的
         *
         * @return boolean
         */
        boolean isDefault();

    }

}

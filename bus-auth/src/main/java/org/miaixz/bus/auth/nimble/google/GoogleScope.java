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
package org.miaixz.bus.auth.nimble.google;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Google authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum GoogleScope implements AuthorizeScope {

    /**
     * Google authorization scope for {@code USER_OPENID}.
     */
    USER_OPENID("openid", "Associate you with your personal info on GoogleScope", true),
    /**
     * Google authorization scope for {@code USER_EMAIL}.
     */
    USER_EMAIL("email", "View your email address", true),
    /**
     * Google authorization scope for {@code USER_PROFILE}.
     */
    USER_PROFILE("profile", "View your basic profile info", true),
    /**
     * Google authorization scope for {@code USER_PHONENUMBERS_READ}.
     */
    USER_PHONENUMBERS_READ("https://www.googleapis.com/auth/user.phonenumbers.read", "View your phone numbers", false),
    /**
     * Google authorization scope for {@code USER_ORGANIZATION_READ}.
     */
    USER_ORGANIZATION_READ("https://www.googleapis.com/auth/user.organization.read",
            "See your education, work history and org info", false),
    /**
     * Google authorization scope for {@code USER_GENDER_READ}.
     */
    USER_GENDER_READ("https://www.googleapis.com/auth/user.gender.read", "See your gender", false),
    /**
     * Google authorization scope for {@code USER_EMAILS_READ}.
     */
    USER_EMAILS_READ("https://www.googleapis.com/auth/user.emails.read", "View your email addresses", false),
    /**
     * Google authorization scope for {@code USER_BIRTHDAY_READ}.
     */
    USER_BIRTHDAY_READ("https://www.googleapis.com/auth/user.birthday.read", "View your complete date of birth", false),
    /**
     * Google authorization scope for {@code USER_ADDRESSES_READ}.
     */
    USER_ADDRESSES_READ("https://www.googleapis.com/auth/user.addresses.read", "View your street addresses", false),
    /**
     * Google authorization scope for {@code USERINFO_PROFILE}.
     */
    USERINFO_PROFILE("https://www.googleapis.com/auth/userinfo.profile",
            "See your personal info, including any personal info you've made publicly available", false),
    /**
     * Google authorization scope for {@code USERINFO_EMAIL}.
     */
    USERINFO_EMAIL("https://www.googleapis.com/auth/userinfo.email", "View your email address", false),
    /**
     * Google authorization scope for {@code YT_ANALYTICS_READONLY}.
     */
    YT_ANALYTICS_READONLY("https://www.googleapis.com/auth/yt-analytics.readonly",
            "View YouTube Analytics reports for your YouTube content", false),
    /**
     * Google authorization scope for {@code YT_ANALYTICS_MONETARY_READONLY}.
     */
    YT_ANALYTICS_MONETARY_READONLY("https://www.googleapis.com/auth/yt-analytics-monetary.readonly",
            "View monetary and non-monetary YouTube Analytics reports for your YouTube content", false),
    /**
     * Google authorization scope for {@code YOUTUBEPARTNER_CHANNEL_AUDIT}.
     */
    YOUTUBEPARTNER_CHANNEL_AUDIT("https://www.googleapis.com/auth/youtubepartner-channel-audit",
            "View private information of your YouTube channel relevant during the audit process with a YouTube partner",
            false),
    /**
     * Google authorization scope for {@code YOUTUBEPARTNER}.
     */
    YOUTUBEPARTNER("https://www.googleapis.com/auth/youtubepartner",
            "View and manage your assets and associated content on YouTube", false),
    /**
     * Google authorization scope for {@code YOUTUBE_UPLOAD}.
     */
    YOUTUBE_UPLOAD("https://www.googleapis.com/auth/youtube.upload", "Manage your YouTube videos", false),
    /**
     * Google authorization scope for {@code YOUTUBE_READONLY}.
     */
    YOUTUBE_READONLY("https://www.googleapis.com/auth/youtube.readonly", "View your YouTube account", false),
    /**
     * Google authorization scope for {@code YOUTUBE_FORCE_SSL}.
     */
    YOUTUBE_FORCE_SSL("https://www.googleapis.com/auth/youtube.force-ssl",
            "See, edit, and permanently delete your YouTube videos, ratings, comments and captions", false),
    /**
     * Google authorization scope for {@code YOUTUBE_CHANNEL_MEMBERSHIPS_CREATOR}.
     */
    YOUTUBE_CHANNEL_MEMBERSHIPS_CREATOR("https://www.googleapis.com/auth/youtube.channel-memberships.creator",
            "See a list of your current active channel members, their current level, and when they became a member",
            false),
    /**
     * Google authorization scope for {@code YOUTUBE}.
     */
    YOUTUBE("https://www.googleapis.com/auth/youtube", "Manage your YouTube account", false),
    /**
     * Google authorization scope for {@code WEBMASTERS_READONLY}.
     */
    WEBMASTERS_READONLY("https://www.googleapis.com/auth/webmasters.readonly",
            "View Search Console data for your verified sites", false),
    /**
     * Google authorization scope for {@code WEBMASTERS}.
     */
    WEBMASTERS("https://www.googleapis.com/auth/webmasters",
            "View and manage Search Console data for your verified sites", false),
    /**
     * Google authorization scope for {@code VERIFIEDACCESS}.
     */
    VERIFIEDACCESS("https://www.googleapis.com/auth/verifiedaccess", "Verify your enterprise credentials", false),
    /**
     * Google authorization scope for {@code TRACE_APPEND}.
     */
    TRACE_APPEND("https://www.googleapis.com/auth/trace.append", "Write Trace data for a project or application",
            false),
    /**
     * Google authorization scope for {@code TASKS_READONLY}.
     */
    TASKS_READONLY("https://www.googleapis.com/auth/tasks.readonly", "View your tasks", false),
    /**
     * Google authorization scope for {@code TASKS}.
     */
    TASKS("https://www.googleapis.com/auth/tasks", "Create, edit, organize, and delete all your tasks", false),
    /**
     * Google authorization scope for {@code TAGMANAGER_READONLY}.
     */
    TAGMANAGER_READONLY("https://www.googleapis.com/auth/tagmanager.readonly",
            "View your GoogleScope Tag Manager container and its subcomponents", false),
    /**
     * Google authorization scope for {@code TAGMANAGER_PUBLISH}.
     */
    TAGMANAGER_PUBLISH("https://www.googleapis.com/auth/tagmanager.publish",
            "Publish your GoogleScope Tag Manager container versions", false),
    /**
     * Google authorization scope for {@code TAGMANAGER_MANAGE_USERS}.
     */
    TAGMANAGER_MANAGE_USERS("https://www.googleapis.com/auth/tagmanager.manage.users",
            "Manage user permissions of your GoogleScope Tag Manager account and container", false),
    /**
     * Google authorization scope for {@code TAGMANAGER_MANAGE_ACCOUNTS}.
     */
    TAGMANAGER_MANAGE_ACCOUNTS("https://www.googleapis.com/auth/tagmanager.manage.accounts",
            "View and manage your GoogleScope Tag Manager accounts", false),
    /**
     * Google authorization scope for {@code TAGMANAGER_EDIT_CONTAINERVERSIONS}.
     */
    TAGMANAGER_EDIT_CONTAINERVERSIONS("https://www.googleapis.com/auth/tagmanager.edit.containerversions",
            "Manage your GoogleScope Tag Manager container versions", false),
    /**
     * Google authorization scope for {@code TAGMANAGER_EDIT_CONTAINERS}.
     */
    TAGMANAGER_EDIT_CONTAINERS("https://www.googleapis.com/auth/tagmanager.edit.containers",
            "Manage your GoogleScope Tag Manager container and its subcomponents, excluding versioning and publishing",
            false),
    /**
     * Google authorization scope for {@code TAGMANAGER_DELETE_CONTAINERS}.
     */
    TAGMANAGER_DELETE_CONTAINERS("https://www.googleapis.com/auth/tagmanager.delete.containers",
            "Delete your GoogleScope Tag Manager containers", false),
    /**
     * Google authorization scope for {@code STREETVIEWPUBLISH}.
     */
    STREETVIEWPUBLISH("https://www.googleapis.com/auth/streetviewpublish",
            "Publish and manage your 360 photos on GoogleScope Street View", false),
    /**
     * Google authorization scope for {@code SQLSERVICE_ADMIN}.
     */
    SQLSERVICE_ADMIN("https://www.googleapis.com/auth/sqlservice.admin",
            "Manage your GoogleScope SQL Service instances", false),
    /**
     * Google authorization scope for {@code SPREADSHEETS_READONLY}.
     */
    SPREADSHEETS_READONLY("https://www.googleapis.com/auth/spreadsheets.readonly", "View your GoogleScope Spreadsheets",
            false),
    /**
     * Google authorization scope for {@code SPREADSHEETS}.
     */
    SPREADSHEETS("https://www.googleapis.com/auth/spreadsheets",
            "See, edit, create, and delete your spreadsheets in GoogleScope Drive", false),
    /**
     * Google authorization scope for {@code SPANNER_DATA}.
     */
    SPANNER_DATA("https://www.googleapis.com/auth/spanner.data",
            "View and manage the contents of your Spanner databases", false),
    /**
     * Google authorization scope for {@code SPANNER_ADMIN}.
     */
    SPANNER_ADMIN("https://www.googleapis.com/auth/spanner.admin", "Administer your Spanner databases", false),
    /**
     * Google authorization scope for {@code SOURCE_READ_WRITE}.
     */
    SOURCE_READ_WRITE("https://www.googleapis.com/auth/source.read_write",
            "Manage the contents of your complex code repositories", false),
    /**
     * Google authorization scope for {@code SOURCE_READ_ONLY}.
     */
    SOURCE_READ_ONLY("https://www.googleapis.com/auth/source.read_only",
            "View the contents of your complex code repositories", false),
    /**
     * Google authorization scope for {@code SOURCE_FULL_CONTROL}.
     */
    SOURCE_FULL_CONTROL("https://www.googleapis.com/auth/source.full_control", "Manage your complex code repositories",
            false),
    /**
     * Google authorization scope for {@code SITEVERIFICATION_VERIFY_ONLY}.
     */
    SITEVERIFICATION_VERIFY_ONLY("https://www.googleapis.com/auth/siteverification.verify_only",
            "Manage your new site verifications with GoogleScope", false),
    /**
     * Google authorization scope for {@code SITEVERIFICATION}.
     */
    SITEVERIFICATION("https://www.googleapis.com/auth/siteverification",
            "Manage the list of sites and domains you control", false),
    /**
     * Google authorization scope for {@code SERVICECONTROL}.
     */
    SERVICECONTROL("https://www.googleapis.com/auth/servicecontrol", "Manage your GoogleScope Service Control data",
            false),
    /**
     * Google authorization scope for {@code SERVICE_MANAGEMENT_READONLY}.
     */
    SERVICE_MANAGEMENT_READONLY("https://www.googleapis.com/auth/service.management.readonly",
            "View your GoogleScope API service configuration", false),
    /**
     * Google authorization scope for {@code SERVICE_MANAGEMENT}.
     */
    SERVICE_MANAGEMENT("https://www.googleapis.com/auth/service.management",
            "Manage your GoogleScope API service configuration", false),
    /**
     * Google authorization scope for {@code SCRIPT_PROJECTS_READONLY}.
     */
    SCRIPT_PROJECTS_READONLY("https://www.googleapis.com/auth/script.projects.readonly",
            "View GoogleScope Apps Script projects", false),
    /**
     * Google authorization scope for {@code SCRIPT_PROJECTS}.
     */
    SCRIPT_PROJECTS("https://www.googleapis.com/auth/script.projects",
            "Create and update GoogleScope Apps Script projects", false),
    /**
     * Google authorization scope for {@code SCRIPT_PROCESSES}.
     */
    SCRIPT_PROCESSES("https://www.googleapis.com/auth/script.processes", "View GoogleScope Apps Script processes",
            false),
    /**
     * Google authorization scope for {@code SCRIPT_METRICS}.
     */
    SCRIPT_METRICS("https://www.googleapis.com/auth/script.metrics", "View GoogleScope Apps Script project's metrics",
            false),
    /**
     * Google authorization scope for {@code SCRIPT_DEPLOYMENTS_READONLY}.
     */
    SCRIPT_DEPLOYMENTS_READONLY("https://www.googleapis.com/auth/script.deployments.readonly",
            "View GoogleScope Apps Script deployments", false),
    /**
     * Google authorization scope for {@code SCRIPT_DEPLOYMENTS}.
     */
    SCRIPT_DEPLOYMENTS("https://www.googleapis.com/auth/script.deployments",
            "Create and update GoogleScope Apps Script deployments", false),
    /**
     * Google authorization scope for {@code PUBSUB}.
     */
    PUBSUB("https://www.googleapis.com/auth/pubsub", "View and manage Pub/Sub topics and subscriptions", false),
    /**
     * Google authorization scope for {@code PRESENTATIONS_READONLY}.
     */
    PRESENTATIONS_READONLY("https://www.googleapis.com/auth/presentations.readonly",
            "View your GoogleScope Slides presentations", false),
    /**
     * Google authorization scope for {@code PRESENTATIONS}.
     */
    PRESENTATIONS("https://www.googleapis.com/auth/presentations",
            "View and manage your GoogleScope Slides presentations", false),
    /**
     * Google authorization scope for {@code PHOTOSLIBRARY_SHARING}.
     */
    PHOTOSLIBRARY_SHARING("https://www.googleapis.com/auth/photoslibrary.sharing",
            "Manage and add to shared albums on your behalf", false),
    /**
     * Google authorization scope for {@code PHOTOSLIBRARY_READONLY_APPCREATEDDATA}.
     */
    PHOTOSLIBRARY_READONLY_APPCREATEDDATA("https://www.googleapis.com/auth/photoslibrary.readonly.appcreateddata",
            "Manage photos added by this app", false),
    /**
     * Google authorization scope for {@code PHOTOSLIBRARY_READONLY}.
     */
    PHOTOSLIBRARY_READONLY("https://www.googleapis.com/auth/photoslibrary.readonly",
            "View your GoogleScope Photos library", false),
    /**
     * Google authorization scope for {@code PHOTOSLIBRARY_APPENDONLY}.
     */
    PHOTOSLIBRARY_APPENDONLY("https://www.googleapis.com/auth/photoslibrary.appendonly",
            "Add to your GoogleScope Photos library", false),
    /**
     * Google authorization scope for {@code PHOTOSLIBRARY}.
     */
    PHOTOSLIBRARY("https://www.googleapis.com/auth/photoslibrary", "View and manage your GoogleScope Photos library",
            false),
    /**
     * Google authorization scope for {@code NDEV_CLOUDMAN_READONLY}.
     */
    NDEV_CLOUDMAN_READONLY("https://www.googleapis.com/auth/ndev.cloudman.readonly",
            "View your GoogleScope Cloud Platform management resources and deployment status information", false),
    /**
     * Google authorization scope for {@code NDEV_CLOUDMAN}.
     */
    NDEV_CLOUDMAN("https://www.googleapis.com/auth/ndev.cloudman",
            "View and manage your GoogleScope Cloud Platform management resources and deployment status information",
            false),
    /**
     * Google authorization scope for {@code NDEV_CLOUDDNS_READWRITE}.
     */
    NDEV_CLOUDDNS_READWRITE("https://www.googleapis.com/auth/ndev.clouddns.readwrite",
            "View and manage your DNS records hosted by GoogleScope Cloud DNS", false),
    /**
     * Google authorization scope for {@code NDEV_CLOUDDNS_READONLY}.
     */
    NDEV_CLOUDDNS_READONLY("https://www.googleapis.com/auth/ndev.clouddns.readonly",
            "View your DNS records hosted by GoogleScope Cloud DNS", false),
    /**
     * Google authorization scope for {@code MONITORING_WRITE}.
     */
    MONITORING_WRITE("https://www.googleapis.com/auth/monitoring.write",
            "Publish nimble data to your GoogleScope Cloud projects", false),
    /**
     * Google authorization scope for {@code MONITORING_READ}.
     */
    MONITORING_READ("https://www.googleapis.com/auth/monitoring.read",
            "View monitoring data for all of your GoogleScope Cloud and third-party projects", false),
    /**
     * Google authorization scope for {@code MONITORING}.
     */
    MONITORING("https://www.googleapis.com/auth/monitoring",
            "View and write monitoring data for all of your GoogleScope and third-party Cloud and API projects", false),
    /**
     * Google authorization scope for {@code MANUFACTURERCENTER}.
     */
    MANUFACTURERCENTER("https://www.googleapis.com/auth/manufacturercenter",
            "Manage your product listings for GoogleScope Manufacturer Center", false),
    /**
     * Google authorization scope for {@code LOGGING_WRITE}.
     */
    LOGGING_WRITE("https://www.googleapis.com/auth/logging.write", "Submit log data for your projects", false),
    /**
     * Google authorization scope for {@code LOGGING_READ}.
     */
    LOGGING_READ("https://www.googleapis.com/auth/logging.read", "View log data for your projects", false),
    /**
     * Google authorization scope for {@code LOGGING_ADMIN}.
     */
    LOGGING_ADMIN("https://www.googleapis.com/auth/logging.admin", "Administrate log data for your projects", false),
    /**
     * Google authorization scope for {@code JOBS}.
     */
    JOBS("https://www.googleapis.com/auth/jobs", "Manage job postings", false),
    /**
     * Google authorization scope for {@code INDEXING}.
     */
    INDEXING("https://www.googleapis.com/auth/indexing", "Submit data to GoogleScope for indexing", false),
    /**
     * Google authorization scope for {@code GROUPS}.
     */
    GROUPS("https://www.googleapis.com/auth/groups", "View and manage your GoogleScope Groups", false),
    /**
     * Google authorization scope for {@code GMAIL}.
     */
    GMAIL("https://mail.google.com/", "Read, compose, send, and permanently delete all your email from Gmail", false),
    /**
     * Google authorization scope for {@code GMAIL_SETTINGS_SHARING}.
     */
    GMAIL_SETTINGS_SHARING("https://www.googleapis.com/auth/gmail.settings.sharing",
            "Manage your sensitive mail settings, including who can manage your mail", false),
    /**
     * Google authorization scope for {@code GMAIL_SETTINGS_BASIC}.
     */
    GMAIL_SETTINGS_BASIC("https://www.googleapis.com/auth/gmail.settings.basic", "Manage your basic mail settings",
            false),
    /**
     * Google authorization scope for {@code GMAIL_SEND}.
     */
    GMAIL_SEND("https://www.googleapis.com/auth/gmail.send", "Send email on your behalf", false),
    /**
     * Google authorization scope for {@code GMAIL_READONLY}.
     */
    GMAIL_READONLY("https://www.googleapis.com/auth/gmail.readonly", "View your email messages and settings", false),
    /**
     * Google authorization scope for {@code GMAIL_MODIFY}.
     */
    GMAIL_MODIFY("https://www.googleapis.com/auth/gmail.modify", "View and modify but not delete your email", false),
    /**
     * Google authorization scope for {@code GMAIL_METADATA}.
     */
    GMAIL_METADATA("https://www.googleapis.com/auth/gmail.metadata",
            "View your email message metadata such as labels and headers, but not the email body", false),
    /**
     * Google authorization scope for {@code GMAIL_LABELS}.
     */
    GMAIL_LABELS("https://www.googleapis.com/auth/gmail.labels", "Manage mailbox labels", false),
    /**
     * Google authorization scope for {@code GMAIL_INSERT}.
     */
    GMAIL_INSERT("https://www.googleapis.com/auth/gmail.insert", "Insert mail into your mailbox", false),
    /**
     * Google authorization scope for {@code GMAIL_COMPOSE}.
     */
    GMAIL_COMPOSE("https://www.googleapis.com/auth/gmail.compose", "Manage drafts and send emails", false),
    /**
     * Google authorization scope for {@code GMAIL_ADDONS_CURRENT_MESSAGE_READONLY}.
     */
    GMAIL_ADDONS_CURRENT_MESSAGE_READONLY("https://www.googleapis.com/auth/gmail.addons.current.message.readonly",
            "View your email messages when the add-on is running", false),
    /**
     * Google authorization scope for {@code GMAIL_ADDONS_CURRENT_MESSAGE_METADATA}.
     */
    GMAIL_ADDONS_CURRENT_MESSAGE_METADATA("https://www.googleapis.com/auth/gmail.addons.current.message.metadata",
            "View your email message metadata when the add-on is running", false),
    /**
     * Google authorization scope for {@code GMAIL_ADDONS_CURRENT_MESSAGE_ACTION}.
     */
    GMAIL_ADDONS_CURRENT_MESSAGE_ACTION("https://www.googleapis.com/auth/gmail.addons.current.message.action",
            "View your email messages when you interact with the add-on", false),
    /**
     * Google authorization scope for {@code GMAIL_ADDONS_CURRENT_ACTION_COMPOSE}.
     */
    GMAIL_ADDONS_CURRENT_ACTION_COMPOSE("https://www.googleapis.com/auth/gmail.addons.current.action.compose",
            "Manage drafts and send emails when you interact with the add-on", false),
    /**
     * Google authorization scope for {@code GENOMICS}.
     */
    GENOMICS("https://www.googleapis.com/auth/genomics", "View and manage Genomics data", false),
    /**
     * Google authorization scope for {@code GAMES}.
     */
    GAMES("https://www.googleapis.com/auth/games", "Create, edit, and delete your GoogleScope Play Games activity",
            false),
    /**
     * Google authorization scope for {@code FORMS_CURRENTONLY}.
     */
    FORMS_CURRENTONLY("https://www.googleapis.com/auth/forms.currentonly",
            "View and manage forms that this application has been installed in", false),
    /**
     * Google authorization scope for {@code FORMS}.
     */
    FORMS("https://www.googleapis.com/auth/forms", "View and manage your forms in GoogleScope Drive", false),
    /**
     * Google authorization scope for {@code FITNESS_REPRODUCTIVE_HEALTH_WRITE}.
     */
    FITNESS_REPRODUCTIVE_HEALTH_WRITE("https://www.googleapis.com/auth/fitness.reproductive_health.write",
            "See and add info about your reproductive health in GoogleScope Fit. I consent to GoogleScope sharing my reporductive health information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_REPRODUCTIVE_HEALTH_READ}.
     */
    FITNESS_REPRODUCTIVE_HEALTH_READ("https://www.googleapis.com/auth/fitness.reproductive_health.read",
            "See info about your reproductive health in GoogleScope Fit. I consent to GoogleScope sharing my reporductive health information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_OXYGEN_SATURATION_WRITE}.
     */
    FITNESS_OXYGEN_SATURATION_WRITE("https://www.googleapis.com/auth/fitness.oxygen_saturation.write",
            "See and add info about your oxygen saturation in GoogleScope Fit. I consent to GoogleScope sharing my oxygen saturation information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_OXYGEN_SATURATION_READ}.
     */
    FITNESS_OXYGEN_SATURATION_READ("https://www.googleapis.com/auth/fitness.oxygen_saturation.read",
            "See info about your oxygen saturation in GoogleScope Fit. I consent to GoogleScope sharing my oxygen saturation information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_NUTRITION_WRITE}.
     */
    FITNESS_NUTRITION_WRITE("https://www.googleapis.com/auth/fitness.nutrition.write",
            "See and add to info about your nutrition in GoogleScope Fit", false),
    /**
     * Google authorization scope for {@code FITNESS_NUTRITION_READ}.
     */
    FITNESS_NUTRITION_READ("https://www.googleapis.com/auth/fitness.nutrition.read",
            "See info about your nutrition in GoogleScope Fit", false),
    /**
     * Google authorization scope for {@code FITNESS_LOCATION_WRITE}.
     */
    FITNESS_LOCATION_WRITE("https://www.googleapis.com/auth/fitness.location.write",
            "See and add to your GoogleScope Fit location data", false),
    /**
     * Google authorization scope for {@code FITNESS_LOCATION_READ}.
     */
    FITNESS_LOCATION_READ("https://www.googleapis.com/auth/fitness.location.read",
            "See your GoogleScope Fit speed and distance data", false),
    /**
     * Google authorization scope for {@code FITNESS_BODY_TEMPERATURE_WRITE}.
     */
    FITNESS_BODY_TEMPERATURE_WRITE("https://www.googleapis.com/auth/fitness.body_temperature.write",
            "See and add to info about your body temperature in GoogleScope Fit. I consent to GoogleScope sharing my body temperature information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_BODY_TEMPERATURE_READ}.
     */
    FITNESS_BODY_TEMPERATURE_READ("https://www.googleapis.com/auth/fitness.body_temperature.read",
            "See info about your body temperature in GoogleScope Fit. I consent to GoogleScope sharing my body temperature information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_BODY_WRITE}.
     */
    FITNESS_BODY_WRITE("https://www.googleapis.com/auth/fitness.body.write",
            "See and add info about your body measurements and heart rate to GoogleScope Fit", false),
    /**
     * Google authorization scope for {@code FITNESS_BODY_READ}.
     */
    FITNESS_BODY_READ("https://www.googleapis.com/auth/fitness.body.read",
            "See info about your body measurements and heart rate in GoogleScope Fit", false),
    /**
     * Google authorization scope for {@code FITNESS_BLOOD_PRESSURE_WRITE}.
     */
    FITNESS_BLOOD_PRESSURE_WRITE("https://www.googleapis.com/auth/fitness.blood_pressure.write",
            "See and add info about your blood pressure in GoogleScope Fit. I consent to GoogleScope sharing my blood pressure information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_BLOOD_PRESSURE_READ}.
     */
    FITNESS_BLOOD_PRESSURE_READ("https://www.googleapis.com/auth/fitness.blood_pressure.read",
            "See info about your blood pressure in GoogleScope Fit. I consent to GoogleScope sharing my blood pressure information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_BLOOD_GLUCOSE_WRITE}.
     */
    FITNESS_BLOOD_GLUCOSE_WRITE("https://www.googleapis.com/auth/fitness.blood_glucose.write",
            "See and add info about your blood glucose to GoogleScope Fit. I consent to GoogleScope sharing my blood glucose information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_BLOOD_GLUCOSE_READ}.
     */
    FITNESS_BLOOD_GLUCOSE_READ("https://www.googleapis.com/auth/fitness.blood_glucose.read",
            "See info about your blood glucose in GoogleScope Fit. I consent to GoogleScope sharing my blood glucose information with this app.",
            false),
    /**
     * Google authorization scope for {@code FITNESS_ACTIVITY_WRITE}.
     */
    FITNESS_ACTIVITY_WRITE("https://www.googleapis.com/auth/fitness.activity.write",
            "See and add to your GoogleScope Fit physical activity data", false),
    /**
     * Google authorization scope for {@code FITNESS_ACTIVITY_READ}.
     */
    FITNESS_ACTIVITY_READ("https://www.googleapis.com/auth/fitness.activity.read",
            "Use GoogleScope Fit to see and store your physical activity data", false),
    /**
     * Google authorization scope for {@code FIREBASE_READONLY}.
     */
    FIREBASE_READONLY("https://www.googleapis.com/auth/firebase.readonly", "View all your Firebase data and settings",
            false),
    /**
     * Google authorization scope for {@code FIREBASE}.
     */
    FIREBASE("https://www.googleapis.com/auth/firebase", "View and administer all your Firebase data and settings",
            false),
    /**
     * Google authorization scope for {@code EDISCOVERY_READONLY}.
     */
    EDISCOVERY_READONLY("https://www.googleapis.com/auth/ediscovery.readonly", "View your eDiscovery data", false),
    /**
     * Google authorization scope for {@code EDISCOVERY}.
     */
    EDISCOVERY("https://www.googleapis.com/auth/ediscovery", "Manage your eDiscovery data", false),
    /**
     * Google authorization scope for {@code DRIVE_SCRIPTS}.
     */
    DRIVE_SCRIPTS("https://www.googleapis.com/auth/drive.scripts",
            "Modify your GoogleScope Apps Script scripts' behavior", false),
    /**
     * Google authorization scope for {@code DRIVE_READONLY}.
     */
    DRIVE_READONLY("https://www.googleapis.com/auth/drive.readonly",
            "See and download all your GoogleScope Drive files", false),
    /**
     * Google authorization scope for {@code DRIVE_PHOTOS_READONLY}.
     */
    DRIVE_PHOTOS_READONLY("https://www.googleapis.com/auth/drive.photos.readonly",
            "View the photos, videos and albums in your GoogleScope Photos", false),
    /**
     * Google authorization scope for {@code DRIVE_METADATA_READONLY}.
     */
    DRIVE_METADATA_READONLY("https://www.googleapis.com/auth/drive.metadata.readonly",
            "View metadata for files in your GoogleScope Drive", false),
    /**
     * Google authorization scope for {@code DRIVE_METADATA}.
     */
    DRIVE_METADATA("https://www.googleapis.com/auth/drive.metadata",
            "View and manage metadata of files in your GoogleScope Drive", false),
    /**
     * Google authorization scope for {@code DRIVE_FILE}.
     */
    DRIVE_FILE("https://www.googleapis.com/auth/drive.file",
            "View and manage GoogleScope Drive files and folders that you have opened or created with this app", false),
    /**
     * Google authorization scope for {@code DRIVE_APPDATA}.
     */
    DRIVE_APPDATA("https://www.googleapis.com/auth/drive.appdata",
            "View and manage its own configuration data in your GoogleScope Drive", false),
    /**
     * Google authorization scope for {@code DRIVE_ACTIVITY_READONLY}.
     */
    DRIVE_ACTIVITY_READONLY("https://www.googleapis.com/auth/drive.activity.readonly",
            "View the activity record of files in your GoogleScope Drive", false),
    /**
     * Google authorization scope for {@code DRIVE_ACTIVITY}.
     */
    DRIVE_ACTIVITY("https://www.googleapis.com/auth/drive.activity",
            "View and add to the activity record of files in your GoogleScope Drive", false),
    /**
     * Google authorization scope for {@code DRIVE}.
     */
    DRIVE("https://www.googleapis.com/auth/drive", "See, edit, create, and delete all of your GoogleScope Drive files",
            false),
    /**
     * Google authorization scope for {@code ACTIVITY}.
     */
    ACTIVITY("https://www.googleapis.com/auth/activity", "View the activity history of your GoogleScope apps", false),
    /**
     * Google authorization scope for {@code DOUBLECLICKSEARCH}.
     */
    DOUBLECLICKSEARCH("https://www.googleapis.com/auth/doubleclicksearch",
            "View and manage your advertising data in DoubleClick Search", false),
    /**
     * Google authorization scope for {@code DOUBLECLICKBIDMANAGER}.
     */
    DOUBLECLICKBIDMANAGER("https://www.googleapis.com/auth/doubleclickbidmanager",
            "View and manage your reports in DoubleClick Bid Manager", false),
    /**
     * Google authorization scope for {@code DOCUMENTS_READONLY}.
     */
    DOCUMENTS_READONLY("https://www.googleapis.com/auth/documents.readonly", "View your GoogleScope Docs documents",
            false),
    /**
     * Google authorization scope for {@code DOCUMENTS}.
     */
    DOCUMENTS("https://www.googleapis.com/auth/documents", "View and manage your GoogleScope Docs documents", false),
    /**
     * Google authorization scope for {@code DISPLAY_VIDEO}.
     */
    DISPLAY_VIDEO("https://www.googleapis.com/auth/display-video",
            "Create, see, edit, and permanently delete your Display & Video 360 entities and reports", false),
    /**
     * Google authorization scope for {@code DIRECTORY_READONLY}.
     */
    DIRECTORY_READONLY("https://www.googleapis.com/auth/directory.readonly",
            "See and download your organization's GSuite directory", false),
    /**
     * Google authorization scope for {@code DIALOGFLOW}.
     */
    DIALOGFLOW("https://www.googleapis.com/auth/dialogflow", "View, manage and query your Dialogflow agents", false),
    /**
     * Google authorization scope for {@code DFATRAFFICKING}.
     */
    DFATRAFFICKING("https://www.googleapis.com/auth/dfatrafficking",
            "View and manage your DoubleClick Campaign Manager's (DCM) display ad campaigns", false),
    /**
     * Google authorization scope for {@code DFAREPORTING}.
     */
    DFAREPORTING("https://www.googleapis.com/auth/dfareporting", "View and manage DoubleClick for Advertisers reports",
            false),
    /**
     * Google authorization scope for {@code DEVSTORAGE_READ_WRITE}.
     */
    DEVSTORAGE_READ_WRITE("https://www.googleapis.com/auth/devstorage.read_write",
            "Manage your data in GoogleScope Cloud Storage", false),
    /**
     * Google authorization scope for {@code DEVSTORAGE_READ_ONLY}.
     */
    DEVSTORAGE_READ_ONLY("https://www.googleapis.com/auth/devstorage.read_only",
            "View your data in GoogleScope Cloud Storage", false),
    /**
     * Google authorization scope for {@code DEVSTORAGE_FULL_CONTROL}.
     */
    DEVSTORAGE_FULL_CONTROL("https://www.googleapis.com/auth/devstorage.full_control",
            "Manage your data and permissions in GoogleScope Cloud Storage", false),
    /**
     * Google authorization scope for {@code DDMCONVERSIONS}.
     */
    DDMCONVERSIONS("https://www.googleapis.com/auth/ddmconversions", "Manage DoubleClick Digital Marketing conversions",
            false),
    /**
     * Google authorization scope for {@code DATASTORE}.
     */
    DATASTORE("https://www.googleapis.com/auth/datastore", "View and manage your GoogleScope Cloud Datastore data",
            false),
    /**
     * Google authorization scope for {@code CONTENT}.
     */
    CONTENT("https://www.googleapis.com/auth/content",
            "Manage your product listings and accounts for GoogleScope Shopping", false),
    /**
     * Google authorization scope for {@code CONTACTS_READONLY}.
     */
    CONTACTS_READONLY("https://www.googleapis.com/auth/contacts.readonly", "See and download your contacts", false),
    /**
     * Google authorization scope for {@code CONTACTS_OTHER_READONLY}.
     */
    CONTACTS_OTHER_READONLY("https://www.googleapis.com/auth/contacts.other.readonly",
            "See and download contact info automatically saved in your \"Other contacts\"", false),
    /**
     * Google authorization scope for {@code CONTACTS}.
     */
    CONTACTS("https://www.googleapis.com/auth/contacts", "See, edit, download, and permanently delete your contacts",
            false),
    /**
     * Google authorization scope for {@code CONTACTS_FEEDS}.
     */
    CONTACTS_FEEDS("https://www.google.com/m8/feeds", "See, edit, download, and permanently delete your contacts",
            false),
    /**
     * Google authorization scope for {@code COMPUTE_READONLY}.
     */
    COMPUTE_READONLY("https://www.googleapis.com/auth/compute.readonly",
            "View your GoogleScope Compute Engine resources", false),
    /**
     * Google authorization scope for {@code COMPUTE}.
     */
    COMPUTE("https://www.googleapis.com/auth/compute", "View and manage your GoogleScope Compute Engine resources",
            false),
    /**
     * Google authorization scope for {@code CLOUDRUNTIMECONFIG}.
     */
    CLOUDRUNTIMECONFIG("https://www.googleapis.com/auth/cloudruntimeconfig",
            "Manage your GoogleScope Cloud Platform services' runtime configuration", false),
    /**
     * Google authorization scope for {@code CLOUDKMS}.
     */
    CLOUDKMS("https://www.googleapis.com/auth/cloudkms",
            "View and manage your keys and secrets stored in Cloud Key Management Service", false),
    /**
     * Google authorization scope for {@code CLOUDIOT}.
     */
    CLOUDIOT("https://www.googleapis.com/auth/cloudiot",
            "Register and manage devices in the GoogleScope Cloud IoT service", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_STATS_INDEXING}.
     */
    CLOUD_SEARCH_STATS_INDEXING("https://www.googleapis.com/auth/cloud_search.stats.indexing",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_STATS}.
     */
    CLOUD_SEARCH_STATS("https://www.googleapis.com/auth/cloud_search.stats",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_SETTINGS_QUERY}.
     */
    CLOUD_SEARCH_SETTINGS_QUERY("https://www.googleapis.com/auth/cloud_search.settings.query",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_SETTINGS_INDEXING}.
     */
    CLOUD_SEARCH_SETTINGS_INDEXING("https://www.googleapis.com/auth/cloud_search.settings.indexing",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_SETTINGS}.
     */
    CLOUD_SEARCH_SETTINGS("https://www.googleapis.com/auth/cloud_search.settings",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_QUERY}.
     */
    CLOUD_SEARCH_QUERY("https://www.googleapis.com/auth/cloud_search.query",
            "Search your organization's data in the Cloud Search index", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_INDEXING}.
     */
    CLOUD_SEARCH_INDEXING("https://www.googleapis.com/auth/cloud_search.indexing",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH_DEBUG}.
     */
    CLOUD_SEARCH_DEBUG("https://www.googleapis.com/auth/cloud_search.debug",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_SEARCH}.
     */
    CLOUD_SEARCH("https://www.googleapis.com/auth/cloud_search",
            "Index and serve your organization's data with Cloud Search", false),
    /**
     * Google authorization scope for {@code CLOUD_DEBUGGER}.
     */
    CLOUD_DEBUGGER("https://www.googleapis.com/auth/cloud_debugger", "Use Stackdriver Debugger", false),
    /**
     * Google authorization scope for {@code CLOUD_VISION}.
     */
    CLOUD_VISION("https://www.googleapis.com/auth/cloud-vision",
            "Apply machine learning models to understand and label images", false),
    /**
     * Google authorization scope for {@code CLOUD_TRANSLATION}.
     */
    CLOUD_TRANSLATION("https://www.googleapis.com/auth/cloud-translation",
            "Translate text from one language to another using GoogleScope Translate", false),
    /**
     * Google authorization scope for {@code CLOUD_PLATFORM_READ_ONLY}.
     */
    CLOUD_PLATFORM_READ_ONLY("https://www.googleapis.com/auth/cloud-platform.read-only",
            "View your data across GoogleScope Cloud Platform services", false),
    /**
     * Google authorization scope for {@code CLOUD_PLATFORM}.
     */
    CLOUD_PLATFORM("https://www.googleapis.com/auth/cloud-platform",
            "View and manage your data across GoogleScope Cloud Platform services", false),
    /**
     * Google authorization scope for {@code CLOUD_LANGUAGE}.
     */
    CLOUD_LANGUAGE("https://www.googleapis.com/auth/cloud-language",
            "Apply machine learning models to reveal the structure and meaning of text", false),
    /**
     * Google authorization scope for {@code CLOUD_IDENTITY_GROUPS_READONLY}.
     */
    CLOUD_IDENTITY_GROUPS_READONLY("https://www.googleapis.com/auth/cloud-identity.groups.readonly",
            "See any Cloud Identity Groups that you can access, including group members and their emails", false),
    /**
     * Google authorization scope for {@code CLOUD_IDENTITY_GROUPS}.
     */
    CLOUD_IDENTITY_GROUPS("https://www.googleapis.com/auth/cloud-identity.groups",
            "See, change, create, and delete any of the Cloud Identity Groups that you can access, including the members of each group",
            false),
    /**
     * Google authorization scope for {@code CLOUD_BIGTABLE_ADMIN_TABLE}.
     */
    CLOUD_BIGTABLE_ADMIN_TABLE("https://www.googleapis.com/auth/cloud-bigtable.admin.table",
            "Administer your Cloud Bigtable tables", false),
    /**
     * Google authorization scope for {@code CLOUD_BIGTABLE_ADMIN_CLUSTER}.
     */
    CLOUD_BIGTABLE_ADMIN_CLUSTER("https://www.googleapis.com/auth/cloud-bigtable.admin.cluster",
            "Administer your Cloud Bigtable clusters", false),
    /**
     * Google authorization scope for {@code CLOUD_BIGTABLE_ADMIN}.
     */
    CLOUD_BIGTABLE_ADMIN("https://www.googleapis.com/auth/cloud-bigtable.admin",
            "Administer your Cloud Bigtable tables and clusters", false),
    /**
     * Google authorization scope for {@code CLASSROOM_TOPICS_READONLY}.
     */
    CLASSROOM_TOPICS_READONLY("https://www.googleapis.com/auth/classroom.topics.readonly",
            "View topics in GoogleScope Classroom", false),
    /**
     * Google authorization scope for {@code CLASSROOM_TOPICS}.
     */
    CLASSROOM_TOPICS("https://www.googleapis.com/auth/classroom.topics",
            "See, create, and edit topics in GoogleScope Classroom", false),
    /**
     * Google authorization scope for {@code CLASSROOM_STUDENT_SUBMISSIONS_STUDENTS_READONLY}.
     */
    CLASSROOM_STUDENT_SUBMISSIONS_STUDENTS_READONLY(
            "https://www.googleapis.com/auth/classroom.student-submissions.students.readonly",
            "View course work and grades for students in the GoogleScope Classroom classes you teach or administer",
            false),
    /**
     * Google authorization scope for {@code CLASSROOM_STUDENT_SUBMISSIONS_ME_READONLY}.
     */
    CLASSROOM_STUDENT_SUBMISSIONS_ME_READONLY(
            "https://www.googleapis.com/auth/classroom.student-submissions.me.readonly",
            "View your course work and grades in GoogleScope Classroom", false),
    /**
     * Google authorization scope for {@code CLASSROOM_ROSTERS_READONLY}.
     */
    CLASSROOM_ROSTERS_READONLY("https://www.googleapis.com/auth/classroom.rosters.readonly",
            "View your GoogleScope Classroom class rosters", false),
    /**
     * Google authorization scope for {@code CLASSROOM_ROSTERS}.
     */
    CLASSROOM_ROSTERS("https://www.googleapis.com/auth/classroom.rosters",
            "Manage your GoogleScope Classroom class rosters", false),
    /**
     * Google authorization scope for {@code CLASSROOM_PUSH_NOTIFICATIONS}.
     */
    CLASSROOM_PUSH_NOTIFICATIONS("https://www.googleapis.com/auth/classroom.push-notifications",
            "Receive notifications about your GoogleScope Classroom data", false),
    /**
     * Google authorization scope for {@code CLASSROOM_PROFILE_PHOTOS}.
     */
    CLASSROOM_PROFILE_PHOTOS("https://www.googleapis.com/auth/classroom.profile.photos",
            "View the profile photos of people in your classes", false),
    /**
     * Google authorization scope for {@code CLASSROOM_PROFILE_EMAILS}.
     */
    CLASSROOM_PROFILE_EMAILS("https://www.googleapis.com/auth/classroom.profile.emails",
            "View the email addresses of people in your classes", false),
    /**
     * Google authorization scope for {@code CLASSROOM_GUARDIANLINKS_STUDENTS_READONLY}.
     */
    CLASSROOM_GUARDIANLINKS_STUDENTS_READONLY(
            "https://www.googleapis.com/auth/classroom.guardianlinks.students.readonly",
            "View guardians for students in your GoogleScope Classroom classes", false),
    /**
     * Google authorization scope for {@code CLASSROOM_GUARDIANLINKS_STUDENTS}.
     */
    CLASSROOM_GUARDIANLINKS_STUDENTS("https://www.googleapis.com/auth/classroom.guardianlinks.students",
            "View and manage guardians for students in your GoogleScope Classroom classes", false),
    /**
     * Google authorization scope for {@code CLASSROOM_GUARDIANLINKS_ME_READONLY}.
     */
    CLASSROOM_GUARDIANLINKS_ME_READONLY("https://www.googleapis.com/auth/classroom.guardianlinks.me.readonly",
            "View your GoogleScope Classroom guardians", false),
    /**
     * Google authorization scope for {@code CLASSROOM_COURSEWORK_STUDENTS_READONLY}.
     */
    CLASSROOM_COURSEWORK_STUDENTS_READONLY("https://www.googleapis.com/auth/classroom.coursework.students.readonly",
            "View course work and grades for students in the GoogleScope Classroom classes you teach or administer",
            false),
    /**
     * Google authorization scope for {@code CLASSROOM_COURSEWORK_STUDENTS}.
     */
    CLASSROOM_COURSEWORK_STUDENTS("https://www.googleapis.com/auth/classroom.coursework.students",
            "Manage course work and grades for students in the GoogleScope Classroom classes you teach and view the course work and grades for classes you administer",
            false),
    /**
     * Google authorization scope for {@code CLASSROOM_COURSEWORK_ME_READONLY}.
     */
    CLASSROOM_COURSEWORK_ME_READONLY("https://www.googleapis.com/auth/classroom.coursework.me.readonly",
            "View your course work and grades in GoogleScope Classroom", false),
    /**
     * Google authorization scope for {@code CLASSROOM_COURSEWORK_ME}.
     */
    CLASSROOM_COURSEWORK_ME("https://www.googleapis.com/auth/classroom.coursework.me",
            "Manage your course work and view your grades in GoogleScope Classroom", false),
    /**
     * Google authorization scope for {@code CLASSROOM_COURSES_READONLY}.
     */
    CLASSROOM_COURSES_READONLY("https://www.googleapis.com/auth/classroom.courses.readonly",
            "View your GoogleScope Classroom classes", false),
    /**
     * Google authorization scope for {@code CLASSROOM_COURSES}.
     */
    CLASSROOM_COURSES("https://www.googleapis.com/auth/classroom.courses", "Manage your GoogleScope Classroom classes",
            false),
    /**
     * Google authorization scope for {@code CLASSROOM_ANNOUNCEMENTS_READONLY}.
     */
    CLASSROOM_ANNOUNCEMENTS_READONLY("https://www.googleapis.com/auth/classroom.announcements.readonly",
            "View announcements in GoogleScope Classroom", false),
    /**
     * Google authorization scope for {@code CLASSROOM_ANNOUNCEMENTS}.
     */
    CLASSROOM_ANNOUNCEMENTS("https://www.googleapis.com/auth/classroom.announcements",
            "View and manage announcements in GoogleScope Classroom", false),
    /**
     * Google authorization scope for {@code CALENDAR_SETTINGS_READONLY}.
     */
    CALENDAR_SETTINGS_READONLY("https://www.googleapis.com/auth/calendar.settings.readonly",
            "View your Calendar settings", false),
    /**
     * Google authorization scope for {@code CALENDAR_READONLY}.
     */
    CALENDAR_READONLY("https://www.googleapis.com/auth/calendar.readonly", "View your calendars", false),
    /**
     * Google authorization scope for {@code CALENDAR_EVENTS_READONLY}.
     */
    CALENDAR_EVENTS_READONLY("https://www.googleapis.com/auth/calendar.events.readonly",
            "View events on all your calendars", false),
    /**
     * Google authorization scope for {@code CALENDAR_EVENTS}.
     */
    CALENDAR_EVENTS("https://www.googleapis.com/auth/calendar.events", "View and edit events on all your calendars",
            false),
    /**
     * Google authorization scope for {@code CALENDAR}.
     */
    CALENDAR("https://www.googleapis.com/auth/calendar",
            "See, edit, share, and permanently delete all the calendars you can access using GoogleScope Calendar",
            false),
    /**
     * Google authorization scope for {@code CALENDAR_FEEDS}.
     */
    CALENDAR_FEEDS("https://www.google.com/calendar/feeds",
            "See, edit, share, and permanently delete all the calendars you can access using GoogleScope Calendar",
            false),
    /**
     * Google authorization scope for {@code BOOKS}.
     */
    BOOKS("https://www.googleapis.com/auth/books", "Manage your books", false),
    /**
     * Google authorization scope for {@code BLOGGER_READONLY}.
     */
    BLOGGER_READONLY("https://www.googleapis.com/auth/blogger.readonly", "View your Blogger account", false),
    /**
     * Google authorization scope for {@code BLOGGER}.
     */
    BLOGGER("https://www.googleapis.com/auth/blogger", "Manage your Blogger account", false),
    /**
     * Google authorization scope for {@code BIGTABLE_ADMIN_TABLE}.
     */
    BIGTABLE_ADMIN_TABLE("https://www.googleapis.com/auth/bigtable.admin.table",
            "Administer your Cloud Bigtable tables", false),
    /**
     * Google authorization scope for {@code BIGTABLE_ADMIN_INSTANCE}.
     */
    BIGTABLE_ADMIN_INSTANCE("https://www.googleapis.com/auth/bigtable.admin.instance",
            "Administer your Cloud Bigtable clusters", false),
    /**
     * Google authorization scope for {@code BIGTABLE_ADMIN_CLUSTER}.
     */
    BIGTABLE_ADMIN_CLUSTER("https://www.googleapis.com/auth/bigtable.admin.cluster",
            "Administer your Cloud Bigtable clusters", false),
    /**
     * Google authorization scope for {@code BIGTABLE_ADMIN}.
     */
    BIGTABLE_ADMIN("https://www.googleapis.com/auth/bigtable.admin",
            "Administer your Cloud Bigtable tables and clusters", false),
    /**
     * Google authorization scope for {@code BIGQUERY_READONLY}.
     */
    BIGQUERY_READONLY("https://www.googleapis.com/auth/bigquery.readonly", "View your data in GoogleScope BigQuery",
            false),
    /**
     * Google authorization scope for {@code BIGQUERY_INSERTDATA}.
     */
    BIGQUERY_INSERTDATA("https://www.googleapis.com/auth/bigquery.insertdata", "Insert data into GoogleScope BigQuery",
            false),
    /**
     * Google authorization scope for {@code BIGQUERY}.
     */
    BIGQUERY("https://www.googleapis.com/auth/bigquery", "View and manage your data in GoogleScope BigQuery", false),
    /**
     * Google authorization scope for {@code APPS_ORDER_READONLY}.
     */
    APPS_ORDER_READONLY("https://www.googleapis.com/auth/apps.order.readonly", "Manage users on your domain", false),
    /**
     * Google authorization scope for {@code APPS_ORDER}.
     */
    APPS_ORDER("https://www.googleapis.com/auth/apps.order", "Manage users on your domain", false),
    /**
     * Google authorization scope for {@code APPS_LICENSING}.
     */
    APPS_LICENSING("https://www.googleapis.com/auth/apps.licensing", "View and manage G Suite licenses for your domain",
            false),
    /**
     * Google authorization scope for {@code APPS_GROUPS_SETTINGS}.
     */
    APPS_GROUPS_SETTINGS("https://www.googleapis.com/auth/apps.groups.settings",
            "View and manage the settings of a G Suite group", false),
    /**
     * Google authorization scope for {@code APPS_GROUPS_MIGRATION}.
     */
    APPS_GROUPS_MIGRATION("https://www.googleapis.com/auth/apps.groups.migration",
            "Manage messages in groups on your domain", false),
    /**
     * Google authorization scope for {@code APPS_ALERTS}.
     */
    APPS_ALERTS("https://www.googleapis.com/auth/apps.alerts",
            "See and delete your domain's G Suite alerts, and send alert feedback", false),
    /**
     * Google authorization scope for {@code APPENGINE_ADMIN}.
     */
    APPENGINE_ADMIN("https://www.googleapis.com/auth/appengine.admin",
            "View and manage your applications deployed on GoogleScope App Engine", false),
    /**
     * Google authorization scope for {@code ANDROIDPUBLISHER}.
     */
    ANDROIDPUBLISHER("https://www.googleapis.com/auth/androidpublisher",
            "View and manage your GoogleScope Play Developer account", false),
    /**
     * Google authorization scope for {@code ANDROIDMANAGEMENT}.
     */
    ANDROIDMANAGEMENT("https://www.googleapis.com/auth/androidmanagement",
            "Manage Android devices and apps for your customers", false),
    /**
     * Google authorization scope for {@code ANDROIDENTERPRISE}.
     */
    ANDROIDENTERPRISE("https://www.googleapis.com/auth/androidenterprise", "Manage corporate Android devices", false),
    /**
     * Google authorization scope for {@code ANALYTICS_USER_DELETION}.
     */
    ANALYTICS_USER_DELETION("https://www.googleapis.com/auth/analytics.user.deletion",
            "Manage GoogleScope Analytics user deletion requests", false),
    /**
     * Google authorization scope for {@code ANALYTICS_READONLY}.
     */
    ANALYTICS_READONLY("https://www.googleapis.com/auth/analytics.readonly", "View your GoogleScope Analytics data",
            false),
    /**
     * Google authorization scope for {@code ANALYTICS_PROVISION}.
     */
    ANALYTICS_PROVISION("https://www.googleapis.com/auth/analytics.provision",
            "Create a new GoogleScope Analytics account along with its default property and view", false),
    /**
     * Google authorization scope for {@code ANALYTICS_MANAGE_USERS_READONLY}.
     */
    ANALYTICS_MANAGE_USERS_READONLY("https://www.googleapis.com/auth/analytics.manage.users.readonly",
            "View GoogleScope Analytics user permissions", false),
    /**
     * Google authorization scope for {@code ANALYTICS_MANAGE_USERS}.
     */
    ANALYTICS_MANAGE_USERS("https://www.googleapis.com/auth/analytics.manage.users",
            "Manage GoogleScope Analytics Account users by email address", false),
    /**
     * Google authorization scope for {@code ANALYTICS_EDIT}.
     */
    ANALYTICS_EDIT("https://www.googleapis.com/auth/analytics.edit", "Edit GoogleScope Analytics management entities",
            false),
    /**
     * Google authorization scope for {@code ANALYTICS}.
     */
    ANALYTICS("https://www.googleapis.com/auth/analytics", "View and manage your GoogleScope Analytics data", false),
    /**
     * Google authorization scope for {@code ADSENSEHOST}.
     */
    ADSENSEHOST("https://www.googleapis.com/auth/adsensehost",
            "View and manage your AdSense host data and associated accounts", false),
    /**
     * Google authorization scope for {@code ADSENSE_READONLY}.
     */
    ADSENSE_READONLY("https://www.googleapis.com/auth/adsense.readonly", "View your AdSense data", false),
    /**
     * Google authorization scope for {@code ADSENSE}.
     */
    ADSENSE("https://www.googleapis.com/auth/adsense", "View and manage your AdSense data", false),
    /**
     * Google authorization scope for {@code ADMIN_REPORTS_USAGE_READONLY}.
     */
    ADMIN_REPORTS_USAGE_READONLY("https://www.googleapis.com/auth/admin.reports.usage.readonly",
            "View usage reports for your G Suite domain", false),
    /**
     * Google authorization scope for {@code ADMIN_REPORTS_AUDIT_READONLY}.
     */
    ADMIN_REPORTS_AUDIT_READONLY("https://www.googleapis.com/auth/admin.reports.audit.readonly",
            "View audit reports for your G Suite domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_USERSCHEMA_READONLY}.
     */
    ADMIN_DIRECTORY_USERSCHEMA_READONLY("https://www.googleapis.com/auth/admin.directory.userschema.readonly",
            "View user schemas on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_USERSCHEMA}.
     */
    ADMIN_DIRECTORY_USERSCHEMA("https://www.googleapis.com/auth/admin.directory.userschema",
            "View and manage the provisioning of user schemas on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_USER_SECURITY}.
     */
    ADMIN_DIRECTORY_USER_SECURITY("https://www.googleapis.com/auth/admin.directory.user.security",
            "Manage data access permissions for users on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_USER_READONLY}.
     */
    ADMIN_DIRECTORY_USER_READONLY("https://www.googleapis.com/auth/admin.directory.user.readonly",
            "View users on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_USER_ALIAS_READONLY}.
     */
    ADMIN_DIRECTORY_USER_ALIAS_READONLY("https://www.googleapis.com/auth/admin.directory.user.alias.readonly",
            "View user aliases on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_USER_ALIAS}.
     */
    ADMIN_DIRECTORY_USER_ALIAS("https://www.googleapis.com/auth/admin.directory.user.alias",
            "View and manage user aliases on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_USER}.
     */
    ADMIN_DIRECTORY_USER("https://www.googleapis.com/auth/admin.directory.user",
            "View and manage the provisioning of users on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_ROLEMANAGEMENT_READONLY}.
     */
    ADMIN_DIRECTORY_ROLEMANAGEMENT_READONLY("https://www.googleapis.com/auth/admin.directory.rolemanagement.readonly",
            "View delegated admin roles for your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_ROLEMANAGEMENT}.
     */
    ADMIN_DIRECTORY_ROLEMANAGEMENT("https://www.googleapis.com/auth/admin.directory.rolemanagement",
            "Manage delegated admin roles for your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY}.
     */
    ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY(
            "https://www.googleapis.com/auth/admin.directory.resource.calendar.readonly",
            "View calendar resources on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_RESOURCE_CALENDAR}.
     */
    ADMIN_DIRECTORY_RESOURCE_CALENDAR("https://www.googleapis.com/auth/admin.directory.resource.calendar",
            "View and manage the provisioning of calendar resources on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_ORGUNIT_READONLY}.
     */
    ADMIN_DIRECTORY_ORGUNIT_READONLY("https://www.googleapis.com/auth/admin.directory.orgunit.readonly",
            "View organization units on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_ORGUNIT}.
     */
    ADMIN_DIRECTORY_ORGUNIT("https://www.googleapis.com/auth/admin.directory.orgunit",
            "View and manage organization units on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_NOTIFICATIONS}.
     */
    ADMIN_DIRECTORY_NOTIFICATIONS("https://www.googleapis.com/auth/admin.directory.notifications",
            "View and manage notifications received on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_GROUP_READONLY}.
     */
    ADMIN_DIRECTORY_GROUP_READONLY("https://www.googleapis.com/auth/admin.directory.group.readonly",
            "View groups on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_GROUP_MEMBER_READONLY}.
     */
    ADMIN_DIRECTORY_GROUP_MEMBER_READONLY("https://www.googleapis.com/auth/admin.directory.group.member.readonly",
            "View group subscriptions on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_GROUP_MEMBER}.
     */
    ADMIN_DIRECTORY_GROUP_MEMBER("https://www.googleapis.com/auth/admin.directory.group.member",
            "View and manage group subscriptions on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_GROUP}.
     */
    ADMIN_DIRECTORY_GROUP("https://www.googleapis.com/auth/admin.directory.group",
            "View and manage the provisioning of groups on your domain", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_DOMAIN_READONLY}.
     */
    ADMIN_DIRECTORY_DOMAIN_READONLY("https://www.googleapis.com/auth/admin.directory.domain.readonly",
            "View domains related to your customers", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_DOMAIN}.
     */
    ADMIN_DIRECTORY_DOMAIN("https://www.googleapis.com/auth/admin.directory.domain",
            "View and manage the provisioning of domains for your customers", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_DEVICE_MOBILE_READONLY}.
     */
    ADMIN_DIRECTORY_DEVICE_MOBILE_READONLY("https://www.googleapis.com/auth/admin.directory.device.mobile.readonly",
            "View your mobile devices' metadata", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_DEVICE_MOBILE_ACTION}.
     */
    ADMIN_DIRECTORY_DEVICE_MOBILE_ACTION("https://www.googleapis.com/auth/admin.directory.device.mobile.action",
            "Manage your mobile devices by performing administrative tasks", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_DEVICE_MOBILE}.
     */
    ADMIN_DIRECTORY_DEVICE_MOBILE("https://www.googleapis.com/auth/admin.directory.device.mobile",
            "View and manage your mobile devices' metadata", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_DEVICE_CHROMEOS_READONLY}.
     */
    ADMIN_DIRECTORY_DEVICE_CHROMEOS_READONLY("https://www.googleapis.com/auth/admin.directory.device.chromeos.readonly",
            "View your Chrome OS devices' metadata", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_DEVICE_CHROMEOS}.
     */
    ADMIN_DIRECTORY_DEVICE_CHROMEOS("https://www.googleapis.com/auth/admin.directory.device.chromeos",
            "View and manage your Chrome OS devices' metadata", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_CUSTOMER_READONLY}.
     */
    ADMIN_DIRECTORY_CUSTOMER_READONLY("https://www.googleapis.com/auth/admin.directory.customer.readonly",
            "View customer related information", false),
    /**
     * Google authorization scope for {@code ADMIN_DIRECTORY_CUSTOMER}.
     */
    ADMIN_DIRECTORY_CUSTOMER("https://www.googleapis.com/auth/admin.directory.customer",
            "View and manage customer related information", false),
    /**
     * Google authorization scope for {@code ADMIN_DATATRANSFER_READONLY}.
     */
    ADMIN_DATATRANSFER_READONLY("https://www.googleapis.com/auth/admin.datatransfer.readonly",
            "View data transfers between users in your organization", false),
    /**
     * Google authorization scope for {@code ADMIN_DATATRANSFER}.
     */
    ADMIN_DATATRANSFER("https://www.googleapis.com/auth/admin.datatransfer",
            "View and manage data transfers between users in your organization", false),
    /**
     * Google authorization scope for {@code ADEXCHANGE_BUYER}.
     */
    ADEXCHANGE_BUYER("https://www.googleapis.com/auth/adexchange.buyer",
            "Manage your Ad Exchange buyer account configuration", false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

    /**
     * Returns Google Admin Directory authorization scopes.
     *
     * @return the Google Admin Directory authorization scopes
     */
    public static List<String> getAdminDirectoryScopes() {
        return Arrays.stream(
                new GoogleScope[] { ADMIN_DIRECTORY_USERSCHEMA_READONLY, ADMIN_DIRECTORY_USERSCHEMA,
                        ADMIN_DIRECTORY_USER_SECURITY, ADMIN_DIRECTORY_USER_READONLY,
                        ADMIN_DIRECTORY_USER_ALIAS_READONLY, ADMIN_DIRECTORY_USER_ALIAS, ADMIN_DIRECTORY_USER,
                        ADMIN_DIRECTORY_ROLEMANAGEMENT_READONLY, ADMIN_DIRECTORY_ROLEMANAGEMENT,
                        ADMIN_DIRECTORY_RESOURCE_CALENDAR_READONLY, ADMIN_DIRECTORY_RESOURCE_CALENDAR,
                        ADMIN_DIRECTORY_ORGUNIT_READONLY, ADMIN_DIRECTORY_ORGUNIT, ADMIN_DIRECTORY_NOTIFICATIONS,
                        ADMIN_DIRECTORY_GROUP_READONLY, ADMIN_DIRECTORY_GROUP_MEMBER_READONLY,
                        ADMIN_DIRECTORY_GROUP_MEMBER, ADMIN_DIRECTORY_GROUP, ADMIN_DIRECTORY_DOMAIN_READONLY,
                        ADMIN_DIRECTORY_DOMAIN, ADMIN_DIRECTORY_DEVICE_MOBILE_READONLY,
                        ADMIN_DIRECTORY_DEVICE_MOBILE_ACTION, ADMIN_DIRECTORY_DEVICE_MOBILE,
                        ADMIN_DIRECTORY_DEVICE_CHROMEOS_READONLY, ADMIN_DIRECTORY_DEVICE_CHROMEOS,
                        ADMIN_DIRECTORY_CUSTOMER_READONLY, ADMIN_DIRECTORY_CUSTOMER })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

    /**
     * View And manage user's mail in Gmail.
     *
     * @return the Gmail authorization scopes
     */
    public static List<String> getGmailScopes() {
        return Arrays
                .stream(
                        new GoogleScope[] { GMAIL, GMAIL_SETTINGS_SHARING, GMAIL_SETTINGS_BASIC, GMAIL_SEND,
                                GMAIL_READONLY, GMAIL_MODIFY, GMAIL_METADATA, GMAIL_LABELS, GMAIL_INSERT, GMAIL_COMPOSE,
                                GMAIL_ADDONS_CURRENT_MESSAGE_READONLY, GMAIL_ADDONS_CURRENT_MESSAGE_METADATA,
                                GMAIL_ADDONS_CURRENT_MESSAGE_ACTION, GMAIL_ADDONS_CURRENT_ACTION_COMPOSE })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

    /**
     * Returns OIDC authorization scopes.
     *
     * @return the OIDC authorization scopes
     */
    public static List<String> getOidcScopes() {
        return Arrays.stream(new GoogleScope[] { USER_OPENID, USER_EMAIL, USER_PROFILE }).map(GoogleScope::getScope)
                .collect(Collectors.toList());
    }

    /**
     * View And manage user's detail and GoogleScope Contacts.
     *
     * @return the people authorization scopes
     */
    public static List<String> getPeopleScopes() {
        return Arrays.stream(
                new GoogleScope[] { CONTACTS_READONLY, CONTACTS_OTHER_READONLY, CONTACTS, CONTACTS_FEEDS,
                        DIRECTORY_READONLY, USER_PHONENUMBERS_READ, USER_ORGANIZATION_READ, USER_GENDER_READ,
                        USER_EMAILS_READ, USER_BIRTHDAY_READ, USER_ADDRESSES_READ, USERINFO_PROFILE, USERINFO_EMAIL })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

    /**
     * View and manage user's photo library.
     *
     * @return the Google Photos Library authorization scopes
     */
    public static List<String> getPhotosLibraryScopes() {
        return Arrays
                .stream(
                        new GoogleScope[] { PHOTOSLIBRARY_SHARING, PHOTOSLIBRARY_READONLY_APPCREATEDDATA,
                                PHOTOSLIBRARY_READONLY, PHOTOSLIBRARY_APPENDONLY, PHOTOSLIBRARY })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

    /**
     * View And manage user's videos, activity and playlists.
     *
     * @return the YouTube authorization scopes
     */
    public static List<String> getYouTubeScopes() {
        return Arrays
                .stream(
                        new GoogleScope[] { YT_ANALYTICS_READONLY, YT_ANALYTICS_MONETARY_READONLY,
                                YOUTUBEPARTNER_CHANNEL_AUDIT, YOUTUBEPARTNER, YOUTUBE_UPLOAD, YOUTUBE_READONLY,
                                YOUTUBE_FORCE_SSL, YOUTUBE_CHANNEL_MEMBERSHIPS_CREATOR, YOUTUBE })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

    /**
     * View And manage user's GoogleScope Analytics.
     *
     * @return the Google Analytics authorization scopes
     */
    public static List<String> getGoogleAnalyticsScopes() {
        return Arrays
                .stream(
                        new GoogleScope[] { ANALYTICS_USER_DELETION, ANALYTICS_READONLY, ANALYTICS_PROVISION,
                                ANALYTICS_MANAGE_USERS_READONLY, ANALYTICS_MANAGE_USERS, ANALYTICS_EDIT, ANALYTICS })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

    /**
     * View And manage user's calendars in GoogleScope Calendar.
     *
     * @return the Google Calendar authorization scopes
     */
    public static List<String> getCalendarScopes() {
        return Arrays
                .stream(
                        new GoogleScope[] { CALENDAR_SETTINGS_READONLY, CALENDAR_READONLY, CALENDAR_EVENTS_READONLY,
                                CALENDAR_EVENTS, CALENDAR, CALENDAR_FEEDS })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

    /**
     * List, download, create, move, edit, share and search all of user's documents and files in GoogleScope Drive.
     *
     * @return the Google Drive authorization scopes
     */
    public static List<String> getDriveScopes() {
        return Arrays
                .stream(
                        new GoogleScope[] { DRIVE_SCRIPTS, DRIVE_READONLY, DRIVE_PHOTOS_READONLY,
                                DRIVE_METADATA_READONLY, DRIVE_METADATA, DRIVE_FILE, DRIVE_APPDATA,
                                DRIVE_ACTIVITY_READONLY, DRIVE_ACTIVITY, DRIVE, ACTIVITY })
                .map(GoogleScope::getScope).collect(Collectors.toList());
    }

}

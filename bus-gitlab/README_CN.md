# 🚀 GitLab API: GitLab REST API 的 Java 库

GitLab API (bus-gitlab) 提供了一个功能齐全且易于使用的 Java 库，用于通过 GitLab REST API 处理 GitLab 仓库。此外，还完全支持处理
GitLab webhook 和系统 hook。

---

## 目录

* [GitLab 服务器版本支持](#gitlab-服务器版本支持)<br/>
* [使用 GitLab-API](#使用-gitlab4j-api)<br/>
* [Java 8 要求](#java-8-要求)<br/>
* [Javadocs](#javadocs)<br/>
* [项目设置](#项目设置)<br/>
* [使用示例](#使用示例)<br/>
* [设置请求超时](#设置请求超时)<br/>
* [通过代理服务器连接](#通过代理服务器连接)<br/>
* [GitLab API V3 和 V4 支持](#gitlab-api-v3-和-v4-支持)<br/>
* [API 请求和响应的日志记录](#api-请求和响应的日志记录)<br/>
* [结果分页](#结果分页)<br/>
* [Java 8 Stream 支持](#java-8-stream-支持)<br/>
* [急切求值示例用法](#急切求值示例用法)<br/>
* [惰性求值示例用法](#惰性求值示例用法)<br/>
* [Java 8 Optional&lt;T&gt; 支持](#java-8-optional-支持)<br/>
* [Issue 时间估算](#issue-时间估算)<br/>
* [进行 API 调用](#进行-api-调用)<br/>
* [可用的子 API](#可用的子-api)

---

## GitLab 服务器版本支持

GitLab-API 支持 GitLab 社区版 [(gitlab-ce)](https://gitlab.com/gitlab-org/gitlab-ce/) 和 GitLab
企业版 [(gitlab-ee)](https://gitlab.com/gitlab-org/gitlab-ee/) 的 11.0+ 版本。

GitLab 于 2018 年 6 月发布了 GitLab 11.0 版本，其中包含许多重大更改。如果您使用的是 11.0 之前的 GitLab 服务器，强烈建议您更新
GitLab 安装或使用与您使用的 GitLab 版本同期发布的此库版本。

**注意**:
从 GitLab 11.0 开始，GitLab 服务器已移除对 GitLab API v3 的支持
(参见 https://about.gitlab.com/2018/06/01/api-v3-removal-impending/)。对 GitLab API v3 的支持将在 2019
年的某个时候从本库中移除。如果您正在使用 v3 支持，请更新代码以使用 GitLab API v4。

---

## 使用 GitLab-API

### **Javadocs**

Javadocs 可在这里获取:
[![javadoc.io](https://javadoc.io/badge2/org.gitlab4j/gitlab4j-api/javadoc.io.svg)](https://javadoc.io/doc/org.gitlab4j/gitlab4j-api)

### **项目设置**

要在 Java 项目中使用 GitLab™ API，只需在项目构建文件中添加以下依赖:<br />
**Gradle: build.gradle**

```
dependencies {
    ...
    compile group: 'org.miaixz', name: 'bus-gitlab', version: 'x.x.x'
}
```

**Maven: pom.xml**

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-gitlab</artifactId>
    <version>x.x.x</version>
</dependency>
```

### **使用示例**

GitLab-API 非常简单易用，您只需要 GitLab 服务器的 URL 和 GitLab
账户设置页面中的个人访问令牌。一旦您拥有这些信息，使用起来就这么简单:

```
// 创建一个 GitLabApi 实例来与您的 GitLab 服务器通信
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.server.com", "YOUR_PERSONAL_ACCESS_TOKEN");

// 获取您的账户有权访问的项目列表
List<Project> projects = gitLabApi.getProjectApi().getProjects();
```

您还可以使用用户名和密码登录到 GitLab 服务器:

```
// 使用用户名和密码登录到 GitLab 服务器
GitLabApi gitLabApi = GitLabApi.oauth2Login("http://your.gitlab.server.com", "username", "password");
```

从 GitLab-API 4.6.6 开始，所有 API 请求都支持以管理员身份执行 API 调用，就像另一个用户一样:

```
// 创建一个 GitLabApi 实例来与您的 GitLab 服务器通信(必须是管理员)
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.server.com", "YOUR_PERSONAL_ACCESS_TOKEN");

// 以不同用户身份执行 sudo，在本例中为用户 "johndoe"，所有未来的调用都将以 "johndoe" 身份执行
gitLabApi.sudo("johndoe")

// 关闭 sudo 模式
gitLabApi.unsudo();
```

---

### **设置请求超时**

从 GitLab-API 4.14.21 开始，已添加对设置 API 客户端的连接和读取超时的支持:

```
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", proxyConfig);

// 设置连接超时为 1 秒，读取超时为 5 秒
gitLabApi.setRequestTimeout(1000, 5000);
```

---

### **通过代理服务器连接**

从 GitLab-API 4.8.2 开始，已添加使用 HTTP 代理服务器连接到 GitLab 服务器的支持:

```
// 使用代理服务器登录到 GitLab 服务器(代理上有基本身份验证)
Map<String, Object> proxyConfig = ProxyClientConfig.createProxyClientConfig(
        "http://your-proxy-server", "proxy-username", "proxy-password");
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", null, proxyConfig);

// 使用代理服务器登录到 GitLab 服务器(代理上无身份验证)
Map<String, Object> proxyConfig = ProxyClientConfig.createProxyClientConfig("http://your-proxy-server");
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", null, proxyConfig);

// 使用 NTLM (Windows DC) 代理登录到 GitLab 服务器
Map<String, Object> ntlmProxyConfig = ProxyClientConfig.createNtlmProxyClientConfig(
        "http://your-proxy-server", "windows-username", "windows-password", "windows-workstation", "windows-domain");
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", null, ntlmProxyConfig);
```

有关接受代理配置的方法的完整列表，请参阅 GitLabApi 类上的 Javadoc (clientConfiguration 参数)

---

### **GitLab API V3 和 V4 支持**

从 GitLab-API 4.2.0 开始，已添加对 GitLab API V4 的支持。如果您的应用程序需要 GitLab API V3，您仍可按如下方式创建 GitLabApi
实例来使用 GitLab-API:

```
// 创建一个 GitLabApi 实例来使用 GitLab API V3 与您的 GitLab 服务器通信
GitLabApi gitLabApi = new GitLabApi(ApiVersion.V3, "http://your.gitlab.server.com", "YOUR_PRIVATE_TOKEN");
```

**注意**:
从 GitLab 11.0 开始，GitLab 服务器已移除对 GitLab API v3 的支持
(参见 https://about.gitlab.com/2018/06/01/api-v3-removal-impending/)。对 GitLab API v3 的支持将在 2019
年的某个时候从本库中移除。如果您正在使用 v3 支持，请更新代码以使用 GitLab API v4。

---

### **API 请求和响应的日志记录**

从 GitLab-API 4.8.39 开始，已添加日志记录功能以记录对 GitLab API 的请求和响应。使用 GitLabApi
实例上的以下方法之一启用日志记录:

```
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.server.com", "YOUR_PERSONAL_ACCESS_TOKEN");

// 使用共享日志记录器和默认的 FINE 级别记录
gitLabApi.enableRequestResponseLogging();

// 使用共享日志记录器和 INFO 级别记录
gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO);

// 使用指定的日志记录器和 INFO 级别记录
gitLabApi.enableRequestResponseLogging(yourLoggerInstance, java.util.logging.Level.INFO);

// 使用共享日志记录器、INFO 级别记录，并包含最多 1024 字节的实体日志记录
gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO, 1024);

// 使用指定的日志记录器、INFO 级别记录，并包含最多 1024 字节的实体日志记录
gitLabApi.enableRequestResponseLogging(yourLoggerInstance, java.util.logging.Level.INFO, 1024);
```

---

### **结果分页**

GitLab-API 提供了一个易于使用的分页机制来分页浏览 GitLab API 的结果列表。以下是关于如何使用 Pager 的几个示例:

```
// 获取一个 Pager 实例，每页将分页浏览 10 个项目
Pager<Project> projectPager = gitlabApi.getProjectsApi().getProjects(10);

// 遍历页面并打印出名称和描述
while (projectsPager.hasNext())) {
    for (Project project : projectPager.next()) {
        System.out.println(project.getName() + " -: " + project.getDescription());
    }
}
```

从 GitLab-API 4.9.2 开始，您还可以使用 Pager 实例将所有项目作为单个列表获取:

```
// 获取一个 Pager 实例，以便我们可以将所有项目加载到单个列表中，每次 10 个项目:
Pager<Project> projectPager = gitlabApi.getProjectsApi().getProjects(10);
List<Project> allProjects = projectPager.all();
```

---

### **Java 8 Stream 支持**

从 GitLab-API 4.9.2 开始，所有返回 List 结果的 GitLabJ-API 方法都有一个类似命名的返回 Java 8 Stream 的方法。返回 Stream
的方法使用以下命名约定:```getXxxxxStream()```。

**重要**
内置的返回 Stream 的方法使用___急切求值___，意味着所有项目都从 GitLab 服务器预先获取，然后返回一个 Stream 来流式传输这些项目。
**急切求值不支持从服务器的并行读取，但它确实支持获取数据后的 Stream 并行处理。**

要使用___惰性求值___进行流式传输，请使用返回```Pager```实例的 GitLab-API 方法，然后在```Pager```实例上调用
```lazyStream()```方法来创建惰性求值 Stream。Stream 使用```Pager```实例来分页浏览可用项目。 **惰性 Stream
不支持并行操作或跳过。**

#### **急切求值条件用法:**

```
// 流式传输可见项目，打印出项目名称。
Stream<Project> projectStream = gitlabApi.getProjectApi().getProjectsStream();
projectStream.map(Project::getName).forEach(name -> System.out.println(name));

// 并行操作流，此条件按用户名排序 User 实例
// 注意: 用户的获取不是并行进行的，
// 只有用户的排序是并行操作。
Stream<User> stream = gitlabApi.getUserApi().getUsersStream();
List<User> users = stream.parallel().sorted(comparing(User::getUsername)).collect(toList());
```

#### **惰性求值条件用法:**

```
// 获取一个 Pager 实例，用于惰性流式传输 Project 实例。
// 在此条件下，每次将预取 10 个项目。
Pager<Project> projectPager = gitlabApi.getProjectApi().getProjects(10);

// 惰性流式传输项目，打印出每个项目名称，将输出限制为 5 个项目名称
projectPager.lazyStream().limit(5).map(Project::getName).forEach(name -> System.out.println(name));
```

---

### **Java 8 Optional 支持**

GitLab-API 支持返回单个项目的 API 调用使用 Java 8 Optional&lt;T&gt;。以下是关于如何使用 Java 8 Optional&lt;T&gt; API
调用的条件:

```
Optional<Group> optionalGroup =  gitlabApi.getGroupApi().getOptionalGroup("my-group-path");
if (optionalGroup.isPresent())
    return optionalGroup.get();

return gitLabApi.getGroupApi().addGroup("my-group-name", "my-group-path");
```

---

### **Issue 时间估算**

GitLab issues 允许时间跟踪。目前可用以下时间单位:

* 月 (mo)
* 周 (w)
* 天 (d)
* 小时 (h)
* 分钟 (m)

转换率为 1mo = 4w，1w = 5d，1d = 8h。

---

## 进行 API 调用

API 已被分解为子 API 类，以便更容易使用和分离关注点。GitLab 子 API 类通常与 [GitLab API](https://docs.gitlab.com/ce/api/)
上的 API 文档具有一对一的关系。以下是 GitLab 子 API 类映射到 GitLab API 文档的示例:

```GroupApi``` -> https://docs.gitlab.com/ce/api/groups.html<br/>
```MergeRequestApi``` -> https://docs.gitlab.com/ce/api/merge_requests.html<br/>
```ProjectApi``` -> https://docs.gitlab.com/ce/api/projects.html<br/>
```UserApi``` -> https://docs.gitlab.com/ce/api/users.html<br/>

### **可用的子 API**

以下是可用的子 API 列表以及每个 API 的使用示例。有关每个子 API
的可用方法的完整列表，请参阅 <a href="https://javadoc.io/doc/org.gitlab4j/gitlab4j-api" target="_top">Javadocs</a>。

---
&nbsp;&nbsp;[ApplicationsApi](#applicationsapi)<br/>
&nbsp;&nbsp;[ApplicationSettingsApi](#applicationsettingsapi)<br/>
&nbsp;&nbsp;[AwardEmojiApi](#awardemojiapi)<br/>
&nbsp;&nbsp;[BoardsApi](#boardsapi)<br/>
&nbsp;&nbsp;[CommitsApi](#commitsapi)<br/>
&nbsp;&nbsp;[ContainerRegistryApi](#containerregistryapi)<br/>
&nbsp;&nbsp;[DeployKeysApi](#deploykeysapi)<br/>
&nbsp;&nbsp;[DiscussionsApi](#discussionsapi)<br/>
&nbsp;&nbsp;[EnvironmentsApi](#environmentsapi)<br/>
&nbsp;&nbsp;[EpicsApi](#epicsapi)<br/>
&nbsp;&nbsp;[EventsApi](#eventsapi)<br/>
&nbsp;&nbsp;[GroupApi](#groupapi)<br/>
&nbsp;&nbsp;[HealthCheckApi](#healthcheckapi)<br/>
&nbsp;&nbsp;[ImportExportApi](#importexportapi)<br/>
&nbsp;&nbsp;[IssuesApi](#issuesapi)<br/>
&nbsp;&nbsp;[JobApi](#jobapi)<br/>
&nbsp;&nbsp;[LabelsApi](#labelsapi)<br/>
&nbsp;&nbsp;[LicenseApi](#licenseapi)<br/>
&nbsp;&nbsp;[LicenseTemplatesApi](#licensetemplatesapi)<br/>
&nbsp;&nbsp;[LabelsApi](#labelsapi)<br/>
&nbsp;&nbsp;[MergeRequestApi](#mergerequestapi)<br/>
&nbsp;&nbsp;[MilestonesApi](#milestonesapi)<br/>
&nbsp;&nbsp;[NamespaceApi](#namespaceapi)<br/>
&nbsp;&nbsp;[NotesApi](#notesapi)<br/>
&nbsp;&nbsp;[NotificationSettingsApi](#notificationsettingsapi)<br/>
&nbsp;&nbsp;[PackagesApi](#packagesapi)<br/>
&nbsp;&nbsp;[PipelineApi](#pipelineapi)<br/>
&nbsp;&nbsp;[ProjectApi](#projectapi)<br/>
&nbsp;&nbsp;[ProtectedBranchesApi](#protectedbranchesapi)<br/>
&nbsp;&nbsp;[ReleasesApi](#releasesapi)<br/>
&nbsp;&nbsp;[RepositoryApi](#repositoryapi)<br/>
&nbsp;&nbsp;[RepositoryFileApi](#repositoryfileapi)<br/>
&nbsp;&nbsp;[ReourceLabelEventsApi](#resourcelabeleventsapi)<br/>
&nbsp;&nbsp;[RunnersApi](#runnersapi)<br/>
&nbsp;&nbsp;[SearchApi](#searchapi)<br/>
&nbsp;&nbsp;[ServicesApi](#servicesapi)<br/>
&nbsp;&nbsp;[SessionApi](#sessionapi)<br/>
&nbsp;&nbsp;[SnippetsApi](#snippetsapi)<br/>
&nbsp;&nbsp;[SystemHooksApi](#systemhooksapi)<br/>
&nbsp;&nbsp;[TagsApi](#tagsapi)<br/>
&nbsp;&nbsp;[TodosApi](#todosapi)<br/>
&nbsp;&nbsp;[UserApi](#userapi)<br/>
&nbsp;&nbsp;[WikisApi](#wikisapi)


### 子 API 示例
----------------

#### ApplicationsApi

```
// 向 GitLab 添加 OAUTH 应用程序
ApplicationScope[] scopes = {ApplicationScope.SUDO, ApplicationScope.PROFILE};
gitLabApi.getApplicationsApi().createApplication("My OAUTH Application", "https//condition.com/myapp/callback", scopes);
```

#### ApplicationSettingsApi

```
// 获取当前 GitLab 服务器应用程序设置
ApplicationSettings appSettings = gitLabApi.getApplicationSettingsApi().getAppliationSettings();
```

#### AwardEmojiApi

```
// 获取属于指定 issue 的 AwardEmoji 列表(组 ID = 1，issues IID = 1)
List<AwardEmoji> awardEmojis = gitLabApi.getAwardEmojiApi().getIssuAwardEmojis(1, 1);
```

#### BoardsApi

```
// 获取属于指定项目的 Issue Boards 列表
List<Board> boards = gitLabApi.getBoardsApi().getBoards(projectId);
```

#### CommitsApi

```
// 获取与指定分支相关的提交列表，这些提交落在指定时间窗口内
// 这使用 ISO8601 类中的 ISO8601 日期工具
Date since = ISO8601.toDate("2017-01-01T00:00:00Z");
Date until = new Date(); // 现在
List<Commit> commits = gitLabApi.getCommitsApi().getCommits(1234, "new-feature", since, until);
```

#### ContainerRegistryApi

```
// 获取属于指定项目的注册表仓库列表
List<RegistryRepository> registryRepos = gitLabApi.ContainerRegistryApi().getRepositories(projectId);
```

#### DeployKeysApi

```
// 获取已认证用户的 DeployKeys 列表
List<DeployKey> deployKeys = gitLabApi.getDeployKeysApi().getDeployKeys();
```

#### DiscussionsApi

```
// 获取指定合并请求的 Discussions 列表
List<Discussion> discussions = gitLabApi.getDiscussionsApi().getMergeRequestDiscussions(projectId, mergeRequestIid);
```

#### EnvironmentsApi

```
// 获取指定项目的 Environments 列表
List<Environment> environments = gitLabApi.getEnvironmentsApi().getEnvironments(projectId);
```

#### EpicsApi

```
// 获取所请求组及其子组的 epic 列表。
List<Epic> epics = gitLabApi.getEpicsApi().getEpics(1);
```

#### EventsApi

```
// 获取已认证用户的事件列表
Date after = new Date(0); // Epoch 之后
Date before = new Date(); // 现在之前
List<Event> events = gitLabApi.getEventsApi().getAuthenticatedUserEvents(null, null, before, after, DESC);
```

#### GroupApi

```
// 获取您有权访问的组列表
List<Group> groups = gitLabApi.getGroupApi().getGroups();
```

#### HealthCheckApi

```
// 获取活跃端点健康检查结果。假定按照以下方式进行了 ip 白名单:
// https://docs.gitlab.com/ee/administration/monitoring/ip_whitelist.html
HealthCheckInfo healthCheck = gitLabApi.getHealthCheckApi().getLiveness();
```

#### ImportExportApi

```
// 为指定的项目 ID 安排项目导出
gitLabApi.getImportExportApi().scheduleExport(projectId);

// 获取指定项目 ID 的项目导出状态
ExportStatus exportStatus = gitLabApi.getImportExportApi().getExportStatus(projectId);
```

#### IssuesApi

```
// 获取指定项目 ID 的 issues 列表
List<Issue> issues = gitLabApi.getIssuesApi().getIssues(1234);
```

#### JobApi

```
// 获取指定项目 ID 的作业列表
List<Job> jobs = gitLabApi.getJobApi().getJobs(1234);
```

#### LabelsApi

```
// 获取指定项目 ID 的标签列表
List<Label> labels = gitLabApi.getLabelsApi().getLabels(1234);
```

#### LicenseApi

```
// 检索有关当前许可证的信息
License license = gitLabApi.getLicenseApi().getLicense();
```

#### LicenseTemplatesApi

```
// 获取开源许可证模板列表
List<LicenseTemplate> licenses = gitLabApi.getLicenseTemplatesApi().getLicenseTemplates();
```

#### MergeRequestApi

```
// 获取指定项目的合并请求列表
List<MergeRequest> mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(1234);
```

#### MilestonesApi

```
// 获取指定项目的里程碑列表
List<Milestone> milestones = gitLabApi.getMilestonesApi().getMilestones(1234);
```

#### NamespaceApi

```
// 获取名称或路径中匹配 "foobar" 的所有命名空间
List<Namespace> namespaces = gitLabApi.getNamespaceApi().findNamespaces("foobar");
```

#### NotesApi

```
// 获取项目 ID 1234、issue IID 1 的 issues 注释列表
List<Note> notes = gitLabApi.getNotesApi().getNotes(1234, 1);
```

#### NotificationSettingsApi

```
// 获取当前全局通知设置
NotificationSettings settings = gitLabApi.getNotificationSettingsApi().getGlobalNotificationSettings();
```

#### PackagesApi

```
// 获取指定项目 ID 的所有包
List<Packages> packages = gitLabApi.getPackagesApi().getPackages(1234);
```

#### PipelineApi

```
// 获取指定项目 ID 的所有管道
List<Pipeline> pipelines = gitLabApi.getPipelineApi().getPipelines(1234);
```

#### ProjectApi

```
// 获取可访问的项目列表
public List<Project> projects = gitLabApi.getProjectApi().getProjects();
```

```
// 创建新项目
Project projectSpec = new Project()
    .withName("my-project")
    .withDescription("My project for demonstration.")
    .withIssuesEnabled(true)
    .withMergeRequestsEnabled(true)
    .withWikiEnabled(true)
    .withSnippetsEnabled(true)
    .withPublic(true);

Project newProject = gitLabApi.getProjectApi().createProject(projectSpec);
```

#### ProtectedBranchesApi

```
List<ProtectedBranch> branches = gitLabApi.getProtectedBranchesApi().getProtectedBranches(project.getId());
```

#### ReleasesApi

```
// 获取指定项目的发布列表
List<Release> releases = gitLabApi.getReleasesApi().getReleases(projectId);
```

#### RepositoryApi

```
// 从项目获取存储库分支列表，按名称字母顺序排序
List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(projectId);
```

```
// 按名称从项目搜索存储库分支
List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(projectId, searchTerm);
```

#### RepositoryFileApi

```
// 从存储库中的文件获取信息(名称、大小等)和内容
RepositoryFile file = gitLabApi.getRepositoryFileApi().getFile("file-path", 1234, "ref");
```

#### ResourceLabelEventsApi

```
// 获取指定合并请求的标签事件
List<LabelEvent> labelEvents = gitLabApi.getResourceLabelEventsApi()
        .getMergeRequestLabelEvents(projectId, mergeRequestIid);
```

#### RunnersApi

```
// 获取所有 Runners。
List<Runner> runners = gitLabApi.getRunnersApi().getAllRunners();
```

#### SearchApi

```
// 全局搜索项目
List<?> projects = gitLabApi.getSearchApi().globalSearch(SearchScope.PROJECTS, "text-to-search-for");
```

#### ServicesApi

```
// 激活/更新 Slack 通知服务
SlackService slackService =  new SlackService()
        .withMergeRequestsEvents(true)
        .withWebhook("https://hooks.slack.com/services/ABCDEFGHI/KJLMNOPQR/wetrewq7897HKLH8998wfjjj")
        .withUsername("GitLab");
gitLabApi.getServicesApi().updateSlackService("project-path", slackService);
```

#### SessionApi

```
// 登录到 GitLab 服务器并获取会话信息
gitLabApi.getSessionApi().login("your-username", "your-email", "your-password");
```

#### SnippetsApi

```
// 获取已认证用户的代码片段列表
List<Snippet> snippets = gitLabApi.getSnippetsApi().getSnippets();
```

#### SystemHooksApi

```
// 获取已安装的系统钩子列表
List<SystemHook> hooks = gitLabApi.getSystemHooksApi().getSystemHooks();
```

#### TagsApi

```
// 获取指定项目 ID 的标签列表
List<Tag> tags = gitLabApi.getTagsApi().getTags(projectId);
```

#### TodosApi

```
// 获取当前用户的所有待办 todos 列表
List<Todo> todos = gitLabApi.getTodosApi().gePendingTodos();
```

#### UserApi

```
// 获取 user_id 1 的用户信息
User user = gitLabApi.getUserApi().getUser(1);

// 创建一个没有密码的新用户，该用户将收到重置密码电子邮件
User userConfig = new User()
    .withEmail("jdoe@condition.com")
    .withName("Jane Doe")
    .withUsername("jdoe");
String password = null;
boolean sendResetPasswordEmail = true;
gitLabApi.getUserApi().createUser(userConfig, password, sendResetPasswordEmail);
```

#### WikisApi

```
// 获取项目 wiki 中的页面列表
List<WikiPage> wikiPages = gitLabApi.getWikisApi().getPages();
```

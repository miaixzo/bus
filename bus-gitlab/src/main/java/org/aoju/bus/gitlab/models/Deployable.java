package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.Constants.DeploymentStatus;
import org.aoju.bus.gitlab.utils.JacksonJson;

import java.util.Date;
import java.util.List;

public class Deployable {

    private Integer id;
    private DeploymentStatus status;
    private String stage;
    private String name;
    private String ref;
    private Boolean tag;
    private Float coverage;
    private Date createdAt;
    private Date startedAt;
    private Date finishedAt;
    private Double duration;
    private org.aoju.bus.gitlab.models.User user;
    private org.aoju.bus.gitlab.models.Commit commit;
    private org.aoju.bus.gitlab.models.Pipeline pipeline;
    private String webUrl;
    private List<org.aoju.bus.gitlab.models.Artifact> artifacts;
    private org.aoju.bus.gitlab.models.Runner runner;
    private Date artifactsExpireAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Boolean getTag() {
        return tag;
    }

    public void setTag(Boolean tag) {
        this.tag = tag;
    }

    public Float getCoverage() {
        return coverage;
    }

    public void setCoverage(Float coverage) {
        this.coverage = coverage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public org.aoju.bus.gitlab.models.User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public org.aoju.bus.gitlab.models.Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public org.aoju.bus.gitlab.models.Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public List<org.aoju.bus.gitlab.models.Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public org.aoju.bus.gitlab.models.Runner getRunner() {
        return runner;
    }

    public void setRunner(Runner runner) {
        this.runner = runner;
    }

    public Date getArtifactsExpireAt() {
        return artifactsExpireAt;
    }

    public void setArtifactsExpireAt(Date artifactsExpireAt) {
        this.artifactsExpireAt = artifactsExpireAt;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}

package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.utils.JacksonJson;

public class TimeStats {

    private Integer timeEstimate;
    private Integer totalTimeSpent;
    private org.aoju.bus.gitlab.models.Duration humanTimeEstimate;
    private org.aoju.bus.gitlab.models.Duration humanTotalTimeSpent;

    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    public void setTimeEstimate(Integer timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    public Integer getTotalTimeSpent() {
        return totalTimeSpent;
    }

    public void setTotalTimeSpent(Integer totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    public org.aoju.bus.gitlab.models.Duration getHumanTimeEstimate() {
        return humanTimeEstimate;
    }

    public void setHumanTimeEstimate(org.aoju.bus.gitlab.models.Duration humanTimeEstimate) {
        this.humanTimeEstimate = humanTimeEstimate;
    }

    public org.aoju.bus.gitlab.models.Duration getHumanTotalTimeSpent() {
        return humanTotalTimeSpent;
    }

    public void setHumanTotalTimeSpent(Duration humanTotalTimeSpent) {
        this.humanTotalTimeSpent = humanTotalTimeSpent;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}

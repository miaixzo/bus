package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.Constants.LineType;
import org.aoju.bus.gitlab.utils.JacksonJson;

import java.util.Date;

public class Comment {

    private org.aoju.bus.gitlab.models.Author author;
    private Date createdAt;
    private LineType lineType;
    private String path;
    private Integer line;
    private String note;

    public org.aoju.bus.gitlab.models.Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public LineType getLineType() {
        return lineType;
    }

    public void setLineType(LineType lineType) {
        this.lineType = lineType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}

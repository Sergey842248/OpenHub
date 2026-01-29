package com.thirtydegreesray.openhub.http.model;

import com.google.gson.annotations.SerializedName;

public class GitHubUploadResponseModel {

    private String markdown;

    private String href;

    private String name;

    private String url;

    @SerializedName("content_type")
    private String contentType;

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

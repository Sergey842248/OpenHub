

package com.thirtydegreesray.openhub.mvp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created on 2017/7/14.
 *
 * @author ThirtyDegreesRay
 */

public class OauthToken {

    @SerializedName("access_token")
    private String accessToken;

    private String scope;

    private String error;

    @SerializedName("error_description")
    private String errorDescription;

    public OauthToken() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}

package com.thirtydegreesray.openhub.http;

import androidx.annotation.NonNull;

import com.thirtydegreesray.openhub.http.model.GitHubUploadResponseModel;

import okhttp3.MultipartBody;
import retrofit2.Response;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

public interface UploadService {

    @NonNull
    @Multipart
    @POST("repos/{owner}/{repo}/issues/{issueNumber}/comments")
    @Headers("Accept: application/vnd.github.v3+json")
    Observable<Response<GitHubUploadResponseModel>> uploadIssueCommentAttachment(
            @Path("owner") String owner,
            @Path("repo") String repo,
            @Path("issueNumber") int issueNumber,
            @Part MultipartBody.Part file
    );
}

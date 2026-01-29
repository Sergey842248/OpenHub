package com.thirtydegreesray.openhub.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.AppConfig;
import com.thirtydegreesray.openhub.AppData;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.http.UploadService;
import com.thirtydegreesray.openhub.http.core.AppRetrofit;
import com.thirtydegreesray.openhub.http.model.GitHubUploadResponseModel;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.ui.activity.MarkdownEditorCallback;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;
import com.thirtydegreesray.openhub.ui.widget.ToastAbleImageButton;
import com.thirtydegreesray.openhub.util.BundleHelper;
import com.thirtydegreesray.openhub.util.ImageUploadUtils;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ThirtyDegreesRay on 2017/9/29 11:52:17
 */

public class MarkdownEditorFragment extends BaseFragment
        implements MarkdownEditorCallback {

    private static final int PICK_IMAGE_REQUEST_CODE = 2001;

    public static MarkdownEditorFragment create(@Nullable String text, ArrayList<String> mentionUsers) {
        return create(text, mentionUsers, null, null, -1);
    }

    public static MarkdownEditorFragment create(@Nullable String text,
                                               @Nullable ArrayList<String> mentionUsers,
                                               @Nullable String uploadOwner,
                                               @Nullable String uploadRepoName,
                                               int uploadIssueNumber) {
        MarkdownEditorFragment fragment = new MarkdownEditorFragment();
        fragment.setArguments(BundleHelper.builder()
                .put("text", text)
                .put("mentionUsers", mentionUsers)
                .put("uploadOwner", uploadOwner)
                .put("uploadRepoName", uploadRepoName)
                .put("uploadIssueNumber", uploadIssueNumber)
                .build());
        return fragment;
    }

    @BindView(R2.id.markdown_edit)
    EditText markdownEdit;

    @BindView(R2.id.add_mention)
    ToastAbleImageButton addMention;

    @BindView(R2.id.add_image)
    ToastAbleImageButton addImage;

    @AutoAccess
    boolean isTextChanged = false;

    @AutoAccess
    String text;

    @AutoAccess
    ArrayList<String> mentionUsers;

    @AutoAccess
    String uploadOwner;

    @AutoAccess
    String uploadRepoName;

    @AutoAccess
    int uploadIssueNumber = -1;

    private PopupMenu popupMenu;
    private Subscription uploadSubscription;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_markdown_editor;
    }

    @Override
    protected void setupFragmentComponent(AppComponent appComponent) {

    }

    @Override
    protected void initFragment(Bundle savedInstanceState) {
        if (!StringUtils.isBlank(text)) {
            isTextChanged = true;
            markdownEdit.setText(text);
            markdownEdit.setSelection(text.length());
        }

        addImage.setVisibility(isImageUploadEnabled() ? View.VISIBLE : View.GONE);
    }

    @OnTextChanged(R2.id.markdown_edit)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isTextChanged = true;
    }

    @OnTextChanged(value = R2.id.markdown_edit, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s) {
        int cursorIndex = markdownEdit.getSelectionStart();
        String curContent = markdownEdit.getText().toString();
        String preStr = curContent.substring(0, cursorIndex);
        if (preStr.endsWith("@")) {
            showMentionView();
        } else {
            if (popupMenu != null) popupMenu.dismiss();
        }
    }

    @Override
    public String getText() {
        return markdownEdit == null ? null : markdownEdit.getText().toString();
    }

    @Override
    public boolean isTextChanged() {
        isTextChanged = !isTextChanged;
        return !isTextChanged;
    }

    @OnClick({R2.id.add_large_head, R2.id.add_medium_head, R2.id.add_small_head, R2.id.add_bold,
            R2.id.add_italic, R2.id.add_quote, R2.id.insert_code, R2.id.add_link, R2.id.add_bulleted_list,
            R2.id.add_image, R2.id.add_mention})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_large_head:
                addKeyWord("#");
                break;
            case R.id.add_medium_head:
                addKeyWord("##");
                break;
            case R.id.add_small_head:
                addKeyWord("###");
                break;
            case R.id.add_bold:
                addKeyWord("****", 2);
                break;
            case R.id.add_italic:
                addKeyWord("__", 1);
                break;
            case R.id.add_quote:
                addKeyWord(">");
                break;
            case R.id.insert_code:
                addKeyWord("``", 1);
                break;
            case R.id.add_link:
                addKeyWord("[](url)", 1);
                break;
            case R.id.add_bulleted_list:
                addKeyWord("-");
                break;
            case R.id.add_image:
                pickImage();
                break;
            case R.id.add_mention:
                addKeyWord("@", 1);
                afterTextChanged(markdownEdit.getText());
                break;
        }
    }

    private boolean isImageUploadEnabled() {
        return !StringUtils.isBlank(uploadOwner) && !StringUtils.isBlank(uploadRepoName) && uploadIssueNumber > 0;
    }

    private void pickImage() {
        if (!isImageUploadEnabled()) {
            showWarningToast(getString(R.string.image_upload_not_supported));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == PICK_IMAGE_REQUEST_CODE && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                uploadImage(uri);
            }
        }
    }

    private void uploadImage(@NonNull Uri uri) {
        if (uploadSubscription != null && !uploadSubscription.isUnsubscribed()) {
            uploadSubscription.unsubscribe();
        }

        showProgressDialog(getString(R.string.uploading_image));
        uploadSubscription = Observable.fromCallable(() -> ImageUploadUtils.processImageForUpload(requireContext(), uri))
                .subscribeOn(Schedulers.io())
                .flatMap(processedFile -> getUploadService()
                        .uploadIssueCommentAttachment(uploadOwner, uploadRepoName, uploadIssueNumber,
                                processedFile.toMultipartBodyPart())
                        .subscribeOn(Schedulers.io())
                        .flatMap(this::toMarkdownObservable))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        dismissProgressDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissProgressDialog();
                        showErrorToast(StringUtils.isBlank(e.getMessage()) ? e.toString() : e.getMessage());
                    }

                    @Override
                    public void onNext(String markdown) {
                        insertImageMarkdown(markdown);
                        showSuccessToast(getString(R.string.image_uploaded));
                    }
                });
    }

    private Observable<String> toMarkdownObservable(@NonNull Response<GitHubUploadResponseModel> response) {
        if (response.isSuccessful()
                && response.body() != null
                && !TextUtils.isEmpty(response.body().getMarkdown())) {
            return Observable.just(response.body().getMarkdown());
        }
        return Observable.error(new IOException(response.message()));
    }

    private UploadService getUploadService() {
        return AppRetrofit.INSTANCE
                .getRetrofit(AppConfig.GITHUB_UPLOAD_BASE_URL, AppData.INSTANCE.getAccessToken())
                .create(UploadService.class);
    }

    private void insertImageMarkdown(@NonNull String markdown) {
        int cursorIndex = markdownEdit.getSelectionStart();
        String curContent = markdownEdit.getText().toString();

        String preStr = curContent.substring(0, cursorIndex);
        String sufStr = curContent.substring(cursorIndex);

        String prefix = "";
        if (!StringUtils.isBlank(preStr) && preStr.charAt(preStr.length() - 1) != '\n') {
            prefix = "\n";
        }

        String insert = prefix + markdown + "\n";
        String newStr = preStr + insert + sufStr;
        int newCursorIndex = preStr.length() + insert.length();

        markdownEdit.setText(newStr);
        markdownEdit.setSelection(newCursorIndex);
    }

    private void addKeyWord(String keyWord) {
        addKeyWord(keyWord, -1);
    }

    private void addKeyWord(String keyWord, int cursorPosition) {
        addKeyWord(keyWord, cursorPosition, true);
    }

    private void addKeyWord(String keyWord, int cursorPosition, boolean preBlankEnable) {
        int cursorIndex = markdownEdit.getSelectionStart();
        String curContent = markdownEdit.getText().toString();
        String preStr = curContent.substring(0, cursorIndex);
        String sufStr = curContent.substring(cursorIndex);
        boolean needPreBlank = preBlankEnable
                && !StringUtils.isBlank(preStr)
                && preStr.charAt(preStr.length() - 1) != ' '
                && preStr.charAt(preStr.length() - 1) != '\n';
        String newStr = preStr;
        if (needPreBlank) {
            newStr = newStr.concat(" ");
        }
        newStr = newStr.concat(keyWord).concat(" ").concat(sufStr);
        int newCursorIndex = cursorIndex + (needPreBlank ? 1 : 0)
                + (cursorPosition == -1 ? keyWord.length() + 1 : cursorPosition);
        markdownEdit.setText(newStr);
        markdownEdit.setSelection(newCursorIndex);
    }

    private void showMentionView() {
        if (mentionUsers != null && mentionUsers.size() > 0) {
            if (popupMenu == null) {
                popupMenu = new PopupMenu(getActivity(), addMention, Gravity.BOTTOM,
                        R.attr.popup_menu_theme, 0);
                Menu menu = popupMenu.getMenu();
                for (String loginId : mentionUsers) {
                    menu.add("@" + loginId);
                }
                popupMenu.setOnMenuItemClickListener(item -> {
                    String loginId = item.getTitle().toString().substring(1);
                    addKeyWord(loginId, -1, false);
                    return false;
                });
            }
            popupMenu.show();
        }
    }

    @Override
    public void onDestroyView() {
        if (uploadSubscription != null && !uploadSubscription.isUnsubscribed()) {
            uploadSubscription.unsubscribe();
        }
        super.onDestroyView();
    }

}

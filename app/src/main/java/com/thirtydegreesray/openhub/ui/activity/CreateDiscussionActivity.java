package com.thirtydegreesray.openhub.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerActivityComponent;
import com.thirtydegreesray.openhub.inject.module.ActivityModule;
import com.thirtydegreesray.openhub.mvp.contract.ICreateDiscussionContract;
import com.thirtydegreesray.openhub.mvp.model.Discussion;
import com.thirtydegreesray.openhub.mvp.model.DiscussionCategory;
import com.thirtydegreesray.openhub.mvp.presenter.CreateDiscussionPresenter;
import com.thirtydegreesray.openhub.ui.activity.base.BaseActivity;
import com.thirtydegreesray.openhub.util.BundleHelper;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Activity for creating new discussions
 */

public class CreateDiscussionActivity extends BaseActivity<CreateDiscussionPresenter>
        implements ICreateDiscussionContract.View {

    @BindView(R2.id.title_edit) TextInputEditText titleEdit;
    @BindView(R2.id.title_layout) TextInputLayout titleLayout;
    @BindView(R2.id.body_edit) TextInputEditText bodyEdit;
    @BindView(R2.id.body_layout) TextInputLayout bodyLayout;
    @BindView(R2.id.category_spinner) Spinner categorySpinner;

    public static void show(@NonNull Activity activity, @NonNull String userId,
                           @NonNull String repoName, int requestCode) {
        Intent intent = new Intent(activity, CreateDiscussionActivity.class);
        intent.putExtras(BundleHelper.builder().put("userId", userId)
                .put("repoName", repoName).build());
        activity.startActivityForResult(intent, requestCode);
    }

    private final int MARKDOWN_EDITOR_REQUEST_CODE = 100;
    @AutoAccess String titleEditStr;
    @AutoAccess String bodyEditStr;

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .activityModule(new ActivityModule(getActivity()))
                .build()
                .inject(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_create_discussion;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_commit){
            if(checkForCommit())
                mPresenter.createDiscussion(titleEdit.getText().toString(), bodyEdit.getText().toString());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setToolbarBackEnable();
        setToolbarTitle(getString(R.string.create_discussion));

        titleEdit.setText(mPresenter.getTitle());
        bodyEdit.setText(mPresenter.getBody());
        titleEdit.setSelection(titleEdit.length());
        bodyEdit.setSelection(bodyEdit.length());
    }

    @Override
    public void showNewDiscussion(@NonNull Discussion discussion) {
        showSuccessToast(getString(R.string.create_discussion_success));
        Intent data = new Intent();
        data.putExtra("discussion", discussion);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void showCategories(ArrayList<DiscussionCategory> categories) {
        ArrayAdapter<DiscussionCategory> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    @OnClick(R2.id.markdown_editor_bn)
    public void onMarkdownEditorClick() {
        MarkdownEditorActivity.show(getActivity(), R.string.edit,
                MARKDOWN_EDITOR_REQUEST_CODE, bodyEdit.getText().toString());
    }

    private boolean checkForCommit(){
        if(StringUtils.isBlank(titleEdit.getText().toString())){
            titleLayout.setError(getString(R.string.discussion_title_null_tip));
            return false;
        }else {
            titleLayout.setErrorEnabled(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        if(requestCode == MARKDOWN_EDITOR_REQUEST_CODE){
            String text = data.getStringExtra("text");
            bodyEdit.setText(text);
            bodyEdit.requestFocus();
            bodyEdit.setSelection(text.length());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        titleEditStr = titleEdit.getText().toString();
        bodyEditStr = bodyEdit.getText().toString();
    }
}

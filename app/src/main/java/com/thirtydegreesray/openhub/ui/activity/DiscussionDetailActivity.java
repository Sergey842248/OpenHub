package com.thirtydegreesray.openhub.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.mvp.model.Discussion;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity for displaying discussion details
 */
public class DiscussionDetailActivity extends AppCompatActivity {

    @BindView(R2.id.title_text) TextView titleText;
    @BindView(R2.id.body_text) TextView bodyText;

    public static void show(@NonNull Context activity, @NonNull Discussion discussion) {
        Intent intent = new Intent(activity, DiscussionDetailActivity.class);
        intent.putExtra("discussion", discussion);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion_detail);
        ButterKnife.bind(this);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.discussion));
        }

        Discussion discussion = getIntent().getParcelableExtra("discussion");
        if (discussion != null) {
            titleText.setText(discussion.getTitle());
            bodyText.setText(discussion.getBody());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

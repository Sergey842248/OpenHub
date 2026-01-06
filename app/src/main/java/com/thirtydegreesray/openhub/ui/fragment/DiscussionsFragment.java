package com.thirtydegreesray.openhub.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.IDiscussionsContract;
import com.thirtydegreesray.openhub.mvp.model.Discussion;
import com.thirtydegreesray.openhub.mvp.presenter.DiscussionsPresenter;
import com.thirtydegreesray.openhub.ui.activity.DiscussionDetailActivity;
import com.thirtydegreesray.openhub.ui.adapter.DiscussionsAdapter;
import com.thirtydegreesray.openhub.ui.fragment.base.ListFragment;
import com.thirtydegreesray.openhub.util.BundleHelper;

import java.util.ArrayList;

/**
 * Fragment for displaying repository discussions
 */

public class DiscussionsFragment extends ListFragment<DiscussionsPresenter, DiscussionsAdapter>
        implements IDiscussionsContract.View {

    public static DiscussionsFragment create(@NonNull String owner, @NonNull String repoName){
        DiscussionsFragment fragment = new DiscussionsFragment();
        fragment.setArguments(BundleHelper.builder()
                .put("owner", owner)
                .put("repoName", repoName).build());
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    protected void setupFragmentComponent(AppComponent appComponent) {
        DaggerFragmentComponent.builder()
                .appComponent(appComponent)
                .fragmentModule(new FragmentModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void initFragment(Bundle savedInstanceState) {
        super.initFragment(savedInstanceState);
        setLoadMoreEnable(true);
    }

    @Override
    protected void onReLoadData() {
        mPresenter.loadDiscussions(1, true);
    }

    @Override
    protected String getEmptyTip() {
        return getString(R.string.no_discussions);
    }

    @Override
    public void showDiscussions(ArrayList<Discussion> discussions) {
        adapter.setData(discussions);
        postNotifyDataSetChanged();
    }

    @Override
    protected void onLoadMore(int page) {
        super.onLoadMore(page);
        mPresenter.loadDiscussions(page, false);
    }

    @Override
    public void onItemClick(int position, @NonNull View view) {
        super.onItemClick(position, view);
        Discussion discussion = adapter.getData().get(position);
        DiscussionDetailActivity.show(getActivity(), discussion);
    }

    public void addNewDiscussion(Discussion discussion) {
        adapter.getData().add(0, discussion);
        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onFragmentShowed() {
        super.onFragmentShowed();
        if(mPresenter != null) mPresenter.prepareLoadData();
    }

}

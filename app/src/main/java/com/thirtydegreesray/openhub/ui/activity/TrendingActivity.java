

package com.thirtydegreesray.openhub.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerActivityComponent;
import com.thirtydegreesray.openhub.inject.module.ActivityModule;
import com.thirtydegreesray.openhub.mvp.contract.ITrendingContract;
import com.thirtydegreesray.openhub.mvp.model.TrendingLanguage;
import com.thirtydegreesray.openhub.mvp.model.filter.TrendingSince;
import com.thirtydegreesray.openhub.mvp.presenter.TrendingPresenter;
import com.thirtydegreesray.openhub.ui.activity.base.PagerActivity;
import com.thirtydegreesray.openhub.ui.adapter.base.FragmentPagerModel;
import com.thirtydegreesray.openhub.ui.fragment.RepositoriesFragment;

import java.util.ArrayList;

/**
 * Created by ThirtyDegreesRay on 2017/8/26 16:56:35
 */

public class TrendingActivity extends PagerActivity<TrendingPresenter>
        implements ITrendingContract.View {

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, TrendingActivity.class);
        context.startActivity(intent);
    }

    private final int SORT_LANGUAGE_REQUEST_CODE = 100;
    private TrendingLanguage selectedLanguage ;

    @Override
    protected void initActivity() {
        super.initActivity();
        setEndDrawerEnable(true);
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .activityModule(new ActivityModule(this))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    protected int getContentView() {
        return R.layout.activity_view_pager_with_drawer;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);

        com.google.android.material.appbar.AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams params =
                (androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        params.setMargins(params.leftMargin, 0, params.rightMargin, params.bottomMargin);
        appBarLayout.setLayoutParams(params);

        setToolbarScrollAble(true);
        setToolbarBackEnable();
        pagerAdapter.setPagerList(FragmentPagerModel.createTrendingPagerList(getActivity(), getFragments()));
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(pagerAdapter);
        showFirstPager();
        initLanguagesDrawer();
        updateTitle();

        // Set margins on tabs to increase spacing
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(getResources().getDimensionPixelSize(R.dimen.spacing_normal), 0, getResources().getDimensionPixelSize(R.dimen.spacing_normal), 0);
            tab.setLayoutParams(p);
        }
    }

    private void updateTitle(){
        setToolbarTitle(getString(R.string.trending_repos), selectedLanguage.getName());
    }

    @Override
    public int getPagerSize() {
        return 3;
    }

    @Override
    protected int getFragmentPosition(Fragment fragment) {
        if(fragment instanceof RepositoriesFragment){
            TrendingSince since = null;
            Object obj = fragment.getArguments().get("since");
            if (obj instanceof TrendingSince)
                since = (TrendingSince) obj;

            if(since == null){
                return -1;
            }else if(since.equals(TrendingSince.Daily)){
                return 0;
            } else if(since.equals(TrendingSince.Weekly)){
                return 1;
            } else if(since.equals(TrendingSince.Monthly)){
                return 2;
            } else {
                return -1;
            }
        }else
            return -1;
    }

    @Override
    protected void onNavItemSelected(@NonNull MenuItem item, boolean isStartDrawer) {
        super.onNavItemSelected(item, isStartDrawer);
        TrendingLanguage curSelectedLanguage = mPresenter.getLanguages().get(item.getOrder() - 1);
        if(!curSelectedLanguage.equals(selectedLanguage)){
            selectedLanguage = curSelectedLanguage;
            notifyLanguageUpdate();
            updateTitle();
        }
    }

    @Override
    protected int getEndDrawerToggleMenuItemId() {
        return R.id.nav_languages;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trending, menu);
        return true;
    }

    private void initLanguagesDrawer(){
        if(navViewEnd == null) return;
        updateLanguagesDrawer();
        View view = getLayoutInflater().inflate(R.layout.layout_trending_drawer_bottom, null);
        navViewEnd.addHeaderView(view);
        View editView = view.findViewById(R.id.language_edit_bn);
        editView.setOnClickListener(v -> {
            LanguagesEditorActivity.show(getActivity(),
                    LanguagesEditorActivity.LanguageEditorMode.Sort, SORT_LANGUAGE_REQUEST_CODE);
        });
    }

    private void updateLanguagesDrawer(){
        if(navViewEnd == null) return;
        updateEndDrawerContent(R.menu.drawer_menu_trending);
        ArrayList<TrendingLanguage> languages = mPresenter.getLanguagesFromLocal();
        Menu menu = navViewEnd.getMenu();
        for(TrendingLanguage language : languages){
            menu.add(R.id.group_languages, language.getOrder(), language.getOrder(), language.getName());
        }
        menu.setGroupCheckable(R.id.group_languages, true, true);
        if(languages.contains(selectedLanguage)){
            //maybe list size changed, and order changed too
            selectedLanguage = languages.get(languages.indexOf(selectedLanguage));
        } else {
            selectedLanguage = languages.get(0);
            notifyLanguageUpdate();
            updateTitle();
        }
        menu.findItem(selectedLanguage.getOrder()).setChecked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SORT_LANGUAGE_REQUEST_CODE){
            updateLanguagesDrawer();
        }
    }

    private void notifyLanguageUpdate(){
        for(FragmentPagerModel fragmentPagerModel : pagerAdapter.getPagerList()){
            if(fragmentPagerModel.getFragment() instanceof LanguageUpdateListener){
                ((LanguageUpdateListener)fragmentPagerModel.getFragment())
                        .onLanguageUpdate(selectedLanguage);
            }
        }
    }

    public interface LanguageUpdateListener{
        void onLanguageUpdate(TrendingLanguage language);
    }

}

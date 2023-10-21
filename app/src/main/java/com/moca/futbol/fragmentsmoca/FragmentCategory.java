package com.moca.futbol.fragmentsmoca;

import static com.moca.futbol.MocConf.REST_API_KEY;
import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_GRID_3_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_LIST_DEFAULT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.moca.futbol.adaptersmoca.MocCategory;
import com.moca.futbol.helpermoca.MocHelper;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.moca.futbol.R;
import com.moca.futbol.helpermoca.MocConstant;
import com.moca.futbol.activitymoca.DetCategoryMoc;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.Category;
import com.moca.futbol.adsmoca.AdsInterstitial;
import com.moca.futbol.rests.ApiInterface;
import com.moca.futbol.rests.RestAdapter;
import com.moca.futbol.helpermoca.ItemOffsetDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FragmentCategory extends Fragment {

    private int post_total = 0;
    private int failed_page = 0;
    private View root_view;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MocCategory MocCategory;
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ShimmerFrameLayout myLyt_shimmer;
    SharedPrefs mSharedPref;
    private Activity mActivity;

    private AdsInterstitial adsInterstitial;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_category, container, false);
        mSharedPref = new SharedPrefs(mActivity);
        adsInterstitial = new AdsInterstitial(mActivity);


        myLyt_shimmer = root_view.findViewById(R.id.shimmer_view_container);
        initShimmerLayout();

        swipeRefreshLayout = root_view.findViewById(R.id.swipe_refresh_layout_category);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        mRecyclerView = root_view.findViewById(R.id.recyclerViewCategory);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(mActivity, R.dimen.grid_space_category);
        if (mSharedPref.getCategoryViewType() == CATEGORY_LIST_DEFAULT) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        } else if (mSharedPref.getCategoryViewType() == CATEGORY_GRID_2_COLUMN) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
            mRecyclerView.addItemDecoration(itemDecoration);
        } else if (mSharedPref.getCategoryViewType() == CATEGORY_GRID_3_COLUMN) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL));
            mRecyclerView.addItemDecoration(itemDecoration);
        }

        //set data and list adapter
        MocCategory = new MocCategory(mActivity, mRecyclerView, new ArrayList<>());
        mRecyclerView.setAdapter(MocCategory);

        // on item list clicked
        MocCategory.setOnItemClickListener((v, obj, position) -> {

            adsInterstitial.showInterstitial(MocFirstAds.frag_category_inter, MocSecAds.frag_category_inter, () -> {

                Intent intent = new Intent(mActivity, DetCategoryMoc.class);
                intent.putExtra(EXTRA_OBJC, obj);
                startActivity(intent);
            });

        });

        // detect when scroll reach bottom
        MocCategory.setOnLoadMoreListener(this::setLoadMore);

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
                mCompositeDisposable.dispose();
                mCompositeDisposable = new CompositeDisposable();
            }
            MocCategory.resetListData();
            requestAction(1);
        });

        requestAction(1);

        return root_view;
    }

    public void setLoadMore(int current_page) {
        Log.d("page", "currentPage: " + current_page);
        // Assuming final total items equal to real post items plus the ad
        int totalItemBeforeAds = (MocCategory.getItemCount() - current_page);
        if (post_total > totalItemBeforeAds && current_page != 0) {
            int next_page = current_page + 1;
            requestAction(next_page);
        } else {
            MocCategory.setLoaded();
        }
    }
    private void displayApiResult(final List<Category> categories) {
        List<Category> reversedCategories = new ArrayList<>(categories);
        Collections.reverse(reversedCategories);
        MocCategory.insertDataWithNativeAd(reversedCategories);
        swipeTheProgress(false);
        if (categories.size() == 0) {
            showFreNoItemtoView(true);
        }
    }

    private void requestCategoriesApi() {
        ApiInterface apiInterface = RestAdapter.createAPI(mSharedPref.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getAllCategories(REST_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        displayApiResult(resp.categories);
                    } else {
                        onFailRequest();
                    }
                }));
    }

    private void onFailRequest() {
        swipeTheProgress(false);
        if (MocHelper.isInternetKoloConnected(mActivity)) {
            showTheFailedView(true, getString(R.string.failed_text));
        } else {
            showTheFailedView(true, getString(R.string.connect_internet_msg));
        }
    }

    private void requestAction(final int page_no) {
        showTheFailedView(false, "");
        showFreNoItemtoView(false);
        if (page_no == 1) {
            swipeTheProgress(true);
        } else {
            MocCategory.setLoading();
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    MocCategory.notifyItemInserted(MocCategory.getItemCount() - 1);
                }
            });
        }
        new Handler().postDelayed(this::requestCategoriesApi, MocConstant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeTheProgress(false);
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
        myLyt_shimmer.stopShimmer();
    }

    private void showTheFailedView(boolean flag, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_category);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            mRecyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }

        root_view.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showFreNoItemtoView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_category);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            mRecyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeTheProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            myLyt_shimmer.setVisibility(View.GONE);
            myLyt_shimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            myLyt_shimmer.setVisibility(View.VISIBLE);
            myLyt_shimmer.startShimmer();
        });
    }

    private void initShimmerLayout() {
        View lyt_shimmer_category_list = root_view.findViewById(R.id.lyt_shimmer_category_list);
        View lyt_shimmer_category_grid2 = root_view.findViewById(R.id.lyt_shimmer_category_grid2);
        View lyt_shimmer_category_grid3 = root_view.findViewById(R.id.lyt_shimmer_category_grid3);
        if (mSharedPref.getCategoryViewType() == CATEGORY_LIST_DEFAULT) {
            lyt_shimmer_category_list.setVisibility(View.VISIBLE);
            lyt_shimmer_category_grid2.setVisibility(View.GONE);
            lyt_shimmer_category_grid3.setVisibility(View.GONE);
        } else if (mSharedPref.getCategoryViewType() == CATEGORY_GRID_2_COLUMN) {
            lyt_shimmer_category_list.setVisibility(View.GONE);
            lyt_shimmer_category_grid2.setVisibility(View.VISIBLE);
            lyt_shimmer_category_grid3.setVisibility(View.GONE);
        } else if (mSharedPref.getCategoryViewType() == CATEGORY_GRID_3_COLUMN) {
            lyt_shimmer_category_list.setVisibility(View.GONE);
            lyt_shimmer_category_grid2.setVisibility(View.GONE);
            lyt_shimmer_category_grid3.setVisibility(View.VISIBLE);
        }
    }

}

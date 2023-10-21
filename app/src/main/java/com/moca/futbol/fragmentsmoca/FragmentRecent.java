package com.moca.futbol.fragmentsmoca;


import static com.moca.futbol.MocConf.REST_API_KEY;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_3_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_LIST_DEFAULT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.moca.futbol.helpermoca.MocHelper;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.moca.futbol.MocConf;
import com.moca.futbol.R;
import com.moca.futbol.activitymoca.DetailChannelMoc;
import com.moca.futbol.adaptersmoca.MocRecent;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.Channel;
import com.moca.futbol.adsmoca.AdsInterstitial;
import com.moca.futbol.rests.ApiInterface;
import com.moca.futbol.rests.RestAdapter;
import com.moca.futbol.helpermoca.MocConstant;
import com.moca.futbol.helpermoca.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FragmentRecent extends Fragment {

    View root_view;
    LinearLayout parent_view;
    private RecyclerView mRecyclerView;
    private MocRecent MocRecent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int post_total = 0;
    private int failed_page = 0;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ShimmerFrameLayout mLyt_shimmer;
    private SharedPrefs mSharedPref;
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
        root_view = inflater.inflate(R.layout.fragment_recent, container, false);
        mSharedPref = new SharedPrefs(mActivity);
        parent_view = root_view.findViewById(R.id.parent_view);
        mLyt_shimmer = root_view.findViewById(R.id.shimmer_view_container);

        initShimmerLayout();

        swipeRefreshLayout = root_view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRecyclerView = root_view.findViewById(R.id.recyclerView);

        adsInterstitial = new AdsInterstitial(mActivity);

        if (mSharedPref.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        } else if (mSharedPref.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else if (mSharedPref.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        }

        int padding = getResources().getDimensionPixelOffset(R.dimen.recycler_view_padding);
        if (mSharedPref.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            mRecyclerView.setPadding(0, padding, 0, padding);
        } else {
            mRecyclerView.setPadding(padding, padding, padding, padding);
        }

        //set data and list adapter
        MocRecent = new MocRecent(mActivity, mRecyclerView, new ArrayList<>());
        mRecyclerView.setAdapter(MocRecent);

        // on item list clicked
        MocRecent.setOnItemClickListener((v, obj, position) -> {
            adsInterstitial.showInterstitial(MocFirstAds.frag_recent_inter, MocSecAds.frag_recent_inter, () -> {
                Intent intent = new Intent(mActivity, DetailChannelMoc.class);
                intent.putExtra(MocConstant.EXTRA_OBJC, obj);
                startActivity(intent);
            });
        });

        // detect when scroll reach bottom
        MocRecent.setOnLoadMoreListener(this::setLoadMore);

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
                mCompositeDisposable.dispose();
                mCompositeDisposable = new CompositeDisposable();
            }
            MocRecent.resetListData();
            requestAction(1);
        });

        requestAction(1);

        return root_view;
    }

    public void setLoadMore(int current_page) {
        Log.d("page", "currentPage: " + current_page);
        // Assuming final total items equal to real post items plus the ad
        int totalItemBeforeAds = (MocRecent.getItemCount() - current_page);
        if (post_total > totalItemBeforeAds && current_page != 0) {
            int next_page = current_page + 1;
            requestAction(next_page);
        } else {
            MocRecent.setLoaded();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private void displayApiResult(final List<Channel> channels) {
        MocRecent.insertDataWithNativeAd(channels);
        swipeProgress(false);
        if (channels.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(mSharedPref.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getRecentChannel(page_no, MocConf.LOAD_MORE, REST_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        post_total = resp.count_total;
                        displayApiResult(resp.posts);
                        addFavorite();
                    } else {
                        onFailRequest(page_no);
                    }
                }));
    }

    public void addFavorite() {
        MocRecent.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popupMenu = new PopupMenu(mActivity);
            popupMenu.onClickItemOverflow(v, obj, parent_view);
        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        MocRecent.setLoaded();
        swipeProgress(false);
        if (MocHelper.isInternetKoloConnected(mActivity)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.connect_internet_msg));
        }
    }
    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            MocRecent.setLoading();
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    MocRecent.notifyItemInserted(MocRecent.getItemCount() - 1);
                }
            });
        }
        new Handler().postDelayed(() -> requestListPostApi(page_no), MocConstant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
        mLyt_shimmer.stopShimmer();
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = (View) root_view.findViewById(R.id.lyt_failed_home);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            mRecyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_home);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            mRecyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            mLyt_shimmer.setVisibility(View.GONE);
            mLyt_shimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            mLyt_shimmer.setVisibility(View.VISIBLE);
            mLyt_shimmer.startShimmer();
        });
    }

    private void initShimmerLayout() {
        View lyt_shimmer_channel_list = root_view.findViewById(R.id.lyt_shimmer_channel_list);
        View lyt_shimmer_channel_grid2 = root_view.findViewById(R.id.lyt_shimmer_channel_grid2);
        View lyt_shimmer_channel_grid3 = root_view.findViewById(R.id.lyt_shimmer_channel_grid3);
        if (mSharedPref.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            lyt_shimmer_channel_list.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (mSharedPref.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (mSharedPref.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.VISIBLE);
        }
    }

}

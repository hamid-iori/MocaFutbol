package com.moca.futbol.activitymoca;

import static com.moca.futbol.MocConf.REST_API_KEY;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_3_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_LIST_DEFAULT;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.moca.futbol.helpermoca.MocHelper;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.moca.futbol.MocConf;
import com.moca.futbol.R;
import com.moca.futbol.adaptersmoca.MocRecent;
import com.moca.futbol.helpermoca.MocConstant;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.Category;
import com.moca.futbol.modelmoca.Channel;
import com.moca.futbol.adsmoca.AdsBanner;
import com.moca.futbol.adsmoca.AdsInterstitial;
import com.moca.futbol.rests.ApiInterface;
import com.moca.futbol.rests.RestAdapter;
import com.moca.futbol.helpermoca.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetCategoryMoc extends AppCompatActivity {

    private int post_total = 0;
    private int failed_page = 0;
    private Category category_moc;
    private ShimmerFrameLayout lyt_shimmer_moc;
    SharedPrefs sharedPrefs_moc;

    private AdsInterstitial adsInterstitial_moc;
    private RecyclerView myRecyclerView_moc;
    private MocRecent MocRecent;
    private SwipeRefreshLayout swipeRefresh_Layout_moc;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_category_details);

        sharedPrefs_moc = new SharedPrefs(this);
        adsInterstitial_moc = new AdsInterstitial(this);
        AdsBanner adsBanner = new AdsBanner(DetCategoryMoc.this);
        adsBanner.loadActivityBanner(MocFirstAds.category_banner, MocSecAds.category_banner);

        category_moc = (Category) getIntent().getSerializableExtra(MocConstant.EXTRA_OBJC);

        lyt_shimmer_moc = findViewById(R.id.shimmer_view_container);
        initShimmerLayout();

        swipeRefresh_Layout_moc = findViewById(R.id.swipe_refresh_layout);
        swipeRefresh_Layout_moc.setColorSchemeResources(R.color.colorPrimary);

        myRecyclerView_moc = findViewById(R.id.recyclerView);

        if (sharedPrefs_moc.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            myRecyclerView_moc.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs_moc.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            myRecyclerView_moc.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs_moc.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            myRecyclerView_moc.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        }

        int padding = getResources().getDimensionPixelOffset(R.dimen.recycler_view_padding);
        if (sharedPrefs_moc.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            myRecyclerView_moc.setPadding(0, padding, 0, padding);
        } else {
            myRecyclerView_moc.setPadding(padding, padding, padding, padding);
        }

        //set data and list adapter
        MocRecent = new MocRecent(this, myRecyclerView_moc, new ArrayList<>());
        myRecyclerView_moc.setAdapter(MocRecent);

        // on item list clicked
        MocRecent.setOnItemClickListener((v, obj, position) -> {
            adsInterstitial_moc.showInterstitial(MocFirstAds.category_inter, MocSecAds.category_inter, () -> {
                Intent intent = new Intent(getApplicationContext(), DetailChannelMoc.class);
                intent.putExtra(MocConstant.EXTRA_OBJC, obj);
                startActivity(intent);
            });
        });

        // detect when scroll reach bottom
        MocRecent.setOnLoadMoreListener(this::setLoadMoreMoc);

        // on swipe list
        swipeRefresh_Layout_moc.setOnRefreshListener(() -> {
            if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
                mCompositeDisposable.dispose();
                mCompositeDisposable = new CompositeDisposable();
            }
            MocRecent.resetListData();
            requestActionMoc(1);
        });

        requestActionMoc(1);

        setupToolbarMoc();

    }

    public void setLoadMoreMoc(int current_page) {
        Log.d("page", "currentPage: " + current_page);
        // Assuming final total items equal to real post items plus the ad
        int totalItemBeforeAds = (MocRecent.getItemCount() - current_page);
        if (post_total > totalItemBeforeAds && current_page != 0) {
            int next_page = current_page + 1;
            requestActionMoc(next_page);
        } else {
            MocRecent.setLoaded();
        }
    }

    public void setupToolbarMoc() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(category_moc.category_name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.search) {
            Intent intent = new Intent(getApplicationContext(), SearchMoc.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void displayApiResultMoc(final List<Channel> channels) {
        MocRecent.insertDataWithNativeAd(channels);
        swipeProgressMoc(false);
        if (channels.size() == 0) {
            showNoItemViewMoc(true);
        }
    }

    private void requestPostApiMoc(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPrefs_moc.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getChannelByCategory(category_moc.cid, page_no, MocConf.LOAD_MORE, REST_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        post_total = resp.count_total;
                        displayApiResultMoc(resp.posts);
                        addFavorite();
                    } else {
                        onFailRequestMoc(page_no);
                    }
                }));
    }

    private void onFailRequestMoc(int page_no) {
        failed_page = page_no;
        MocRecent.setLoaded();
        swipeProgressMoc(false);
        if (MocHelper.isInternetKoloConnected(getApplicationContext())) {
            showFailedViewMoc(true, getString(R.string.failed_text));
        } else {
            showFailedViewMoc(true, getString(R.string.connect_internet_msg));
        }
    }

    private void requestActionMoc(final int page_no) {
        showFailedViewMoc(false, "");
        showNoItemViewMoc(false);
        if (page_no == 1) {
            swipeProgressMoc(true);
        } else {
            MocRecent.setLoading();
        }
        new Handler().postDelayed(() -> requestPostApiMoc(page_no), MocConstant.DELAY_TIME);
    }

    private void showFailedViewMoc(boolean show, String message) {
        View view = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            myRecyclerView_moc.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            myRecyclerView_moc.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view1 -> requestActionMoc(failed_page));
    }

    private void showNoItemViewMoc(boolean show) {
        View view = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            myRecyclerView_moc.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        } else {
            myRecyclerView_moc.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
    }

    private void swipeProgressMoc(final boolean show) {
        if (!show) {
            swipeRefresh_Layout_moc.setRefreshing(show);
            lyt_shimmer_moc.setVisibility(View.GONE);
            lyt_shimmer_moc.stopShimmer();
            return;
        }
        swipeRefresh_Layout_moc.post(() -> {
            swipeRefresh_Layout_moc.setRefreshing(show);
            lyt_shimmer_moc.setVisibility(View.VISIBLE);
            lyt_shimmer_moc.startShimmer();
        });
    }

    private void initShimmerLayout() {
        View lyt_shimmer_channel_list = findViewById(R.id.lyt_shimmer_channel_list);
        View lyt_shimmer_channel_grid2 = findViewById(R.id.lyt_shimmer_channel_grid2);
        View lyt_shimmer_channel_grid3 = findViewById(R.id.lyt_shimmer_channel_grid3);
        if (sharedPrefs_moc.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            lyt_shimmer_channel_list.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs_moc.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs_moc.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.VISIBLE);
        }
    }

    public void addFavorite() {
        MocRecent.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popupMenu = new PopupMenu(this);
            popupMenu.onClickItemOverflow(v, obj, swipeRefresh_Layout_moc);
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgressMoc(false);
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
        lyt_shimmer_moc.stopShimmer();
    }

}
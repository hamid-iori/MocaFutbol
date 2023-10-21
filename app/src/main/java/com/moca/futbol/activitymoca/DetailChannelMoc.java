package com.moca.futbol.activitymoca;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.moca.futbol.helpermoca.MocHelper;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.moca.futbol.adsmoca.AdsBanner;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.moca.futbol.MocConf;
import com.moca.futbol.R;
import com.moca.futbol.adaptersmoca.MocSuggestion;
import com.moca.futbol.callbackmoca.CallbackChannelDetail;
import com.moca.futbol.dataprefsmoca.appdata.AppDatabase;
import com.moca.futbol.dataprefsmoca.appdata.ChannelEntity;
import com.moca.futbol.dataprefsmoca.appdata.DAO;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.Channel;
import com.moca.futbol.adsmoca.AdsInterstitial;
import com.moca.futbol.adsmoca.AdsNative;
import com.moca.futbol.rests.ApiInterface;
import com.moca.futbol.rests.RestAdapter;
import com.moca.futbol.helpermoca.AppBarLayoutBehavior;
import com.moca.futbol.helpermoca.MocConstant;
import com.moca.futbol.helpermoca.PopupMenu;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DetailChannelMoc extends AppCompatActivity {

    private DAO dao;
    boolean flag_read_later_moc;
    CoordinatorLayout parent_view_moc;
    private ShimmerFrameLayout lyt_shimmer_moc;
    RelativeLayout lyt_suggested_moc;
    private SwipeRefreshLayout swipe_refresh_moc;
    SharedPrefs sharedPrefs_moc;
    ImageButton botona_favorite_moc, botona_share_moc;
    private AdsInterstitial adsInterstitial_moc;
    private LinearLayout lyt_main_content_moc;
    private Channel channel_moc;
    ImageView my_channel_image_moc;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    TextView my_channel_name_moc, my_channel_category_moc, my_title_toolbar_moc;
    WebView channel_description_moc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_channel_details);
        sharedPrefs_moc = new SharedPrefs(this);

        adsInterstitial_moc = new AdsInterstitial(DetailChannelMoc.this);
        AdsBanner adsBanner2 = new AdsBanner(this);
        adsBanner2.loadActivityBanner( MocFirstAds.channel_banner, MocSecAds.channel_banner);


        if (MocFirstAds.meta_native) {
            AdsNative adsNative = new AdsNative(this);
            adsNative.loadNativeAd(MocFirstAds.channel_native, MocSecAds.channel_native);
        } else if (MocFirstAds.ad_state) {
            if (MocFirstAds.ad_type.equals("meta")) {
                AdsBanner adsBanner = new AdsBanner(this);
                adsBanner.loadMrEC(this, MocFirstAds.channel_rect, MocSecAds.channel_rect);
            }
        }

        dao = AppDatabase.getDatabase(this).get();

        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipe_refresh_moc = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_moc.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh_moc.setRefreshing(false);


        lyt_main_content_moc = findViewById(R.id.lyt_main_content);
        lyt_shimmer_moc = findViewById(R.id.shimmer_view_container);
        parent_view_moc = findViewById(R.id.parent_view);

        my_title_toolbar_moc = findViewById(R.id.title_toolbar);
        botona_favorite_moc = findViewById(R.id.btn_favorite);
        botona_share_moc = findViewById(R.id.btn_share);
        my_channel_image_moc = findViewById(R.id.my_channel_image);
        my_channel_name_moc = findViewById(R.id.my_channel_name);
        my_channel_category_moc = findViewById(R.id.my_channel_category);
        channel_description_moc = findViewById(R.id.channel_description);

        lyt_suggested_moc = findViewById(R.id.lyt_suggested);

        channel_moc = (Channel) getIntent().getSerializableExtra(MocConstant.EXTRA_OBJC);

        requestActionMoc();

        swipe_refresh_moc.setOnRefreshListener(() -> {
            if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
                mCompositeDisposable.dispose();
                mCompositeDisposable = new CompositeDisposable();
            }
            lyt_shimmer_moc.setVisibility(View.VISIBLE);
            lyt_shimmer_moc.startShimmer();
            lyt_main_content_moc.setVisibility(View.GONE);
            requestActionMoc();
        });

        initToolbar();
        refreshReadLaterMenuMoc();

    }
    //end OnCreate

    private void requestActionMoc() {
        showMyFailedViewMoc(false, "");
        swipeProgressMoc(true);
        new Handler().postDelayed(this::requestPostDataMoc, 200);
    }

    private void requestPostDataMoc() {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPrefs_moc.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getChannelDetail(channel_moc.channel_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        displayAllMyDataMoc(resp);
                        swipeProgressMoc(false);
                        lyt_main_content_moc.setVisibility(View.VISIBLE);
                    } else {
                        onFailRequestMoc();
                    }
                }));
    }

    private void onFailRequestMoc() {
        swipeProgressMoc(false);
        lyt_main_content_moc.setVisibility(View.GONE);
        if (MocHelper.isInternetKoloConnected(DetailChannelMoc.this)) {
            showMyFailedViewMoc(true, getString(R.string.failed_text));
        } else {
            showMyFailedViewMoc(true, getString(R.string.failed_text));
        }
    }

    private void showMyFailedViewMoc(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestActionMoc());
    }

    private void swipeProgressMoc(final boolean show) {
        if (!show) {
            swipe_refresh_moc.setRefreshing(show);
            lyt_shimmer_moc.setVisibility(View.GONE);
            lyt_shimmer_moc.stopShimmer();
            lyt_main_content_moc.setVisibility(View.VISIBLE);
            return;
        }
        lyt_main_content_moc.setVisibility(View.GONE);
    }

    private void displayAllMyDataMoc(CallbackChannelDetail resp) {
        displayDataMoc(resp.post);
        displaySuggestedMoc(resp.suggested);
    }

    public void displayDataMoc(final Channel channel) {

        my_channel_name_moc.setText(channel.channel_name);

        my_channel_category_moc.setText(channel.category_name);
        if (MocConf.ENABLE_CHANNEL_LIST_CATEGORY_NAME) {
            my_channel_category_moc.setVisibility(View.VISIBLE);
        } else {
            my_channel_category_moc.setVisibility(View.GONE);
        }

        Glide.with(this)
                .load(sharedPrefs_moc.getBaseUrl() + "/upload/" + channel.channel_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .into(my_channel_image_moc);


        my_channel_image_moc.setOnClickListener(view -> {

            //  todo player ads to verify
            if (MocFirstAds.isPlayerAds) {
                adsInterstitial_moc.showInterstitial(MocFirstAds.channel_inter, MocSecAds.channel_inter, () -> {
                    MocHelper.startLecteur(this, parent_view_moc, channel);
                });

            } else {
                MocHelper.startLecteur(this, parent_view_moc, channel);
            }
        });

        MocHelper.displayContent(this, channel_description_moc, channel.channel_description);

        botona_share_moc.setOnClickListener(view -> MocHelper.share(this, channel.channel_name));

        addToFavorite();

        new Handler().postDelayed(() -> lyt_suggested_moc.setVisibility(View.VISIBLE), 1000);

    }

    private void displaySuggestedMoc(List<Channel> list) {

        RecyclerView recyclerView = findViewById(R.id.recycler_view_suggested);
        recyclerView.setLayoutManager(new LinearLayoutManager(DetailChannelMoc.this));
        MocSuggestion MocSuggestion = new MocSuggestion(DetailChannelMoc.this, recyclerView, list);
        recyclerView.setAdapter(MocSuggestion);
        recyclerView.setNestedScrollingEnabled(false);
        MocSuggestion.setOnItemClickListener((view, obj, position) -> {
            if (MocFirstAds.ad_state) {

                adsInterstitial_moc.showInterstitial(MocFirstAds.channel_inter, MocSecAds.channel_inter, () -> {
                    Intent intent = new Intent(getApplicationContext(), DetailChannelMoc.class);
                    intent.putExtra(MocConstant.EXTRA_OBJC, obj);
                    startActivity(intent);

                });
            } else {
                Intent intent = new Intent(getApplicationContext(), DetailChannelMoc.class);
                intent.putExtra(MocConstant.EXTRA_OBJC, obj);
                startActivity(intent);

            }

        });

        MocSuggestion.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popupMenu = new PopupMenu(this);
            popupMenu.onClickItemOverflow(v, obj, parent_view_moc);
        });

        TextView txt_suggested = findViewById(R.id.txt_suggested);
        if (list.size() > 0) {
            txt_suggested.setText(getResources().getString(R.string.txt_suggested));
        } else {
            txt_suggested.setText("");
        }

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }

        my_title_toolbar_moc.setText(channel_moc.category_name);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void addToFavorite() {
        botona_favorite_moc.setOnClickListener(view -> {
            String str;
            if (flag_read_later_moc) {
                dao.deleteChannel(channel_moc.channel_id);
                str = getString(R.string.favorite_removed);
            } else {
                dao.insertChannel(ChannelEntity.entity(channel_moc));
                str = getString(R.string.favorite_added);
            }
            Snackbar.make(parent_view_moc, str, Snackbar.LENGTH_SHORT).show();
            refreshReadLaterMenuMoc();
        });
    }

    //todo check icons difference
    private void refreshReadLaterMenuMoc() {
        flag_read_later_moc = dao.getChannel(channel_moc.channel_id) != null;
        if (flag_read_later_moc) {
            botona_favorite_moc.setImageResource(R.drawable.moca_heart_white);
        } else {
            botona_favorite_moc.setImageResource(R.drawable.moca_heart_white);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        lyt_shimmer_moc.stopShimmer();
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
    }

}
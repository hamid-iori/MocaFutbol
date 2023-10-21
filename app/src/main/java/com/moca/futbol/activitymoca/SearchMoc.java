package com.moca.futbol.activitymoca;


import static com.moca.futbol.MocConf.REST_API_KEY;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_3_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_LIST_DEFAULT;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.moca.futbol.helpermoca.MocHelper;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.moca.futbol.helpermoca.MocConstant;
import com.google.android.material.snackbar.Snackbar;
import com.moca.futbol.R;
import com.moca.futbol.adaptersmoca.MocRecent;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.adsmoca.AdsInterstitial;
import com.moca.futbol.rests.ApiInterface;
import com.moca.futbol.rests.RestAdapter;
import com.moca.futbol.helpermoca.PopupMenu;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchMoc extends AppCompatActivity {

    private EditText edit_txt_search;
    private RecyclerView recyclerView;
    private MocRecent MocRecent;
    private ImageButton image_btn_clear;
    RelativeLayout parent_view;
    Snackbar snackbar;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ShimmerFrameLayout lyt_shimmer;
    SharedPrefs sharedPrefs;
    private AdsInterstitial adsInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_search);

        sharedPrefs = new SharedPrefs(this);

        adsInterstitial = new AdsInterstitial(SearchMoc.this);


        parent_view = findViewById(R.id.parent_view);
        edit_txt_search = findViewById(R.id.et_search);
        image_btn_clear = findViewById(R.id.bt_clear);
        image_btn_clear.setVisibility(View.GONE);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        initShimmerLayout();
        swipeProgress(false);
        recyclerView = findViewById(R.id.recyclerView);

        if (sharedPrefs.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        }

        int padding = getResources().getDimensionPixelOffset(R.dimen.recycler_view_padding);
        if (sharedPrefs.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            recyclerView.setPadding(0, padding, 0, padding);
        } else {
            recyclerView.setPadding(padding, padding, padding, padding);
        }

        edit_txt_search.addTextChangedListener(textWatcher);

        //set data and list adapter
        MocRecent = new MocRecent(this, recyclerView, new ArrayList<>());
        recyclerView.setAdapter(MocRecent);

        image_btn_clear.setOnClickListener(view -> edit_txt_search.setText(""));

        edit_txt_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                searchAction();
                return true;
            }
            return false;
        });

        MocRecent.setOnItemClickListener((v, obj, position) -> {

            adsInterstitial.showInterstitial(MocFirstAds.search_inter, MocSecAds.search_inter, () -> {

                Intent intent = new Intent(getApplicationContext(), DetailChannelMoc.class);
                intent.putExtra(MocConstant.EXTRA_OBJC, obj);
                startActivity(intent);

            });
        });

        setupToolbar();


    }
    //end OnCreate

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                image_btn_clear.setVisibility(View.GONE);
            } else {
                image_btn_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


    private void requestSearchApi(final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPrefs.getBaseUrl());
        mCompositeDisposable.add(apiInterface.getSearchChannel(query, MocConstant.MAX_SEARCH_RESULT, REST_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((resp, throwable) -> {
                    if (resp != null && resp.status.equals("ok")) {
                        MocRecent.insertData(resp.posts);
                        addFavorite();
                        if (resp.posts.size() == 0) showNotFoundView(true);
                    } else {
                        onFailRequest();
                    }
                    swipeProgress(false);
                }));
    }

    private void onFailRequest() {
        if (MocHelper.isInternetKoloConnected(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.connect_internet_msg));
        }
    }

    private void searchAction() {
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = edit_txt_search.getText().toString().trim();
        if (!query.equals("")) {
            MocRecent.resetListData();
            swipeProgress(true);
            new Handler().postDelayed(() -> {

                requestSearchApi(query);

            }, MocConstant.DELAY_TIME);
        } else {
            snackbar = Snackbar.make(parent_view, getResources().getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchAction());
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_search_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
        } else {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        }
    }

    private void initShimmerLayout() {
        View lyt_shimmer_channel_list = findViewById(R.id.lyt_shimmer_channel_list);
        View lyt_shimmer_channel_grid2 = findViewById(R.id.lyt_shimmer_channel_grid2);
        View lyt_shimmer_channel_grid3 = findViewById(R.id.lyt_shimmer_channel_grid3);
        if (sharedPrefs.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            lyt_shimmer_channel_list.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.VISIBLE);
            lyt_shimmer_channel_grid3.setVisibility(View.GONE);
        } else if (sharedPrefs.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            lyt_shimmer_channel_list.setVisibility(View.GONE);
            lyt_shimmer_channel_grid2.setVisibility(View.GONE);
            lyt_shimmer_channel_grid3.setVisibility(View.VISIBLE);
        }
    }

    public void addFavorite() {
        MocRecent.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popupMenu = new PopupMenu(this);
            popupMenu.onClickItemOverflow(v, obj, parent_view);
        });
    }


    @Override
    public void onBackPressed() {
        if (edit_txt_search.length() > 0) {
            edit_txt_search.setText("");
        } else {
            super.onBackPressed();
        }
    }

}
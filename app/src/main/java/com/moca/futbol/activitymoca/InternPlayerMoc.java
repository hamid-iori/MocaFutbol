package com.moca.futbol.activitymoca;


import static com.moca.futbol.MocConf.PRESS_BACK_TWICE_TO_CLOSE_PLAYER;
import static com.moca.futbol.helpermoca.MocConstant.PLAYER_MODE_LANDSCAPE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.text.CueGroup;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.snackbar.Snackbar;
import com.moca.futbol.MocConf;
import com.moca.futbol.R;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;

public class InternPlayerMoc extends AppCompatActivity {

    private DataSource.Factory dataSourceFactory;
    private ProgressBar progressBar;
    private boolean fullscreen = false;
    private ImageView fullscreenButton_Ibo;
    private long exitTime = 0;
    private RelativeLayout parent_view;
    private SharedPrefs sharedPrefs;
    private String video_url_Ibo;
    private StyledPlayerView styledPlayerView;
    private ExoPlayer myExoPlayer;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.pobr_player_stream);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            this.getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            this.getWindow().getDecorView().setSystemUiVisibility(0);
        }

        Intent intent = getIntent();
        video_url_Ibo = intent.getStringExtra("url");
        String user_agent = intent.getStringExtra("user_agent");

        sharedPrefs = new SharedPrefs(this);

        parent_view = findViewById(R.id.parent_view);
        progressBar = findViewById(R.id.progressBar);

        if (user_agent.equals("default")) {
            HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true).setUserAgent(getTheUserAgent());
            dataSourceFactory = new DefaultDataSource.Factory(getApplicationContext(), httpDataSourceFactory);

        } else {
            HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true).setUserAgent(user_agent);
            dataSourceFactory = new DefaultDataSource.Factory(getApplicationContext(), httpDataSourceFactory);

        }

        LoadControl loadControl = new DefaultLoadControl();

        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, trackSelectionFactory);

        myExoPlayer = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build();

        styledPlayerView = findViewById(R.id.player_view);
        styledPlayerView.setPlayer(myExoPlayer);
        styledPlayerView.setUseController(true);
        styledPlayerView.requestFocus();

        myPlayerOrientation();

        Uri uri = Uri.parse(video_url_Ibo);

        MediaSource mediaSource = buildMediaSource(uri);
        myExoPlayer.setMediaSource(mediaSource);
        myExoPlayer.prepare();
        myExoPlayer.setPlayWhenReady(true);

        myExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onCues(@NonNull CueGroup cueGroup) {

            }

            @Override
            public void onTimelineChanged(@NonNull Timeline timeline, int reason) {

            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == PlaybackStateCompat.STATE_PLAYING) {
                    progressBar.setVisibility(View.GONE);
                }

                if (MocConf.ENABLE_LOOPING_MODE) {
                    switch (state) {
                        case Player.STATE_READY:
                            progressBar.setVisibility(View.GONE);
                            myExoPlayer.setPlayWhenReady(true);
                            break;
                        case Player.STATE_ENDED:
                            myExoPlayer.seekTo(0);
                            break;
                        case Player.STATE_BUFFERING:
                            progressBar.setVisibility(View.VISIBLE);
                            myExoPlayer.seekTo(0);
                            //exoPlayer.setPlayWhenReady(true);
                            break;
                        case Player.STATE_IDLE:
                            break;
                    }
                }
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {

            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                myExoPlayer.stop();
                errorDialog();
                Log.d("TAG", "onPlayerError " + error);
            }

            @Override
            public void onPlayerErrorChanged(@Nullable PlaybackException error) {
                Log.d("TAG", "onPlayerErrorChanged " + error);
            }

        });

    }

    private void myPlayerOrientation() {
        fullscreenButton_Ibo = styledPlayerView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenButton_Ibo.setOnClickListener(view -> {
            if (fullscreen) {
                setPortrait();
            } else {
                setLandscape();
            }
        });

        if (sharedPrefs.getPlayerMode() == PLAYER_MODE_LANDSCAPE) {
            setLandscape();
        }

    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void setPortrait() {
        fullscreenButton_Ibo.setImageDrawable(ContextCompat.getDrawable(InternPlayerMoc.this, R.drawable.pob_fullscreen_open));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) styledPlayerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        styledPlayerView.setLayoutParams(params);
        fullscreen = false;
    }



    @SuppressLint("SwitchIntDef")
    private MediaSource buildMediaSource(Uri uri) {
        MediaItem mMediaItem = MediaItem.fromUri(Uri.parse(String.valueOf(uri)));
        int type = TextUtils.isEmpty(null) ? Util.inferContentType(uri) : Util.inferContentTypeForExtension("." + null);
        switch (type) {
            case C.CONTENT_TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mMediaItem);
            case C.CONTENT_TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(mMediaItem);
            case C.CONTENT_TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory())
                        .createMediaSource(mMediaItem);
            case C.CONTENT_TYPE_RTSP:
                return new RtspMediaSource.Factory()
                        .createMediaSource(MediaItem.fromUri(uri));


            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private String getTheUserAgent() {

        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version"));
        result.append(" (Linux; U; Android ");

        String version = Build.VERSION.RELEASE;
        result.append(version.length() > 0 ? version : "1.0");

        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }

        String id = Build.ID;

        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }

        result.append(")");
        return result.toString();
    }


    private void setLandscape() {
        fullscreenButton_Ibo.setImageDrawable(ContextCompat.getDrawable(InternPlayerMoc.this, R.drawable.pob_fullscreen_close));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) styledPlayerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        styledPlayerView.setLayoutParams(params);
        fullscreen = true;
    }

    public void closeMyPlayer() {
        if (PRESS_BACK_TWICE_TO_CLOSE_PLAYER) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Snackbar.make(parent_view, getString(R.string.press_again_to_close_player), Snackbar.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                myExoPlayer.stop();
            }
        } else {
            finish();
            myExoPlayer.stop();
        }
    }

    public void errorDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.pobr_whop))
                .setCancelable(false)
                .setMessage(getString(R.string.msg_failed))
                .setPositiveButton(getString(R.string.option_retry), (dialog, which) -> retryToLoad())
                .setNegativeButton(getString(R.string.option_no), (dialogInterface, i) -> finish())
                .show();
    }

    public void retryToLoad() {
        Uri uri = Uri.parse(video_url_Ibo);
        MediaSource mediaSource = buildMediaSource(uri);
        myExoPlayer.setMediaSource(mediaSource);
        myExoPlayer.prepare();
        myExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        closeMyPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myExoPlayer.setPlayWhenReady(false);
        myExoPlayer.getPlaybackState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myExoPlayer.setPlayWhenReady(true);
        myExoPlayer.getPlaybackState();
    }


}
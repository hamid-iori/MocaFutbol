package com.moca.futbol;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.BuildConfig;
import androidx.multidex.MultiDex;

import com.moca.futbol.adopenmoca.AdNetwork;
import com.moca.futbol.adopenmoca.AppOpenAd;

import com.moca.futbol.adopenmoca.AppOpenAdMob;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryPerformance;

import com.onesignal.OneSignal;

public class MyApplication extends Application {

    Activity currentActivity;
    public String adMobAppOpenAdUnitId;
    private AppOpenAdMob appOpenAdMob;
    AdNetwork.Initialize adNetwork;
    AppOpenAd.Builder appOpenAdBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
        appOpenAdMob = new AppOpenAdMob();

        new FlurryAgent.Builder()
                .withDataSaleOptOut(false)
                .withCaptureUncaughtExceptions(true)
                .withIncludeBackgroundSessionsInMetrics(true)
                .withLogLevel(Log.VERBOSE)
                .withPerformanceMetrics(FlurryPerformance.ALL)
                .withLogEnabled(true)
                .withReportLocation(true)
                .build(this, getString(R.string.my_flurry_id));

        // OneSignal Initialization
        OneSignal.initWithContext(this,getString(R.string.my_onesignal_id));

        adMobAppOpenAdUnitId = MocFirstAds.admob_open_id;

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    LifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);
            if (MocFirstAds.isAppOpen) {
                if (MocFirstAds.ad_state) {
                    if ("admob".equals(MocFirstAds.ad_type)) {
                        if (!MocFirstAds.admob_open_id.equals("0")) {

                            initAds();
                            appOpenAdMob.showAdIfAvailable(currentActivity, MocFirstAds.admob_open_id);

                        }
                    }
                }
            }
        }
    };
    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
                if (MocFirstAds.ad_state) {
                    if ("admob".equals(MocFirstAds.ad_type)) {
                        if (!adMobAppOpenAdUnitId.equals("0")) {
                            if (!appOpenAdMob.isShowingAd) {
                                currentActivity = activity;
                            }
                        }

                }
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }
        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }
        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }
        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }
        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    };

    private void loadOpenAds() {
        if (MocFirstAds.ad_state) {
            if ("admob".equals(MocFirstAds.ad_type) || "admob".equals(MocFirstAds.second_ads)) {

                appOpenAdBuilder = new AppOpenAd.Builder(currentActivity)
                        .setAdStatus(MocFirstAds.ad_state)
                        .setAdNetwork(MocFirstAds.ad_type)
                        .setBackupAdNetwork(MocFirstAds.second_ads)
                        .setAdMobAppOpenId(MocFirstAds.admob_open_id)
                        .build();

            }
        }
    }
    private void initAds() {
        adNetwork = new AdNetwork.Initialize(currentActivity)
                .setAdStatus(MocFirstAds.ad_state)
                .setAdNetwork(MocFirstAds.ad_type)
                .setBackupAdNetwork(MocFirstAds.second_ads)
                .setDebug(BuildConfig.DEBUG)
                .build();
        loadOpenAds();
    }
}

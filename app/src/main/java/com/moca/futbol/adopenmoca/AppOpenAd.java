package com.moca.futbol.adopenmoca;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;


@SuppressLint("StaticFieldLeak")
public class AppOpenAd {
    public static com.google.android.gms.ads.appopen.AppOpenAd appOpenAd = null;
    public static boolean isAppOpenAdLoaded = false;

    public static class Builder {
        private static final String TAG = "AppOpenAd";
        private final Activity activity;
        private boolean adStatus ;
        private String adNetwork = "";
        private String backupAdNetwork = "";
        private String adMobAppOpenId = MocFirstAds.admob_open_id ;


        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder build() {
            loadAppOpenAd();
            return this;
        }

        public Builder build(OnShowAdCompleteListener onShowAdCompleteListener) {
            loadAppOpenAd(onShowAdCompleteListener);
            return this;
        }

        public Builder show() {
            showAppOpenAd();
            return this;
        }

        public Builder show(OnShowAdCompleteListener onShowAdCompleteListener) {
            showAppOpenAd(onShowAdCompleteListener);
            return this;
        }

        public Builder setAdStatus(boolean adStatus) {
            this.adStatus = adStatus;
            return this;
        }

        public Builder setAdNetwork(String adNetwork) {
            this.adNetwork = adNetwork;
            return this;
        }

        public Builder setBackupAdNetwork(String backupAdNetwork) {
            this.backupAdNetwork = backupAdNetwork;
            return this;
        }

        public Builder setAdMobAppOpenId(String adMobAppOpenId) {
            this.adMobAppOpenId = adMobAppOpenId;
            return this;
        }



        public void destroyOpenAd() {
            AppOpenAd.isAppOpenAdLoaded = false;
            if (MocFirstAds.ad_state) {
                //do nothing
                if("admob".equals(MocFirstAds.ad_type) || "admob".equals(MocFirstAds.second_ads)) {
                    if (appOpenAd != null) {
                        appOpenAd = null;
                    }
                }
            }
        }

        //main ads
        public void loadAppOpenAd(OnShowAdCompleteListener onShowAdCompleteListener) {
            if (MocFirstAds.ad_state) {
                if ("admob".equals(MocFirstAds.ad_type)) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    com.google.android.gms.ads.appopen.AppOpenAd.load(activity, adMobAppOpenId, adRequest, new com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull com.google.android.gms.ads.appopen.AppOpenAd ad) {
                            appOpenAd = ad;
                            showAppOpenAd(onShowAdCompleteListener);
                            Log.d(TAG, "[" + adNetwork + "] " + "[on start] app open ad loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            appOpenAd = null;
                            loadBackupAppOpenAd(onShowAdCompleteListener);
                            Log.d(TAG, "[" + adNetwork + "] " + "[on start] failed to load app open ad: " + loadAdError.getMessage());
                        }
                    });
                } else {
                    onShowAdCompleteListener.onShowAdComplete();
                }
            } else {
                onShowAdCompleteListener.onShowAdComplete();
            }
        }

        public void showAppOpenAd(OnShowAdCompleteListener onShowAdCompleteListener) {
            if ("admob".equals(MocFirstAds.ad_type)) {
                if (appOpenAd != null) {
                    appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            appOpenAd = null;
                            onShowAdCompleteListener.onShowAdComplete();
                            Log.d(TAG, "[" + adNetwork + "] " + "[on start] close app open ad");
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            appOpenAd = null;
                            onShowAdCompleteListener.onShowAdComplete();
                            Log.d(TAG, "[" + adNetwork + "] " + "[on start] failed to show app open ad: " + adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "[" + adNetwork + "] " + "[on start] show app open ad");
                        }
                    });
                    appOpenAd.show(activity);
                } else {
                    onShowAdCompleteListener.onShowAdComplete();
                }
            } else {
                onShowAdCompleteListener.onShowAdComplete();
            }
        }

        public void loadAppOpenAd() {
            if (MocFirstAds.ad_state) {
                //do nothing
                if ("admob".equals(MocFirstAds.ad_type)) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    com.google.android.gms.ads.appopen.AppOpenAd.load(activity, adMobAppOpenId, adRequest, new com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull com.google.android.gms.ads.appopen.AppOpenAd ad) {
                            appOpenAd = ad;
                            isAppOpenAdLoaded = true;
                            Log.d(TAG, "[" + adNetwork + "] " + "[on resume] app open ad loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            appOpenAd = null;
                            isAppOpenAdLoaded = false;
                            loadBackupAppOpenAd();
                            Log.d(TAG, "[" + adNetwork + "] " + "[on resume] failed to load app open ad : " + loadAdError.getMessage());
                        }
                    });
                }
            }
        }

        public void showAppOpenAd() {
            //do nothing
            if ("admob".equals(MocFirstAds.ad_type)) {
                if (appOpenAd != null) {
                    appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            appOpenAd = null;
                            loadAppOpenAd();
                            Log.d(TAG, "[" + adNetwork + "] " + "[on resume] close app open ad");
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            appOpenAd = null;
                            loadAppOpenAd();
                            Log.d(TAG, "[" + adNetwork + "] " + "[on resume] failed to show app open ad: " + adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "[" + adNetwork + "] " + "[on resume] show app open ad");
                        }
                    });
                    appOpenAd.show(activity);
                } else {
                    showBackupAppOpenAd();
                }
            }
        }

        //backup ads
        public void loadBackupAppOpenAd(OnShowAdCompleteListener onShowAdCompleteListener) {
            if (MocFirstAds.ad_state) {
                if ("admob".equals(MocFirstAds.second_ads)) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    com.google.android.gms.ads.appopen.AppOpenAd.load(activity, adMobAppOpenId, adRequest, new com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull com.google.android.gms.ads.appopen.AppOpenAd ad) {
                            appOpenAd = ad;
                            showBackupAppOpenAd(onShowAdCompleteListener);
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on start] [backup] app open ad loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            appOpenAd = null;
                            showBackupAppOpenAd(onShowAdCompleteListener);
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on start] [backup] failed to load app open ad: " + loadAdError.getMessage());
                        }
                    });
                } else {
                    onShowAdCompleteListener.onShowAdComplete();
                }
            } else {
                onShowAdCompleteListener.onShowAdComplete();
            }
        }

        public void showBackupAppOpenAd(OnShowAdCompleteListener onShowAdCompleteListener) {
            if ("admob".equals(MocFirstAds.second_ads)) {
                if (appOpenAd != null) {
                    appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            appOpenAd = null;
                            onShowAdCompleteListener.onShowAdComplete();
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on start] [backup] close app open ad");
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            appOpenAd = null;
                            onShowAdCompleteListener.onShowAdComplete();
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on start] [backup] failed to show app open ad: " + adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on start] [backup] show app open ad");
                        }
                    });
                    appOpenAd.show(activity);
                } else {
                    onShowAdCompleteListener.onShowAdComplete();
                }
            } else {
                onShowAdCompleteListener.onShowAdComplete();
            }
        }

        public void loadBackupAppOpenAd() {
            if (MocFirstAds.ad_state) {
                //do nothing
                if ("admob".equals(MocFirstAds.second_ads)) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    com.google.android.gms.ads.appopen.AppOpenAd.load(activity, adMobAppOpenId, adRequest, new com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull com.google.android.gms.ads.appopen.AppOpenAd ad) {
                            appOpenAd = ad;
                            isAppOpenAdLoaded = true;
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on resume] [backup] app open ad loaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            appOpenAd = null;
                            isAppOpenAdLoaded = false;
                            loadBackupAppOpenAd();
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on resume] [backup] failed to load app open ad : " + loadAdError.getMessage());
                        }
                    });
                }
            }
        }

        public void showBackupAppOpenAd() {
            //do nothing
            if ("admob".equals(MocFirstAds.second_ads)) {
                if (appOpenAd != null) {
                    appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            appOpenAd = null;
                            loadBackupAppOpenAd();
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on resume] [backup] close app open ad");
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            appOpenAd = null;
                            loadBackupAppOpenAd();
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on resume] [backup] failed to show app open ad: " + adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "[" + backupAdNetwork + "] " + "[on resume] [backup] show app open ad");
                        }
                    });
                    appOpenAd.show(activity);
                }
            }
        }

    }

}

package com.moca.futbol.adsmoca;

import static com.moca.futbol.MocConf.interCounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.moca.futbol.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;


public class AdsInterstitial {

   public int interInterval = MocFirstAds.interInterval;

    private static final String TAG = "--->JSON";
    private final Activity mActivity;
    private AlertDialog mAlertDialog;
    private com.google.android.gms.ads.interstitial.InterstitialAd adMobInterstitialAd;

    public interface AdFinished {
        void onAdFinished();
    }

    public AdsInterstitial(Activity mActivity) {
        this.mActivity = mActivity;
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.WrapContentDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.rob_loader, null);
        builder.setView(v);
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAlertDialog.setCancelable(false);
    }

    private Boolean canShowInterstitial() {
        return interCounter == interInterval;
    }


    public void showInterstitial(String interstitial_id, String backup_interstitial, AdFinished adFinished) {
        Log.d(TAG, "showInterstitial: start loading " + interstitial_id);
        mAlertDialog.show();
        if (MocFirstAds.ad_state && canShowInterstitial() && !interstitial_id.equals("0")) {
            switch (MocFirstAds.ad_type) {
                case "meta":
                    com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(mActivity, interstitial_id);
                    InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            adFinished.onAdFinished();
                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            showBackupInterstitial(backup_interstitial, adFinished);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            mAlertDialog.dismiss();
                            interCounter = 1;
                            interstitialAd.show();
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    interstitialAd.loadAd(
                            interstitialAd.buildLoadAdConfig()
                                    .withAdListener(interstitialAdListener)
                                    .build());
                    break;
                case "max":
                    MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(interstitial_id, mActivity);
                    maxInterstitialAd.setListener(new MaxAdListener() {
                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            mAlertDialog.dismiss();
                            interCounter = 1;
                            maxInterstitialAd.showAd();
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {
                            adFinished.onAdFinished();
                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            showBackupInterstitial(backup_interstitial, adFinished);
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            showBackupInterstitial(backup_interstitial, adFinished);
                        }
                    });
                    maxInterstitialAd.loadAd();
                    break;

                case "admob":

                    AdRequest adRequest = new AdRequest.Builder().build();
                    com.google.android.gms.ads.interstitial.InterstitialAd.load(mActivity, interstitial_id, adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                            mAlertDialog.dismiss();
                            adMobInterstitialAd = interstitialAd;
                            interCounter = 1;

                            adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    //.....................................
                                    adFinished.onAdFinished();

                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                    Log.d(TAG, "The ad failed to show.");

                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    adMobInterstitialAd = null;
                                    Log.d(TAG, "The ad was shown.");
                                }
                            });
                            adMobInterstitialAd.show(mActivity);
                            Log.i(TAG, "onAdLoaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.i(TAG, loadAdError.getMessage());
//                            adMobInterstitialAd = null;
                            showBackupInterstitial(backup_interstitial, adFinished);
                            Log.d(TAG, "Failed load AdMob Interstitial Ad");
                        }
                    });
                    break;
                case "unity":
                    UnityAds.load(interstitial_id, new IUnityAdsLoadListener() {
                        @Override
                        public void onUnityAdsAdLoaded(String placementId) {
                            mAlertDialog.dismiss();
                            UnityAds.show(mActivity, interstitial_id, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                                @Override
                                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                                    showBackupInterstitial(backup_interstitial, adFinished);
                                }

                                @Override
                                public void onUnityAdsShowStart(String placementId) {
                                    mAlertDialog.dismiss();
                                    interCounter = 1;
                                }

                                @Override
                                public void onUnityAdsShowClick(String placementId) {

                                }

                                @Override
                                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                                    adFinished.onAdFinished();
                                }
                            });
                        }

                        @Override
                        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                            showBackupInterstitial(backup_interstitial, adFinished);
                        }
                    });
                    break;

                default:
                    mAlertDialog.dismiss();
                    adFinished.onAdFinished();
                    break;
            }
        } else {
            mAlertDialog.dismiss();
            interCounter = interCounter + 1;
            adFinished.onAdFinished();
        }
    }

    public void showBackupInterstitial(String interstitial_id, AdFinished adFinished) {
        if (MocFirstAds.ad_state && !interstitial_id.equals("0")) {
            switch (MocFirstAds.second_ads) {
                case "meta":
                    com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(mActivity, interstitial_id);
                    InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            adFinished.onAdFinished();
                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            adFinished.onAdFinished();
                            mAlertDialog.dismiss();
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            mAlertDialog.dismiss();
                            interCounter = 1;
                            interstitialAd.show();
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    interstitialAd.loadAd(
                            interstitialAd.buildLoadAdConfig()
                                    .withAdListener(interstitialAdListener)
                                    .build());
                    break;
                case "max":
                    MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(interstitial_id, mActivity);
                    maxInterstitialAd.setListener(new MaxAdListener() {
                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            mAlertDialog.dismiss();
                            interCounter = 1;
                            maxInterstitialAd.showAd();
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {
                            adFinished.onAdFinished();
                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            adFinished.onAdFinished();
                            mAlertDialog.dismiss();
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            adFinished.onAdFinished();
                            mAlertDialog.dismiss();
                        }
                    });
                    maxInterstitialAd.loadAd();
                    break;

                case "admob":

                    AdRequest adRequest = new AdRequest.Builder().build();
                    com.google.android.gms.ads.interstitial.InterstitialAd.load(mActivity, interstitial_id, adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                            mAlertDialog.dismiss();
                            adMobInterstitialAd = interstitialAd;
                            interCounter = 1;
                            adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    //...............................................
                                    adFinished.onAdFinished();
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                    Log.d(TAG, "The ad failed to show.");
                                    mAlertDialog.dismiss();
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    adMobInterstitialAd = null;
                                    Log.d(TAG, "The ad was shown.");
                                }
                            });
                            adMobInterstitialAd.show(mActivity);
                            Log.i(TAG, "onAdLoaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.i(TAG, loadAdError.getMessage());
                            adMobInterstitialAd = null;
                            mAlertDialog.dismiss();
                            Log.d(TAG, "Failed load AdMob Interstitial Ad");
                        }
                    });
                    break;

                case "unity":
                    UnityAds.load(interstitial_id, new IUnityAdsLoadListener() {
                        @Override
                        public void onUnityAdsAdLoaded(String placementId) {
                            mAlertDialog.dismiss();
                            UnityAds.show(mActivity, interstitial_id, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                                @Override
                                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {

                                    adFinished.onAdFinished();
                                }

                                @Override
                                public void onUnityAdsShowStart(String placementId) {
                                    mAlertDialog.dismiss();
                                    interCounter = 1;
                                }

                                @Override
                                public void onUnityAdsShowClick(String placementId) {

                                }

                                @Override
                                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                                    adFinished.onAdFinished();
                                }
                            });
                        }

                        @Override
                        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                            adFinished.onAdFinished();
                        }
                    });
                    break;

                default:
                    mAlertDialog.dismiss();
                    adFinished.onAdFinished();
                    break;
            }
        } else {
            mAlertDialog.dismiss();
            interCounter = interCounter + 1;
            adFinished.onAdFinished();
        }
    }
}

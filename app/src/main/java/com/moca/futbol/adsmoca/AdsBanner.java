package com.moca.futbol.adsmoca;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.moca.futbol.R;
import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;


public class AdsBanner {

    private final Activity mActivity;
    private AdView fanAdView;
    private final int heightPx;


    FrameLayout ad_holder;
    ShimmerFrameLayout ad_loader;

    private com.google.android.gms.ads.AdView admobadView;

    public AdsBanner(Activity mActivity) {
        this.mActivity = mActivity;
        this.heightPx = mActivity.getResources().getDimensionPixelSize(R.dimen.applovin_banner_height);
        this.ad_holder = mActivity.findViewById(R.id.banneradHolder);
        this.ad_loader = mActivity.findViewById(R.id.banneradLoader);
    }

    public void loadMrEC(Activity activity, String mrec_id, String backup_mrec) {
        FrameLayout ad_holder2 = activity.findViewById(R.id.applovin_native_ad_container);
        ShimmerFrameLayout ad_loader2 = activity.findViewById(R.id.rect_shimmer_container);

        if (MocFirstAds.ad_state && !mrec_id.equals("0")) {


            fanAdView = new AdView(mActivity, mrec_id, AdSize.RECTANGLE_HEIGHT_250);
            ad_holder2.removeAllViews();
            ad_holder2.addView(fanAdView);
            AdListener adListener = new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    loadBackupMrEC(backup_mrec, ad_holder2, ad_loader2);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    ad_loader2.setVisibility(View.GONE);
                    ad_holder2.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
            fanAdView.loadAd(loadAdConfig);
        }
    }

    public void loadBackupMrEC(String mrec_id, FrameLayout fl, ShimmerFrameLayout al) {

        FrameLayout ad_holder2 = fl;
        ShimmerFrameLayout ad_loader2 = al;
        if (MocFirstAds.ad_state && !mrec_id.equals("0")) {


            fanAdView = new AdView(mActivity, mrec_id, AdSize.RECTANGLE_HEIGHT_250);
            ad_holder2.removeAllViews();
            ad_holder2.addView(fanAdView);
            AdListener adListener = new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    ad_loader2.setVisibility(View.GONE);
                    ad_holder2.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    ad_loader2.setVisibility(View.GONE);
                    ad_holder2.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
            fanAdView.loadAd(loadAdConfig);

        }
    }

//    public void loadFragmentMrec (View view, String mrec_id, String backup_mrec){
//
//        FrameLayout ad_holder = view.findViewById(R.id.applovin_native_ad_container);
//        ShimmerFrameLayout ad_loader = view.findViewById(R.id.rect_shimmer_container);
//
//        fanAdView = new AdView(mActivity, mrec_id, AdSize.RECTANGLE_HEIGHT_250);
//        ad_holder.removeAllViews();
//        ad_holder.addView(fanAdView);
//        AdListener adListener = new AdListener() {
//            @Override
//            public void onError(Ad ad, AdError adError) {
//                loadBackupMrEC(backup_mrec, ad_holder, ad_loader);
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//                ad_loader.setVisibility(View.GONE);
//                ad_holder.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//
//            }
//
//            @Override
//            public void onLoggingImpression(Ad ad) {
//
//            }
//        };
//        AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
//        fanAdView.loadAd(loadAdConfig);
//    }
//
//    public void loadBackupFragMrec(String mrec_id, FrameLayout fl, ShimmerFrameLayout al){
//
//        FrameLayout ad_holder = fl;
//        ShimmerFrameLayout ad_loader = al;
//
//        fanAdView = new AdView(mActivity, mrec_id, AdSize.RECTANGLE_HEIGHT_250);
//        ad_holder.removeAllViews();
//        ad_holder.addView(fanAdView);
//        AdListener adListener = new AdListener() {
//            @Override
//            public void onError(Ad ad, AdError adError) {
//                ad_loader.setVisibility(View.GONE);
//                ad_holder.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//                ad_loader.setVisibility(View.GONE);
//                ad_holder.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//
//            }
//
//            @Override
//            public void onLoggingImpression(Ad ad) {
//
//            }
//        };
//        AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
//        fanAdView.loadAd(loadAdConfig);
//
//    }

    public void loadActivityBanner(String banner_id, String backup_banner) {

        if (MocFirstAds.ad_state && !banner_id.equals("0") && MocFirstAds.isbanner) {
            switch (MocFirstAds.ad_type) {
                case "meta":
                    fanAdView = new AdView(mActivity, banner_id, AdSize.BANNER_HEIGHT_50);
                    ad_holder.removeAllViews();
                    ad_holder.addView(fanAdView);
                    AdListener adListener = new AdListener() {
                        @Override
                        public void onError(Ad ad, AdError adError) {
                            loadActivityBackupBanner(backup_banner);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
                    fanAdView.loadAd(loadAdConfig);
                    break;

                case "max":
                    MaxAdView maxAdView = new MaxAdView(banner_id, mActivity);
                    maxAdView.setListener(new MaxAdViewAdListener() {
                        @Override
                        public void onAdExpanded(MaxAd ad) {

                        }

                        @Override
                        public void onAdCollapsed(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {

                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            loadActivityBackupBanner(backup_banner);
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            loadActivityBackupBanner(backup_banner);
                        }
                    });
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    ad_holder.removeAllViews();
                    ad_holder.addView(maxAdView);
                    maxAdView.loadAd();
                    break;


                case "admob":

                    Log.e("l baaaaaaner", "ha l cas dial admobf l baaner");

                    ad_holder.post(() -> {
                        admobadView = new com.google.android.gms.ads.AdView(mActivity);

                        admobadView.setAdUnitId(banner_id);
                        ad_holder.removeAllViews();
                        ad_holder.addView(admobadView);
                        admobadView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
                        //...................................................................
                        AdRequest adRequest = new AdRequest.Builder().build();
                        admobadView.loadAd(adRequest);
                        admobadView.setAdListener(new com.google.android.gms.ads.AdListener() {
                            @Override
                            public void onAdLoaded() {

                                Log.e("l baaaaaaner", "tloaaaada");
                                // Code to be executed when an ad finishes loading.
                                ad_loader.setVisibility(View.GONE);
                                ad_holder.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                Log.e("l baaaaaaner", "mqkhdqqqqqqqmch22");
                                // Code to be executed when an ad request fails.
//                                ad_loader.setVisibility(View.GONE);
//                                ad_holder.setVisibility(View.GONE);
                                //............................................added
                                loadActivityBackupBanner(backup_banner);
                            }

                            @Override
                            public void onAdOpened() {
                                // Code to be executed when an ad opens an overlay that
                                // covers the screen.
                            }

                            @Override
                            public void onAdClicked() {
                                // Code to be executed when the user clicks on an ad.
                            }

                            @Override
                            public void onAdClosed() {
                                // Code to be executed when the user is about to return
                                // to the app after tapping on an ad.
                            }
                        });
                    });

                    break;

                case "unity":
                    BannerView bannerView = new BannerView(mActivity, banner_id, new UnityBannerSize(320, 50));
                    bannerView.setListener(new BannerView.IListener() {
                        @Override
                        public void onBannerLoaded(BannerView bannerAdView) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.removeAllViews();
                            ad_holder.addView(bannerAdView);
                            ad_holder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onBannerShown(BannerView bannerAdView) {

                        }

                        @Override
                        public void onBannerClick(BannerView bannerAdView) {

                        }

                        @Override
                        public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
                            loadActivityBackupBanner(backup_banner);
                        }

                        @Override
                        public void onBannerLeftApplication(BannerView bannerView) {

                        }
                    });
                    bannerView.load();
                    break;


                default:
                    ad_loader.setVisibility(View.GONE);
                    ad_holder.setVisibility(View.GONE);
                    break;
            }
        } else {
            ad_loader.setVisibility(View.GONE);
            ad_holder.setVisibility(View.GONE);
        }
    }

    public void loadActivityBackupBanner(String banner_id) {

        if (MocFirstAds.ad_state && !banner_id.equals("0") && MocFirstAds.isbanner) {
            switch (MocFirstAds.second_ads) {
                case "meta":

                    fanAdView = new AdView(mActivity, banner_id, AdSize.BANNER_HEIGHT_50);
                    ad_holder.removeAllViews();
                    ad_holder.addView(fanAdView);
                    AdListener adListener = new AdListener() {
                        @Override
                        public void onError(Ad ad, AdError adError) {

                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
                    fanAdView.loadAd(loadAdConfig);
                    break;

                case "max":
                    MaxAdView maxAdView = new MaxAdView(banner_id, mActivity);
                    maxAdView.setListener(new MaxAdViewAdListener() {
                        @Override
                        public void onAdExpanded(MaxAd ad) {

                        }
                        @Override
                        public void onAdCollapsed(MaxAd ad) {

                        }
                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }
                        @Override
                        public void onAdHidden(MaxAd ad) {

                        }
                        @Override
                        public void onAdClicked(MaxAd ad) {
                        }
                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.GONE);
                        }
                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.GONE);
                        }
                    });
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    ad_holder.removeAllViews();
                    ad_holder.addView(maxAdView);
                    maxAdView.loadAd();
                    break;

                case "admob":

                    ad_holder.post(() -> {
                        admobadView = new com.google.android.gms.ads.AdView(mActivity);
                        admobadView.setAdUnitId(banner_id);
                        ad_holder.removeAllViews();
                        ad_holder.addView(admobadView);
                        admobadView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
                        AdRequest adRequest = new AdRequest.Builder().build();
                        admobadView.loadAd(adRequest);
                        admobadView.setAdListener(new com.google.android.gms.ads.AdListener() {
                            @Override
                            public void onAdLoaded() {
                                // Code to be executed when an ad finishes loading.
                                ad_loader.setVisibility(View.GONE);
                                ad_holder.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                // Code to be executed when an ad request fails.
                                ad_loader.setVisibility(View.GONE);
                                ad_holder.setVisibility(View.GONE);
                            }
                            @Override
                            public void onAdOpened() {
                                // Code to be executed when an ad opens an overlay that
                                // covers the screen.
                            }
                            @Override
                            public void onAdClicked() {
                                // Code to be executed when the user clicks on an ad.
                            }
                            @Override
                            public void onAdClosed() {
                                // Code to be executed when the user is about to return
                                // to the app after tapping on an ad.
                            }
                        });
                    });

                    break;

                case "unity":
                    BannerView bannerView = new BannerView(mActivity, banner_id, new UnityBannerSize(320, 50));
                    bannerView.setListener(new BannerView.IListener() {
                        @Override
                        public void onBannerLoaded(BannerView bannerAdView) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.removeAllViews();
                            ad_holder.addView(bannerAdView);
                            ad_holder.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onBannerShown(BannerView bannerAdView) {
                        }
                        @Override
                        public void onBannerClick(BannerView bannerAdView) {
                        }
                        @Override
                        public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
                            ad_loader.setVisibility(View.GONE);
                            ad_holder.setVisibility(View.GONE);
                        }
                        @Override
                        public void onBannerLeftApplication(BannerView bannerView) {
                        }
                    });
                    bannerView.load();
                    break;

                default:
                    ad_loader.setVisibility(View.GONE);
                    ad_holder.setVisibility(View.GONE);
                    break;
            }
        } else {
            ad_loader.setVisibility(View.GONE);
            ad_holder.setVisibility(View.GONE);
        }
    }
}

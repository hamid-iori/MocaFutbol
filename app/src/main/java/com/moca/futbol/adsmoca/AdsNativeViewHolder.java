package com.moca.futbol.adsmoca;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.moca.futbol.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;

public class AdsNativeViewHolder extends RecyclerView.ViewHolder {

    LinearLayout nativeAdViewContainer;
    NativeAdLayout fanNativeAdLayout;
    FrameLayout applovinNativeAd;
    private ShimmerFrameLayout adLoader;

    private MaxAd maxAd;
    private Context context;

    public AdsNativeViewHolder(View view) {
        super(view);
        nativeAdViewContainer = view.findViewById(R.id.native_ad_view_container);
        fanNativeAdLayout = view.findViewById(R.id.fan_native_ad_container);
        applovinNativeAd = view.findViewById(R.id.applovin_native_ad_container);
        adLoader = view.findViewById(R.id.adLoader);
    }

    public void loadNativeAd(Context context, String main_native, String backup_native) {
        this.context = context;
        if (MocFirstAds.ad_state && !main_native.equals("0")) {
            switch (MocFirstAds.ad_type) {
                case "meta":
                    com.facebook.ads.NativeAd fanNativeAd = new com.facebook.ads.NativeAd(context, main_native);
                    NativeAdListener nativeAdListener = new NativeAdListener() {
                        @Override
                        public void onMediaDownloaded(Ad ad) {

                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            loadBackupNativeAd(backup_native);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            fanNativeAdLayout.setVisibility(View.VISIBLE);
                            if (fanNativeAd != ad) {
                                return;
                            }

                            fanNativeAd.unregisterView();
                            LayoutInflater inflater = LayoutInflater.from(context);
                            LinearLayout nativeAdView = (LinearLayout) inflater.inflate(R.layout.native_fan, fanNativeAdLayout, false);
                            fanNativeAdLayout.addView(nativeAdView);
                            adLoader.setVisibility(View.GONE);

                            LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                            AdOptionsView adOptionsView = new AdOptionsView(context, fanNativeAd, fanNativeAdLayout);
                            adChoicesContainer.removeAllViews();
                            adChoicesContainer.addView(adOptionsView, 0);

                            TextView nativeAdTitle = nativeAdView.findViewById(R.id.native_ad_title);
                            com.facebook.ads.MediaView nativeAdMedia = nativeAdView.findViewById(R.id.native_ad_media);
                            com.facebook.ads.MediaView nativeAdIcon = nativeAdView.findViewById(R.id.native_ad_icon);
                            TextView nativeAdSocialContext = nativeAdView.findViewById(R.id.native_ad_social_context);
                            TextView nativeAdBody = nativeAdView.findViewById(R.id.native_ad_body);
                            TextView sponsoredLabel = nativeAdView.findViewById(R.id.native_ad_sponsored_label);
                            Button nativeAdCallToAction = nativeAdView.findViewById(R.id.native_ad_call_to_action);

                            nativeAdTitle.setText(fanNativeAd.getAdvertiserName());
                            nativeAdBody.setText(fanNativeAd.getAdBodyText());
                            nativeAdSocialContext.setText(fanNativeAd.getAdSocialContext());
                            nativeAdCallToAction.setVisibility(fanNativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                            nativeAdCallToAction.setText(fanNativeAd.getAdCallToAction());
                            sponsoredLabel.setText(fanNativeAd.getSponsoredTranslation());

                            List<View> clickableViews = new ArrayList<>();
                            clickableViews.add(nativeAdTitle);
                            clickableViews.add(sponsoredLabel);
                            clickableViews.add(nativeAdIcon);
                            clickableViews.add(nativeAdMedia);
                            clickableViews.add(nativeAdBody);
                            clickableViews.add(nativeAdSocialContext);
                            clickableViews.add(nativeAdCallToAction);

                            fanNativeAd.registerViewForInteraction(nativeAdView, nativeAdIcon, nativeAdMedia, clickableViews);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    NativeAdBase.NativeLoadAdConfig loadAdConfig = fanNativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
                    fanNativeAd.loadAd(loadAdConfig);
                    break;

                case "max":
                    MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(main_native, context);
                    nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                        @Override
                        public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                            if (maxAd != null) {
                                nativeAdLoader.destroy(maxAd);
                            }

                            maxAd = ad;
                            applovinNativeAd.removeAllViews();
                            applovinNativeAd.addView(nativeAdView);
                            applovinNativeAd.setVisibility(View.VISIBLE);
                            adLoader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                            loadBackupNativeAd(backup_native);
                        }

                        @Override
                        public void onNativeAdClicked(final MaxAd ad) {
                            // Optional click callback
                        }
                    });
                    nativeAdLoader.loadAd(createNativeAdView());
                    break;

                case "admob":
                    LayoutInflater inflater = LayoutInflater.from(context);

                    AdLoader admobadLoader = new AdLoader.Builder(context, main_native)
                            .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                                @Override
                                public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {

                                    NativeAdView adView;
                                    adView = (NativeAdView) inflater.inflate(R.layout.native_wortise, null);
                                    populateNativeAdView(nativeAd, adView);
                                    applovinNativeAd.removeAllViews();
                                    applovinNativeAd.addView(adView);
                                    applovinNativeAd.setVisibility(View.VISIBLE);
                                    adLoader.setVisibility(View.GONE);


                                }
                            })
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    loadBackupNativeAd(backup_native);
                                }
                            })
                            .build();
                    admobadLoader.loadAd(new AdRequest.Builder().build());

                    break;


            }
        } else {
            adLoader.setVisibility(View.GONE);
        }
    }

    public void loadBackupNativeAd(String native_id) {
        if (MocFirstAds.ad_state && !native_id.equals("0")) {
            switch (MocFirstAds.second_ads) {
                case "meta":
                    com.facebook.ads.NativeAd fanNativeAd = new com.facebook.ads.NativeAd(context, native_id);
                    NativeAdListener nativeAdListener = new NativeAdListener() {
                        @Override
                        public void onMediaDownloaded(Ad ad) {

                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            adLoader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            fanNativeAdLayout.setVisibility(View.VISIBLE);
                            if (fanNativeAd != ad) {
                                return;
                            }

                            fanNativeAd.unregisterView();
                            LayoutInflater inflater = LayoutInflater.from(context);
                            LinearLayout nativeAdView = (LinearLayout) inflater.inflate(R.layout.native_fan, fanNativeAdLayout, false);
                            fanNativeAdLayout.addView(nativeAdView);
                            adLoader.setVisibility(View.GONE);

                            LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                            AdOptionsView adOptionsView = new AdOptionsView(context, fanNativeAd, fanNativeAdLayout);
                            adChoicesContainer.removeAllViews();
                            adChoicesContainer.addView(adOptionsView, 0);

                            TextView nativeAdTitle = nativeAdView.findViewById(R.id.native_ad_title);
                            com.facebook.ads.MediaView nativeAdMedia = nativeAdView.findViewById(R.id.native_ad_media);
                            com.facebook.ads.MediaView nativeAdIcon = nativeAdView.findViewById(R.id.native_ad_icon);
                            TextView nativeAdSocialContext = nativeAdView.findViewById(R.id.native_ad_social_context);
                            TextView nativeAdBody = nativeAdView.findViewById(R.id.native_ad_body);
                            TextView sponsoredLabel = nativeAdView.findViewById(R.id.native_ad_sponsored_label);
                            Button nativeAdCallToAction = nativeAdView.findViewById(R.id.native_ad_call_to_action);

                            nativeAdTitle.setText(fanNativeAd.getAdvertiserName());
                            nativeAdBody.setText(fanNativeAd.getAdBodyText());
                            nativeAdSocialContext.setText(fanNativeAd.getAdSocialContext());
                            nativeAdCallToAction.setVisibility(fanNativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                            nativeAdCallToAction.setText(fanNativeAd.getAdCallToAction());
                            sponsoredLabel.setText(fanNativeAd.getSponsoredTranslation());

                            List<View> clickableViews = new ArrayList<>();
                            clickableViews.add(nativeAdTitle);
                            clickableViews.add(sponsoredLabel);
                            clickableViews.add(nativeAdIcon);
                            clickableViews.add(nativeAdMedia);
                            clickableViews.add(nativeAdBody);
                            clickableViews.add(nativeAdSocialContext);
                            clickableViews.add(nativeAdCallToAction);

                            fanNativeAd.registerViewForInteraction(nativeAdView, nativeAdIcon, nativeAdMedia, clickableViews);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    NativeAdBase.NativeLoadAdConfig loadAdConfig = fanNativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
                    fanNativeAd.loadAd(loadAdConfig);
                    break;

                case "max":
                    MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(native_id, context);
                    nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                        @Override
                        public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                            if (maxAd != null) {
                                nativeAdLoader.destroy(maxAd);
                            }

                            maxAd = ad;
                            applovinNativeAd.removeAllViews();
                            applovinNativeAd.addView(nativeAdView);
                            applovinNativeAd.setVisibility(View.VISIBLE);
                            adLoader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                            adLoader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onNativeAdClicked(final MaxAd ad) {
                            // Optional click callback
                        }
                    });
                    nativeAdLoader.loadAd(createNativeAdView());
                    break;

                case "admob":
                    LayoutInflater inflater = LayoutInflater.from(context);

                    AdLoader admobadLoader = new AdLoader.Builder(context, native_id)
                            .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                                @Override
                                public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {

                                    NativeAdView adView;
                                    adView = (NativeAdView) inflater.inflate(R.layout.native_wortise, null);
                                    populateNativeAdView(nativeAd, adView);
                                    applovinNativeAd.removeAllViews();
                                    applovinNativeAd.addView(adView);
                                    applovinNativeAd.setVisibility(View.VISIBLE);
                                    adLoader.setVisibility(View.GONE);


                                }
                            })
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                    adLoader.setVisibility(View.GONE);
                                }
                            })
                            .build();
                    admobadLoader.loadAd(new AdRequest.Builder().build());

                    break;

            }
        } else {
            adLoader.setVisibility(View.GONE);
        }
    }


    private MaxNativeAdView createNativeAdView() {
        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.native_max)
                .setTitleTextViewId(R.id.title_text_view)
                .setBodyTextViewId(R.id.body_text_view)
                .setAdvertiserTextViewId(R.id.advertiser_textView)
                .setIconImageViewId(R.id.icon_image_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.cta_button)
                .build();
        return new MaxNativeAdView(binder, context);

    }


    public void populateNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView nativeAdView) {

        nativeAdView.setMediaView(nativeAdView.findViewById(R.id.media_view));
        nativeAdView.setHeadlineView(nativeAdView.findViewById(R.id.primary));
        nativeAdView.setBodyView(nativeAdView.findViewById(R.id.body));
        nativeAdView.setCallToActionView(nativeAdView.findViewById(R.id.cta));
        nativeAdView.setIconView(nativeAdView.findViewById(R.id.icon));

        ((TextView) nativeAdView.getHeadlineView()).setText(nativeAd.getHeadline());
        nativeAdView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        if (nativeAd.getBody() == null) {
            nativeAdView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            nativeAdView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) nativeAdView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            nativeAdView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            nativeAdView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) nativeAdView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            nativeAdView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) nativeAdView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            nativeAdView.getIconView().setVisibility(View.VISIBLE);
        }

        nativeAdView.setNativeAd(nativeAd);
    }

}

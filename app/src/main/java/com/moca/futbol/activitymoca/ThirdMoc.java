package com.moca.futbol.activitymoca;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.BuildConfig;

import com.moca.futbol.R;
import com.moca.futbol.adsmoca.AdsBanner;
import com.moca.futbol.adsmoca.AdsInterstitial;
import com.moca.futbol.adsmoca.AdsNative;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.google.android.material.button.MaterialButton;

public class ThirdMoc extends AppCompatActivity {

    private AdsInterstitial adsInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_second);

        MaterialButton shareMyBtn = findViewById(R.id.btn_second_share);
        MaterialButton goMainBtn = findViewById(R.id.btn_second_next);


        adsInterstitial = new AdsInterstitial(ThirdMoc.this);
        if (MocFirstAds.meta_native) {

            AdsNative adsNative = new AdsNative(this);
            adsNative.loadNativeAd(MocFirstAds.second_native, MocSecAds.second_native);

        } else if (MocFirstAds.ad_state) {
            if (MocFirstAds.ad_type.equals("meta")) {
                AdsBanner adsBanner2 = new AdsBanner(ThirdMoc.this);
                adsBanner2.loadMrEC(ThirdMoc.this, MocFirstAds.second_rect, MocSecAds.second_rect);
            }
        }
        AdsBanner adsBanner = new AdsBanner(ThirdMoc.this);
        adsBanner.loadActivityBanner(MocFirstAds.second_banner, MocSecAds.second_banner);

        goMainBtn.setOnClickListener(view -> {
            adsInterstitial.showInterstitial(MocFirstAds.second_inter, MocSecAds.second_inter, () -> {
                startActivity(new Intent(ThirdMoc.this, MainActivity.class));
                overridePendingTransition(0, 0);
            });
        });

        shareMyBtn.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBody = getString(R.string.share_app_message);
            String shareSub = getString(R.string.app_name);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            startActivity(Intent.createChooser(shareIntent, "Share using"));

        });
    }

}
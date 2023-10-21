package com.moca.futbol.activitymoca;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.moca.futbol.R;
import com.moca.futbol.adsmoca.AdsBanner;
import com.moca.futbol.adsmoca.AdsInterstitial;
import com.moca.futbol.adsmoca.AdsNative;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.google.android.material.button.MaterialButton;

public class SecMoc extends AppCompatActivity {
    private AdsInterstitial adsInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_first);

        MaterialButton goToSecBtn = findViewById(R.id.btn_second_next);
        MaterialButton myPrivacyBtn = findViewById(R.id.btn_policy);

        adsInterstitial = new AdsInterstitial(SecMoc.this);

        if (MocFirstAds.meta_native) {
            AdsNative adsNative = new AdsNative(this);
            adsNative.loadNativeAd(MocFirstAds.first_native, MocSecAds.first_native);

        } else if (MocFirstAds.ad_state) {
            if (MocFirstAds.ad_type.equals("meta")) {

                AdsBanner adsBanner = new AdsBanner(SecMoc.this);
                adsBanner.loadMrEC(SecMoc.this, MocFirstAds.first_rect, MocSecAds.first_rect);
            }
        }

        AdsBanner adsBanner2 = new AdsBanner(SecMoc.this);
        adsBanner2.loadActivityBanner(MocFirstAds.first_banner, MocSecAds.first_banner);

        goToSecBtn.setOnClickListener(view -> {
            adsInterstitial.showInterstitial(MocFirstAds.first_inter, MocSecAds.first_inter, () -> {

                startActivity(new Intent(SecMoc.this, ThirdMoc.class));
                overridePendingTransition(0, 0);
            });
        });
        myPrivacyBtn.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MocFirstAds.policy_link))));

    }

}
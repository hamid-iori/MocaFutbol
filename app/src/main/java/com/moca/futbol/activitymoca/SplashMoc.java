package com.moca.futbol.activitymoca;

import static com.moca.futbol.helpermoca.MocConstant.LOCALHOST_ADDRESS;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.multidex.BuildConfig;

import com.moca.futbol.helpermoca.MocHelper;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.sdk.AppLovinSdk;

import com.moca.futbol.adopenmoca.AdNetwork;
import com.moca.futbol.adopenmoca.AppOpenAd;
import com.moca.futbol.helpermoca.MocConstant;
import com.moca.futbol.MocConf;
import com.moca.futbol.R;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.MocSecAds;
import com.unity3d.ads.UnityAds;


import org.json.JSONException;
import org.json.JSONObject;

public class SplashMoc extends AppCompatActivity {

    ConstraintLayout parent_view_Moc_splash;

    public static int jsonMocStatus = 0;
    AdNetwork.Initialize adNetwork;
    AppOpenAd.Builder appOpenAdBuilder;

    private String json_status = "";
    private String unity_id = "";
    SharedPrefs sharedPrefs_Mat;
    private boolean myVpnStatus = false;

    private MocHelper myPlayHelper;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (jsonMocStatus == 0) {
                handler.postDelayed(this, 500);
            } else if (jsonMocStatus == 1) {

                checkSourceMoc();

            } else if (jsonMocStatus == 2) {
                // If there was an error during data loading, start ActivityComingSoon
                Intent intent = new Intent(SplashMoc.this, ServerMoc.class);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        myVpnStatus = new MocHelper(SplashMoc.this).isVpnConnectionAvailable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_splash);

        parent_view_Moc_splash = findViewById(R.id.parent_view_splash);

        sharedPrefs_Mat = new SharedPrefs(this);
        myPlayHelper = new MocHelper(SplashMoc.this);
        myVpnStatus = new MocHelper(SplashMoc.this).isVpnConnectionAvailable();

        if (MocHelper.isInternetKoloConnected(this)) {

            loadMocJson();
            if (MocFirstAds.allow_VPN) {
                checkLoadingStatus();
            } else {
                if (myVpnStatus) {
                    myPlayHelper.showWarningDialog(SplashMoc.this, "VPN!", "You are Not Allowed To Use VPN Here!", R.raw.network_activity_icon);
                } else {
                    checkLoadingStatus();
                }
            }
        } else {
            // Internet is not connected, show error message
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Internet Connection")
                    .setMessage("Please check your internet connection and try again.")
                    .setPositiveButton("Retry", (dialog, which) -> {
                        dialog.dismiss();
                        recreate();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private void checkLoadingStatus() {
        handler.postDelayed(runnable, 3000);
    }

    private void loadMocJson() {


        final int[] encrypted = {52,77,50,69,112,134,118,137,54,140,143,143,53,69,140,78,77,136,70,48,114,60,51,120,128,57,55,50,71,130,48,116,119,123,130,49,56,60,58,141,61,78,53,123,128,51,52,137,88,140,63,56,136,63,112,51,55,};
        final String json_Url = MocHelper.decrypt(encrypted, "live36");

        RequestQueue requestQueue = Volley.newRequestQueue(SplashMoc.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, json_Url, null, response -> {
            try {
                MocFirstAds.data = response.getJSONObject("Data");
                MocSecAds.data = response.getJSONObject("Data");

                JSONObject settings = MocFirstAds.data.getJSONObject("Settings");

                MocFirstAds.ad_state = settings.getBoolean("ad_state");
                MocFirstAds.ad_type = settings.getString("ad_type");
                MocFirstAds.second_ads = settings.getString("ad_backup");
                MocFirstAds.update_link = settings.getString("update_link");
                MocFirstAds.interInterval = settings.getInt("interInterval");
                MocFirstAds.moreapps_link = settings.getString("moreapps_link");
                MocFirstAds.policy_link = settings.getString("policy_link");
                json_status = settings.getString("my_status");
                MocFirstAds.meta_native = settings.getBoolean("meta_native");
                MocFirstAds.allow_VPN = settings.getBoolean("allow_vpn");
                MocFirstAds.isAdeddActivity = settings.getBoolean("isAdeddActivity");
                MocFirstAds.isPlayerAds = settings.getBoolean("isPlayerAds");

                MocFirstAds.isbanner = settings.getBoolean("isbanner");
                MocFirstAds.ischeckinstal = settings.getBoolean("ischeckinstal");

                MocConf.REST_API_KEY = settings.getString("REST_API_KEY");
                MocConf.SERVER_KEY = settings.getString("SERVER_KEY");

                requestConfigMoc();

                switch (MocFirstAds.ad_type) {
                    case "meta":
                        mainAdsMeta(MocSecAds.data.getJSONObject("Meta"));
                        break;
                    case "max":
                        mainAdsMax(MocSecAds.data.getJSONObject("Max"));
                        break;
                    case "admob":
//                        adAppOpenAdId = response.getJSONObject("Data").getJSONObject("Admob").getString("admob_open_id");
                        mainAdsAdmob(MocSecAds.data.getJSONObject("Admob"));

                        break;
                    case "unity":
                        unity_id = response.getJSONObject("Data").getJSONObject("Unity").getString("unity_id");
                        mainAdsUnity(MocSecAds.data.getJSONObject("Unity"));
                        break;

                }
                switch (MocFirstAds.second_ads) {
                    case "meta":
                        secondAdsMeta(MocSecAds.data.getJSONObject("Meta"));
                        break;
                    case "max":
                        secondAdsMax(MocSecAds.data.getJSONObject("Max"));
                        break;
                    case "admob":
                        secondAdsAdmob(MocSecAds.data.getJSONObject("Admob"));
                        break;
                    case "unity":
                        unity_id = response.getJSONObject("Data").getJSONObject("Unity").getString("unity_id");
                        secondAdsUnity(MocSecAds.data.getJSONObject("Unity"));

                        break;
                }
                jsonMocStatus = 1;
                //................

            } catch (JSONException e) {
                e.printStackTrace();
                jsonMocStatus = 2;
                Log.d("--->JSON21", "onResponse: " + e);
            }
        },
                error -> {
                    Log.d("--->JSON22", "onErrorResponse: " + error.toString());
                    jsonMocStatus = 2;
                });

        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);
    }

    private void requestConfigMoc() {
        String data = MocHelper.decode(MocConf.SERVER_KEY);
        String[] results = data.split("_applicationId_");
        String baseUrl = results[0].replace("localhost", LOCALHOST_ADDRESS);
        String applicationId = results[1];
        sharedPrefs_Mat.saveConfig(baseUrl, applicationId);
    }

    private void statusActivityMoc() {
        switch (json_status) {
            case "1": {
                Intent intent3;
                if (MocFirstAds.isAdeddActivity) {
                    intent3 = new Intent(SplashMoc.this, SecMoc.class);
                } else {
                    intent3 = new Intent(SplashMoc.this, MainActivity.class);
                }
                startActivity(intent3);
                overridePendingTransition(0, 0);
                break;
            }
            case "0": {
                Intent intent1 = new Intent(SplashMoc.this, RedMoc.class);
                startActivity(intent1);
                overridePendingTransition(0, 0);
                break;
            }
            case "2": {
                Intent intent2 = new Intent(SplashMoc.this, ServerMoc.class);
                startActivity(intent2);
                overridePendingTransition(0, 0);
                break;
            }
        }
    }

    private void mainAdsMeta(JSONObject meta) {
        try {
            initAds();

            MocFirstAds.first_banner = meta.getString("first_banner");
            MocFirstAds.second_banner = meta.getString("second_banner");
            MocFirstAds.main_banner = meta.getString("main_banner");
            MocFirstAds.channel_banner = meta.getString("channel_banner");
            MocFirstAds.category_banner = meta.getString("category_banner");

            MocFirstAds.first_inter = meta.getString("first_inter");
            MocFirstAds.second_inter = meta.getString("second_inter");
            MocFirstAds.frag_recent_inter = meta.getString("frag_recent_inter");
            MocFirstAds.frag_category_inter = meta.getString("frag_category_inter");
            MocFirstAds.frag_favorite_inter = meta.getString("frag_favorite_inter");
            MocFirstAds.channel_inter = meta.getString("channel_inter");
            MocFirstAds.category_inter = meta.getString("category_inter");
            MocFirstAds.search_inter = meta.getString("search_inter");

            MocFirstAds.first_native = meta.getString("first_native");
            MocFirstAds.second_native = meta.getString("second_native");
            MocFirstAds.frag_recent_native = meta.getString("frag_recent_native");
            MocFirstAds.channel_native = meta.getString("channel_native");
            MocFirstAds.category_native = meta.getString("category_native");

            MocFirstAds.first_rect = meta.getString("first_rect");
            MocFirstAds.second_rect = meta.getString("second_rect");
            MocFirstAds.channel_rect = meta.getString("channel_rect");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mainAdsMax(JSONObject max) {
        try {
            AppLovinSdk.getInstance(SplashMoc.this).setMediationProvider("max");
            AppLovinSdk.initializeSdk(SplashMoc.this);

            MocFirstAds.first_banner = max.getString("first_banner");
            MocFirstAds.second_banner = max.getString("second_banner");
            MocFirstAds.main_banner = max.getString("main_banner");
            MocFirstAds.channel_banner = max.getString("channel_banner");
            MocFirstAds.category_banner = max.getString("category_banner");

            MocFirstAds.first_inter = max.getString("first_inter");
            MocFirstAds.second_inter = max.getString("second_inter");
            MocFirstAds.frag_recent_inter = max.getString("frag_recent_inter");
            MocFirstAds.frag_category_inter = max.getString("frag_category_inter");
            MocFirstAds.frag_favorite_inter = max.getString("frag_favorite_inter");
            MocFirstAds.channel_inter = max.getString("channel_inter");
            MocFirstAds.category_inter = max.getString("category_inter");
            MocFirstAds.search_inter = max.getString("search_inter");

            MocFirstAds.first_native = max.getString("first_native");
            MocFirstAds.second_native = max.getString("second_native");
            MocFirstAds.frag_recent_native = max.getString("frag_recent_native");
            MocFirstAds.channel_native = max.getString("channel_native");
            MocFirstAds.category_native = max.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mainAdsAdmob(JSONObject admob) {
        try {
            initAds();

            MocFirstAds.admob_open_id = admob.getString("admob_open_id");

            MocFirstAds.first_banner = admob.getString("first_banner");
            MocFirstAds.second_banner = admob.getString("second_banner");
            MocFirstAds.main_banner = admob.getString("main_banner");
            MocFirstAds.channel_banner = admob.getString("channel_banner");
            MocFirstAds.category_banner = admob.getString("category_banner");

            MocFirstAds.first_inter = admob.getString("first_inter");
            MocFirstAds.second_inter = admob.getString("second_inter");
            MocFirstAds.frag_recent_inter = admob.getString("frag_recent_inter");
            MocFirstAds.frag_category_inter = admob.getString("frag_category_inter");
            MocFirstAds.frag_favorite_inter = admob.getString("frag_favorite_inter");
            MocFirstAds.channel_inter = admob.getString("channel_inter");
            MocFirstAds.category_inter = admob.getString("category_inter");
            MocFirstAds.search_inter = admob.getString("search_inter");

            MocFirstAds.first_native = admob.getString("first_native");
            MocFirstAds.second_native = admob.getString("second_native");
            MocFirstAds.frag_recent_native = admob.getString("frag_recent_native");
            MocFirstAds.channel_native = admob.getString("channel_native");
            MocFirstAds.category_native = admob.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mainAdsUnity(JSONObject unity) {
        try {

            UnityAds.initialize(SplashMoc.this, unity_id, true);

            MocFirstAds.first_banner = unity.getString("first_banner");
            MocFirstAds.second_banner = unity.getString("second_banner");
            MocFirstAds.main_banner = unity.getString("main_banner");
            MocFirstAds.channel_banner = unity.getString("channel_banner");
            MocFirstAds.category_banner = unity.getString("category_banner");

            MocFirstAds.first_inter = unity.getString("first_inter");
            MocFirstAds.second_inter = unity.getString("second_inter");
            MocFirstAds.frag_recent_inter = unity.getString("frag_recent_inter");
            MocFirstAds.frag_category_inter = unity.getString("frag_category_inter");
            MocFirstAds.frag_favorite_inter = unity.getString("frag_favorite_inter");
            MocFirstAds.channel_inter = unity.getString("channel_inter");
            MocFirstAds.category_inter = unity.getString("category_inter");
            MocFirstAds.search_inter = unity.getString("search_inter");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void secondAdsMeta(JSONObject meta) {
        try {
            initAds();

            MocSecAds.first_banner = meta.getString("first_banner");
            MocSecAds.second_banner = meta.getString("second_banner");
            MocSecAds.main_banner = meta.getString("main_banner");
            MocSecAds.channel_banner = meta.getString("channel_banner");
            MocSecAds.category_banner = meta.getString("category_banner");

            MocSecAds.first_inter = meta.getString("first_inter");
            MocSecAds.second_inter = meta.getString("second_inter");
            MocSecAds.frag_recent_inter = meta.getString("frag_recent_inter");
            MocSecAds.frag_category_inter = meta.getString("frag_category_inter");
            MocSecAds.frag_favorite_inter = meta.getString("frag_favorite_inter");
            MocSecAds.channel_inter = meta.getString("channel_inter");
            MocSecAds.category_inter = meta.getString("category_inter");
            MocSecAds.search_inter = meta.getString("search_inter");

            MocSecAds.first_native = meta.getString("first_native");
            MocSecAds.second_native = meta.getString("second_native");
            MocSecAds.frag_recent_native = meta.getString("frag_recent_native");
            MocSecAds.channel_native = meta.getString("channel_native");
            MocSecAds.category_native = meta.getString("category_native");

            MocSecAds.first_rect = meta.getString("first_rect");
            MocSecAds.second_rect = meta.getString("second_rect");
            MocSecAds.channel_rect = meta.getString("channel_rect");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void secondAdsMax(JSONObject max) {
        try {

            AppLovinSdk.getInstance(SplashMoc.this).setMediationProvider("max");
            AppLovinSdk.initializeSdk(SplashMoc.this);

            MocSecAds.first_banner = max.getString("first_banner");
            MocSecAds.second_banner = max.getString("second_banner");
            MocSecAds.main_banner = max.getString("main_banner");
            MocSecAds.channel_banner = max.getString("channel_banner");
            MocSecAds.category_banner = max.getString("category_banner");

            MocSecAds.first_inter = max.getString("first_inter");
            MocSecAds.second_inter = max.getString("second_inter");
            MocSecAds.frag_recent_inter = max.getString("frag_recent_inter");
            MocSecAds.frag_category_inter = max.getString("frag_category_inter");
            MocSecAds.frag_favorite_inter = max.getString("frag_favorite_inter");
            MocSecAds.channel_inter = max.getString("channel_inter");
            MocSecAds.category_inter = max.getString("category_inter");
            MocSecAds.search_inter = max.getString("search_inter");

            MocSecAds.first_native = max.getString("first_native");
            MocSecAds.second_native = max.getString("second_native");
            MocSecAds.frag_recent_native = max.getString("frag_recent_native");
            MocSecAds.channel_native = max.getString("channel_native");
            MocSecAds.category_native = max.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void secondAdsAdmob(JSONObject admob) {
        try {

            initAds();

            MocSecAds.first_banner = admob.getString("first_banner");
            MocSecAds.second_banner = admob.getString("second_banner");
            MocSecAds.main_banner = admob.getString("main_banner");
            MocSecAds.channel_banner = admob.getString("channel_banner");
            MocSecAds.category_banner = admob.getString("category_banner");

            MocSecAds.first_inter = admob.getString("first_inter");
            MocSecAds.second_inter = admob.getString("second_inter");
            MocSecAds.frag_recent_inter = admob.getString("frag_recent_inter");
            MocSecAds.frag_category_inter = admob.getString("frag_category_inter");
            MocSecAds.frag_favorite_inter = admob.getString("frag_favorite_inter");
            MocSecAds.channel_inter = admob.getString("channel_inter");
            MocSecAds.category_inter = admob.getString("category_inter");
            MocSecAds.search_inter = admob.getString("search_inter");

            MocSecAds.first_native = admob.getString("first_native");
            MocSecAds.second_native = admob.getString("second_native");
            MocSecAds.frag_recent_native = admob.getString("frag_recent_native");
            MocSecAds.channel_native = admob.getString("channel_native");
            MocSecAds.category_native = admob.getString("category_native");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void secondAdsUnity(JSONObject unity) {
        try {
            UnityAds.initialize(SplashMoc.this, unity_id, true);

            MocSecAds.first_banner = unity.getString("first_banner");
            MocSecAds.second_banner = unity.getString("second_banner");
            MocSecAds.main_banner = unity.getString("main_banner");
            MocSecAds.channel_banner = unity.getString("channel_banner");
            MocSecAds.category_banner = unity.getString("category_banner");

            MocSecAds.first_inter = unity.getString("first_inter");
            MocSecAds.second_inter = unity.getString("second_inter");
            MocSecAds.frag_recent_inter = unity.getString("frag_recent_inter");
            MocSecAds.frag_category_inter = unity.getString("frag_category_inter");
            MocSecAds.frag_favorite_inter = unity.getString("frag_favorite_inter");
            MocSecAds.channel_inter = unity.getString("channel_inter");
            MocSecAds.category_inter = unity.getString("category_inter");
            MocSecAds.search_inter = unity.getString("search_inter");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MocFirstAds.allow_VPN) {
            //check vpn connection
            myPlayHelper = new MocHelper(SplashMoc.this);
            myVpnStatus = myPlayHelper.isVpnConnectionAvailable();
            if (myVpnStatus) {
                myPlayHelper.showWarningDialog(SplashMoc.this, "VPN!", "You are Not Allowed To Use VPN Here!", R.raw.network_activity_icon);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When the Activity is destroyed, remove the callbacks from the Handler
        handler.removeCallbacks(runnable);
    }

    private void initAds() {
        adNetwork = new AdNetwork.Initialize(this)
                .setAdStatus(MocFirstAds.ad_state)
                .setAdNetwork(MocFirstAds.ad_type)
                .setBackupAdNetwork(MocFirstAds.second_ads)
                .setDebug(BuildConfig.DEBUG)
                .build();
    }

    private void loadMyOpenAdMoc() {

        if (MocConstant.OPEN_ADS_ON_START) {
            if (MocFirstAds.ad_state) {
                appOpenAdBuilder = new AppOpenAd.Builder(this)
                        .setAdStatus(MocFirstAds.ad_state)
                        .setAdNetwork(MocFirstAds.ad_type)
                        .setBackupAdNetwork(MocFirstAds.second_ads)
                        .setAdMobAppOpenId(MocFirstAds.admob_open_id)

                        .build(this::statusActivityMoc);
                MocFirstAds.isAppOpen = true;

            } else {
                statusActivityMoc();
            }
        } else {
            statusActivityMoc();
        }
    }

    private void checkOpenMoc() {

        if (MocFirstAds.ad_state) {
            if ("admob".equals(MocFirstAds.ad_type) || "admob".equals(MocFirstAds.second_ads)) {

                loadMyOpenAdMoc();
            } else {
                statusActivityMoc();
            }
        } else {
            statusActivityMoc();
        }
    }

    private boolean isInstalledFromGooglePlay() {
        String installer = getPackageManager()
                .getInstallerPackageName(getPackageName());
        Log.d("TAG", "isInstalledFromGooglePlay: " + installer);
        return "com.android.vending".equals(installer);
    }

    private void checkSourceMoc() {
        if (MocFirstAds.ischeckinstal) {
            if (isInstalledFromGooglePlay()) {

                checkOpenMoc();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashMoc.this);
                builder.setTitle(getString(R.string.google_verification_dialog_title))
                        .setMessage(getString(R.string.google_verification_dialog_message))
                        .setPositiveButton(getString(R.string.external_player_dialog_install), (dialog, id) -> {
                            try {
                                // Try opening the Play Store app first
                                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                // Fallback to opening in a web browser
                                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                                Intent goToBrowser = new Intent(Intent.ACTION_VIEW, uri);
                                goToBrowser.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                startActivity(goToBrowser);
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_cancel), (dialog, id) -> {
                            ActivityCompat.finishAffinity(SplashMoc.this);
                            System.exit(0);
                        })
                        .setCancelable(false);
                builder.create().show();

            }

        } else {
            checkOpenMoc();
        }

    }
}
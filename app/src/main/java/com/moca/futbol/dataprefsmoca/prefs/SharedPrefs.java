package com.moca.futbol.dataprefsmoca.prefs;


import static com.moca.futbol.MocConf.CATEGORY_VIEW_TYPE;
import static com.moca.futbol.MocConf.CHANNEL_VIEW_TYPE;
import static com.moca.futbol.MocConf.DEFAULT_PLAYER_SCREEN_ORIENTATION;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    Context context;

    private final SharedPreferences.Editor editor;
    private final SharedPreferences sharedPreferences;


    public SharedPrefs(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public void saveConfig(String api_url, String application_id) {
        editor.putString("api_url", api_url);
        editor.putString("application_id", application_id);
        editor.apply();
    }
    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }
    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }
    public Integer getCategoryViewType() {
        return sharedPreferences.getInt("category_list", CATEGORY_VIEW_TYPE);
    }

    public void updateCategoryViewType(int position) {
        editor.putInt("category_list", position);
        editor.apply();
    }

    public void updateChannelViewType(int position) {
        editor.putInt("video_list", position);
        editor.apply();
    }
    public Integer getChannelViewType() {
        return sharedPreferences.getInt("video_list", CHANNEL_VIEW_TYPE);
    }


    public String getBaseUrl() {
        return sharedPreferences.getString("api_url", "http://10.0.2.2/the_stream");
    }

    public Integer getPlayerMode() {
        return sharedPreferences.getInt("player_mode", DEFAULT_PLAYER_SCREEN_ORIENTATION);
    }

    public void updatePlayerMode(int position) {
        editor.putInt("player_mode", position);
        editor.apply();
    }


}

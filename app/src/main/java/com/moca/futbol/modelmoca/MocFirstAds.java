package com.moca.futbol.modelmoca;

import org.json.JSONObject;


public class MocFirstAds {

    private static String json_status ;

    public static void setJsonStatus(String jsonstatus){
        json_status=jsonstatus;
    }
    public static String getJsonStatus(){
        return json_status;
    }

    //Settings
    public static JSONObject data;
    public static boolean ad_state ;
    public static String ad_type = "";
    public static String second_ads = "";
    public static int interInterval ;
    public static String update_link = "";
    public static String policy_link = "";
    public static String moreapps_link = "";

    public static boolean meta_native ;
    public static boolean allow_VPN;
    public static boolean isAdeddActivity ;
    public static boolean isPlayerAds ;
    public static boolean isbanner ;
    public static boolean ischeckinstal ;

    public static boolean isAppOpen = false;

    //Array

    // ADS

    public static  String admob_open_id = "";

    // Banner
    public static String first_banner = "";
    public static String second_banner = "";
    public static String main_banner = "";
    public static String channel_banner = "";
    public static String category_banner = "";

    // Inter
    public static String first_inter = "";
    public static String second_inter = "";
    public static String frag_recent_inter = "";
    public static String frag_category_inter = "";
    public static String frag_favorite_inter = "";
    public static String channel_inter = "";
    public static String category_inter = "";
    public static String search_inter = "";

    // Native

    public static String first_native = "";
    public static String second_native = "";
    public static String frag_recent_native = "";
    public static String channel_native = "";
    public static String category_native = "";  //  frag cat native

    //MreC
    public static String first_rect = "";
    public static String second_rect  = "";
    public static String channel_rect = "";


}

package com.moca.futbol;


import com.moca.futbol.helpermoca.MocConstant;

public class MocConf {
    public static String SERVER_KEY = "";
    public static String REST_API_KEY = "";

    //available channel view type : CHANNEL_LIST_DEFAULT, CHANNEL_GRID_2_COLUMN or CHANNEL_GRID_3_COLUMN
    public static final int CHANNEL_VIEW_TYPE = MocConstant.CHANNEL_LIST_DEFAULT;

    //available channel view type : CATEGORY_LIST_DEFAULT, CATEGORY_GRID_2_COLUMN or CATEGORY_GRID_3_COLUMN
    public static final int CATEGORY_VIEW_TYPE = MocConstant.CATEGORY_GRID_2_COLUMN;

    //available player screen orientation : PLAYER_MODE_PORTRAIT or PLAYER_MODE_LANDSCAPE
    public static final int DEFAULT_PLAYER_SCREEN_ORIENTATION = MocConstant.PLAYER_MODE_PORTRAIT;

    //display category name in the channel list
    public static final boolean ENABLE_CHANNEL_LIST_CATEGORY_NAME = true;

    //press back twice to exit from player screen
    public static final boolean PRESS_BACK_TWICE_TO_CLOSE_PLAYER = false;
    public static int interCounter = 1;
    //load more pagination
    public static final int LOAD_MORE = 12;
    //looping mode
    public static final boolean ENABLE_LOOPING_MODE = true;
    public static int fontSize = 16;



}

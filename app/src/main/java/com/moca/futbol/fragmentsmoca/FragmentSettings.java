package com.moca.futbol.fragmentsmoca;


import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_GRID_3_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_LIST_DEFAULT;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_3_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_LIST_DEFAULT;
import static com.moca.futbol.helpermoca.MocConstant.PLAYER_MODE_LANDSCAPE;
import static com.moca.futbol.helpermoca.MocConstant.PLAYER_MODE_PORTRAIT;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.multidex.BuildConfig;

import com.moca.futbol.R;
import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.activitymoca.MainActivity;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;


public class FragmentSettings extends Fragment {

    View root_view;
    SharedPrefs sharedPref;
    TextView txt_current_video_list;
    TextView txt_current_category_list;
    TextView txt_current_player_mode;
    private Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPref = new SharedPrefs(activity);

        initComponent();

        return root_view;
    }

    private void initComponent() {

        txt_current_video_list = root_view.findViewById(R.id.txt_current_video_list);
        if (sharedPref.getChannelViewType() == CHANNEL_LIST_DEFAULT) {
            txt_current_video_list.setText(getResources().getString(R.string.single_choice_default));
        } else if (sharedPref.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
            txt_current_video_list.setText(getResources().getString(R.string.single_choice_grid2));
        } else if (sharedPref.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
            txt_current_video_list.setText(getResources().getString(R.string.single_choice_grid3));
        }

        txt_current_category_list = root_view.findViewById(R.id.txt_current_category_list);
        if (sharedPref.getCategoryViewType() == CATEGORY_LIST_DEFAULT) {
            txt_current_category_list.setText(getResources().getString(R.string.single_choice_list));
        } else if (sharedPref.getCategoryViewType() == CATEGORY_GRID_2_COLUMN) {
            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_2));
        } else if (sharedPref.getCategoryViewType() == CATEGORY_GRID_3_COLUMN) {
            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_3));
        }

        txt_current_player_mode = root_view.findViewById(R.id.txt_current_player_mode);
        if (sharedPref.getPlayerMode() == PLAYER_MODE_PORTRAIT) {
            txt_current_player_mode.setText(getResources().getString(R.string.player_portrait));
        } else if (sharedPref.getPlayerMode() == PLAYER_MODE_LANDSCAPE) {
            txt_current_player_mode.setText(getResources().getString(R.string.player_landscape));
        }

        changeVideoListViewType();
        changeCategoryListViewType();
        changePlayerMode();

        root_view.findViewById(R.id.btn_policy).setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MocFirstAds.policy_link))));
        root_view.findViewById(R.id.btn_rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+activity.getPackageName())));
                }
                catch (ActivityNotFoundException e){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+activity.getPackageName())));
                }
            }
        });

        if (MocFirstAds.moreapps_link.isEmpty()) {
            // The string is empty
            MocFirstAds.moreapps_link = "https://play.google.com/store/apps/details?id="+activity.getPackageName();
        }

        root_view.findViewById(R.id.btn_more).setOnClickListener(view -> startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(MocFirstAds.moreapps_link))));

        root_view.findViewById(R.id.btn_about).setOnClickListener(view -> aboutDialog());


        }


    private void changeVideoListViewType() {

        root_view.findViewById(R.id.btn_switch_list).setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_video_list);
            int itemSelected = sharedPref.getChannelViewType();
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.title_setting_list)
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
                        sharedPref.updateChannelViewType(position);

                        if (position == 0) {
                            txt_current_video_list.setText(getResources().getString(R.string.single_choice_default));
                        } else if (position == 1) {
                            txt_current_video_list.setText(getResources().getString(R.string.single_choice_grid2));
                        }

                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    private void changeCategoryListViewType() {

        root_view.findViewById(R.id.btn_switch_category).setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_category_list);
            int itemSelected = sharedPref.getCategoryViewType();
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.title_setting_category)
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
                        sharedPref.updateCategoryViewType(position);

                        if (position == 0) {
                            txt_current_category_list.setText(getResources().getString(R.string.single_choice_list));
                        } else if (position == 1) {
                            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_2));
                        } else if (position == 2) {
                            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_3));
                        }

                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("category_position", "category_position");
                        startActivity(intent);

                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    private void changePlayerMode() {

        root_view.findViewById(R.id.btn_switch_player_mode).setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_player_mode);
            int itemSelected = sharedPref.getPlayerMode();
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.title_setting_player)
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
                        sharedPref.updatePlayerMode(position);
                        if (position == 0) {
                            txt_current_player_mode.setText(getResources().getString(R.string.player_portrait));
                        } else if (position == 1) {
                            txt_current_player_mode.setText(getResources().getString(R.string.player_landscape));
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    public void aboutDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.my_custom_dialog_about, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);

        TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
        txtAppVersion.setText(getString(R.string.pobr_app_version) + " " + BuildConfig.VERSION_NAME);

        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.option_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

}
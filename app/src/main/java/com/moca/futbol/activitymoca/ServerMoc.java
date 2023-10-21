package com.moca.futbol.activitymoca;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import com.moca.futbol.R;

public class ServerMoc extends AppCompatActivity {
    ImageView myBtnClose;
    MaterialButton moreOfAppsBtn;
    LinearLayout server_parent_view;
    Activity activity ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_coming_soon);

        myBtnClose = findViewById(R.id.btn_close1);
        moreOfAppsBtn = findViewById(R.id.comingmoreapps);
        server_parent_view = findViewById(R.id.server_parentview);

        myBtnClose.setOnClickListener(v -> finish());
        moreOfAppsBtn.setOnClickListener(v -> {

            if(MocFirstAds.update_link.isEmpty()){

                MocFirstAds.moreapps_link = "https://play.google.com/store/apps/details?id="+activity.getPackageName();
            }

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MocFirstAds.moreapps_link)));
                finish();

            } catch (ActivityNotFoundException e) {
                Snackbar.make(server_parent_view, "Something went wrong please try again", Snackbar.LENGTH_LONG).show();
                throw new RuntimeException(e);
            }
        });
    }
}
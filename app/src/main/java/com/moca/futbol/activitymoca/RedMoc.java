package com.moca.futbol.activitymoca;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.moca.futbol.R;

public class RedMoc extends AppCompatActivity {

    ImageButton mybtnClose;
    MaterialButton mybtnRedirect;
    LinearLayout redir_parentview;
    Activity activity ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pobr_rederict);

        redir_parentview = findViewById(R.id.redir_parentview);
        mybtnClose = findViewById(R.id.btn_close);
        mybtnRedirect = findViewById(R.id.my_btn_redirect);

        initView();
    }

    private void initView() {

        mybtnClose.setOnClickListener(view -> finish());

        mybtnRedirect.setOnClickListener(view -> {


          if(MocFirstAds.update_link.isEmpty()){

              MocFirstAds.moreapps_link = "https://play.google.com/store/apps/details?id="+activity.getPackageName();
          }


            try {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MocFirstAds.update_link)));
                finish();

            } catch (ActivityNotFoundException e) {
                Snackbar.make(redir_parentview, "Something went wrong please try again", Snackbar.LENGTH_LONG).show();
                throw new RuntimeException(e);

            }
        });
    }
}
package cn.ruc.readio.userPageActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import cn.ruc.readio.R;

public class FaithActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faith);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ImageView muyu = findViewById(R.id.muyuButton);
//        TextView gongde = findViewById(R.id.gongde);
        muyu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation_small = AnimationUtils.loadAnimation(FaithActivity.this, R.xml.knock_small);
                Animation animation_big = AnimationUtils.loadAnimation(FaithActivity.this, R.xml.knock_big);
                muyu.startAnimation(animation_small);
                muyu.startAnimation(animation_big);
//                gongde.setVisibility(View.VISIBLE);
//                gongde.startAnimation(animation_big);
//                gongde.setVisibility(View.INVISIBLE);


            }
        });

    }
}
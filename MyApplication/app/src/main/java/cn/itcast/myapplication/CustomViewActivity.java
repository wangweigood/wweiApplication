package cn.itcast.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class CustomViewActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = findViewById(R.id.root);

        DrawView dv = new DrawView(this);
        dv.setMinimumHeight(500);
        dv.setMinimumWidth(300);

        layout.addView(dv);

    }


}

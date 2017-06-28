package cn.itcast.buttontest;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.widget.CheckBox;

import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    RadioGroup rg;

    int[] ids = new int[]{
            R.id.red,
            R.id.blue,
            R.id.green,

    };

    CheckBox[] cb = new CheckBox[3];

    TextView show;

    TextView show1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rg = (RadioGroup) findViewById(R.id.rg);

        show = (TextView) findViewById(R.id.show);
        show1 = (TextView) findViewById(R.id.show1);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                String str = checkedId == R.id.male ?
                        "您的性别是男人" : "您的性别是女人";

                show.setText(str);

            }
        });

        for ( int i = 0; i < ids.length; i++) {
            cb[i] = (CheckBox) findViewById(ids[i]);
            final int finalI = i;
            cb[i].setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        cb[finalI].setChecked(true);
                        show1.setText(""+ids[finalI]);
                    }
                    else {
                        cb[finalI].setChecked(false);
                    }
                }
            });

        }


    }

}


package com.oridev.variantsstubsgenerator.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.oridev.variantsstubsgenerator.utils.FruitUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_text1) TextView message1View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.main_button)
    public void onButtonClick( View v) {
        showFlavor1Message();
    }

    private void showFlavor1Message() {

        // get some text from flavor1
        String message1 = FruitUtils.getPulpMessage(this);
        /* It's ok if the IDE complains about this condition always being false (if your're
           on flavor orange) or always true (if your'e on flavor apple) */
        if (message1 == null) {
            // this means we called flavor1 method from flavor2
            message1 = getString(R.string.message_flavor1_failure);
        }

        message1View.setText(message1);
    }

}

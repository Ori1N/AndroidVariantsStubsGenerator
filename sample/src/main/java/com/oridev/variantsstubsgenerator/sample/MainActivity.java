package com.oridev.variantsstubsgenerator.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_text) TextView messageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        showMessage();
    }

    private void showMessage() {
        String text1 = Flavor1SpecificFunctionality.getFlavor1Message();
        if (text1 != null) {
            messageView.setText(text1);
        } else {
            messageView.setText("Received null from method, probably shouldn't use class Flavor1SpecificFunctionality from flavor1 code!");
        }

        try {
            String text2 = Flavor2SpecificFunctionality.getFlavor2MessageOrThrow();
        } catch (Exception e) {

        }
    }
}

package com.oridev.variantsstubsgenerator.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_text1) TextView message1View;
    @BindView(R.id.main_text2) TextView message2View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.main_button)
    public void onButtonClick(View v) {
        showMessages();
    }

    private void showMessages() {
        String message1, message2;

        // get some text from flavor1
        message1 = Flavor1SpecificFunctionality.getFlavor1Message();
        if (message1 == null) {
            // this means we called flavor1 method from flavor2
            message1 = "Got null, probably shouldn't have called flavor1 method from flavor2...";
        }

        // get some text from flavor2
        try {
            message2 = Flavor2SpecificFunctionality.getFlavor2MessageOrThrow();
        } catch (RuntimeException e) {
            // this means we called flavor2 method from flavor1 and specified to throw exception if trying to use stubs
            message2 = "Got exception... good thing it's not on production yet...";
        }

        message1View.setText(message1);
        message2View.setText(message2);
    }

}

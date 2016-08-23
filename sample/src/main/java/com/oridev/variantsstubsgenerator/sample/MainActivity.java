package com.oridev.variantsstubsgenerator.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.oridev.variantsstubsgenerator.sample.Flavor2SpecificFunctionality;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_text1) TextView message1View;
    @BindView(R.id.main_text2) TextView message2View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        showMessages();
    }

    private void showMessages() {
        String message1, message2;

        String text1 = Flavor1SpecificFunctionality.getFlavor1Message();
        if (text1 != null) {
            message1 = text1;
        } else {
            // this means we called flavor1 method from flavor2
            message1 = "Got null, probably shouldn't have called flavor1 method from flavor2...";
        }

        try {
            message2 = Flavor2SpecificFunctionality.getFlavor2MessageOrThrow();
        } catch (Exception e) {
            // this means we called flavor2 method from flavor1 and specified to throw exception if trying to use stubs
            message2 = "Got exception... good thing it's not in production yet...";
        }

        message1View.setText(message1);
        message2View.setText(message2);
    }
}

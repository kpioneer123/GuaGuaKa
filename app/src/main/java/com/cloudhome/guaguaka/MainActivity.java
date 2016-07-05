package com.cloudhome.guaguaka;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cloudhome.guaguaka.view.GuaGuaKa;

public class MainActivity extends AppCompatActivity {

    private GuaGuaKa mguaguaka;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mguaguaka = (GuaGuaKa)findViewById(R.id.guaguaka);

        mguaguaka.setOnGuaGuaKaCompleteListener(new GuaGuaKa.OnGuaGuaKaCompleteListener() {
                                                    @Override
                                                    public void onComplete() {

                                                        Toast.makeText(getApplicationContext(),"用户已经刮得差不多",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
        );

        mguaguaka.setText("Android新技能Get");
    }
}

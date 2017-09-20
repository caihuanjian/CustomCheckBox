package com.rain.customcheckbox;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rain.customcheckbox.views.CustomCheckBox;

public class MainActivity extends AppCompatActivity {

    private CustomCheckBox customCheckBox1, customCheckBox2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customCheckBox1 = (CustomCheckBox) findViewById(R.id.checkbox1);
        customCheckBox2 = (CustomCheckBox) findViewById(R.id.checkbox2);
        customCheckBox1.setOnCheckChangedListener(new CustomCheckBox.onCheckChangedListener() {
            @Override
            public void onCheckChange(CustomCheckBox checkBox, boolean isChecked) {
                Toast.makeText(MainActivity.this, "checkbox1 " + (isChecked ? " checked " : "unchecked"), Toast.LENGTH_SHORT).show();
            }
        });
        customCheckBox2.setOnCheckChangedListener(new CustomCheckBox.onCheckChangedListener() {
            @Override
            public void onCheckChange(CustomCheckBox checkBox, boolean isChecked) {
                Toast.makeText(MainActivity.this, "checkbox2 " + (isChecked ? " checked " : "unchecked"), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

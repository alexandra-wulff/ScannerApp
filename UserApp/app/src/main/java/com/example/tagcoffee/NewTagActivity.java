package com.example.tagcoffee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class NewTagActivity extends AppCompatActivity {
    private EditText mTagName_editTxt;
    private Spinner mCoffee_categories_spinner;

    private Button mAdd_btn;
    private Button mBack_btn;
    private Button mScan_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag);

        mTagName_editTxt = findViewById(R.id.tagName_editTxt);
        mCoffee_categories_spinner = findViewById(R.id.coffee_categories_spinner);
        mAdd_btn = findViewById(R.id.add_btn);
        mBack_btn = findViewById(R.id.back_btn);
        mScan_btn = findViewById(R.id.scan_btn);

        mAdd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NfcTag nfcTag = new NfcTag();
                nfcTag.setTagName(mTagName_editTxt.getText().toString());
                nfcTag.setPoints("0");
                nfcTag.setDateTime(nfcTag.getDateTime());
                nfcTag.setCoffeeCategory(mCoffee_categories_spinner.getSelectedItem().toString());

                new FirebaseDatabaseHelper().addTag(nfcTag, new FirebaseDatabaseHelper.DataStatus() {
                    @Override
                    public void DataIsLoaded(List<NfcTag> nfcTags, List<String> keys) { }

                    @Override
                    public void DataIsInserted() {
                        Toast.makeText(NewTagActivity.this, "NfcTag has been saved successfully!",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void DataIsUpdated(){}

                    @Override
                    public void DataIsDeleted() { }
                });
            }
        });

        mBack_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

        mScan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTagActivity.this, TagScannerActivity.class);
                startActivity(intent);
            }
        });
    }
}

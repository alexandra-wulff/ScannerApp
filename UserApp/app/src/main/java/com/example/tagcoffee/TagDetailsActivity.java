package com.example.tagcoffee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TagDetailsActivity extends AppCompatActivity {

    private String TAG_LOG = "TAG_LOG";

    private EditText mTagName;
    private Spinner mCategory_spinner;

    private Button mUpdatebtn;
    private Button mDeletebtn;
    private Button mBackbtn;

    private String key;
    private String tagName;
    private String category;

    private FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private DatabaseReference mReferenceUserTag;
    private FirebaseDatabase mDatabase;
    private String mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_details);

        mFirebaseDatabaseHelper = new FirebaseDatabaseHelper();
        mUserId = mFirebaseDatabaseHelper.getUser().getUid();
        mDatabase = mFirebaseDatabaseHelper.getmDatabase();
        mReferenceUserTag = mDatabase.getReference("users/" + mUserId);

        key = getIntent().getStringExtra("key");
        tagName = getIntent().getStringExtra("tagName");
        category = getIntent().getStringExtra("category");

        mTagName = findViewById(R.id.tagName_editTxt);
        mTagName.setText(tagName);

        mCategory_spinner = findViewById(R.id.coffee_categories_spinner);
        mCategory_spinner.setSelection(getIndex_SpinnerItem(mCategory_spinner, category));

        mUpdatebtn = findViewById(R.id.update_btn);
        mDeletebtn = findViewById(R.id.delete_btn);
        mBackbtn = findViewById(R.id.back_btn);

        mUpdatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTagInfo(key);
            }
        });

        mDeletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FirebaseDatabaseHelper().deleteTag(key, new FirebaseDatabaseHelper.DataStatus() {
                    @Override
                    public void DataIsLoaded(List<NfcTag> nfcTags, List<String> keys) {

                    }

                    @Override
                    public void DataIsInserted() {

                    }

                    @Override
                    public void DataIsUpdated() {

                    }

                    @Override
                    public void DataIsDeleted() {
                        Toast.makeText(TagDetailsActivity.this, "This tag has been deleted", Toast.LENGTH_LONG).show();
                        finish();
                        return;

                    }
                });
            }
        });

        mBackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

    }


    private void updateTagInfo(final String tagKey) {
        mReferenceUserTag.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean tagExists = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot tagChild : dataSnapshot.getChildren()) {
                    NfcTag nfcTag = tagChild.getValue(NfcTag.class);
                    if (tagKey.equals(tagChild.getKey())) {
                        Log.d(TAG_LOG, "Key found in database " + tagKey);
                        tagExists = true;
                        nfcTag.setPoints(nfcTag.getPoints());
                        nfcTag.setDateTime(nfcTag.getDateTime());
                        nfcTag.setTagName(mTagName.getText().toString());
                        nfcTag.setCoffeeCategory(mCategory_spinner.getSelectedItem().toString());
                        nfcTag.setId(key);
                        mReferenceUserTag.child(nfcTag.getId()).setValue(nfcTag);
                        Log.d(TAG_LOG, "Tag: " + tagKey + " info has been updated!");
                    }
                }
                Toast.makeText(TagDetailsActivity.this, "Tag info has been updated", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    private int getIndex_SpinnerItem(Spinner spinner, String item) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(item)) {
                index = i;
                break;
            }
        }
        return index;
    }
}

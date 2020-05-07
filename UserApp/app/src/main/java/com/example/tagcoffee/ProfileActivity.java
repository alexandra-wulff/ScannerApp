package com.example.tagcoffee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileActivity extends AppCompatActivity {

    private String TAG_LOG = "TAG_LOG";

    private FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private DatabaseReference mReferenceUserTag;
    private FirebaseDatabase mDatabase;
    private String mUserId;

    private EditText mUserName;
    private String userName;

    private TextView mLevelView;
    private TextView mTotalPointScoreView;
    private TextView mRankMonthView;
    private TextView mRankSemesterView;
    private TextView mRankAllTimeView;
    private ImageView mImageLevelView;

    private Button mProfileHistoryButton;
    private int mPointsToDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mFirebaseDatabaseHelper = new FirebaseDatabaseHelper();
        mUserId = mFirebaseDatabaseHelper.getUser().getUid();
        mDatabase = mFirebaseDatabaseHelper.getmDatabase();
        mReferenceUserTag = mDatabase.getReference("users/" + mUserId);

        mUserName = findViewById(R.id.userName_editTxt);
        userName = getIntent().getStringExtra("name");
        mUserName.setText(userName);

        mTotalPointScoreView = findViewById(R.id.userTotalPoints_txtView);
        mLevelView = findViewById(R.id.level_txtView);
        mImageLevelView = findViewById(R.id.imageLevel);
        mRankMonthView = findViewById(R.id.profileRankMonth_txtView);
        mRankSemesterView = findViewById(R.id.profileRankSemester_txtView);
        mRankAllTimeView = findViewById(R.id.profileRankAllTime_txtView);

        countTotalPoints();

        mProfileHistoryButton = findViewById(R.id.profileHistoryButton_btn);
        mProfileHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


    private void countTotalPoints() {
        mReferenceUserTag.addListenerForSingleValueEvent(new ValueEventListener() {
            int totalPoints = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG_LOG, "countTotalPoints inside onDataChange");
                for (DataSnapshot tagChild : dataSnapshot.getChildren()) {

                    NfcTag nfcTag = tagChild.getValue(NfcTag.class);
                    int pointToAdd = Integer.parseInt(nfcTag.getPoints());
                    totalPoints += pointToAdd;

                    Log.d(TAG_LOG, "Tag point: " + pointToAdd);
                }
                Log.d(TAG_LOG, "Total user points: " + totalPoints);

                mPointsToDisplay = totalPoints;
                setLevelInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setTotalPoints(int points) {
        mPointsToDisplay = points;
        mTotalPointScoreView.setText("Point score " + points);
        Log.d(TAG_LOG, "mPointsToDisplay " + mPointsToDisplay);
    }


    private void setLevelInfo() {
        int points = mPointsToDisplay;

        if (points == 0) {
            mImageLevelView.setImageResource(R.drawable.planet);
            mLevelView.setText("Level 1: Planet");
            mRankMonthView.setText("-");
            mRankSemesterView.setText("-");
            mRankAllTimeView.setText("-");
            setTotalPoints(points);

        } else if (points >= 1 && points < 5) {
            mImageLevelView.setImageResource(R.drawable.seed);
            mLevelView.setText("Level 2: Seed");
            mRankMonthView.setText("40th");
            mRankSemesterView.setText("18th");
            mRankAllTimeView.setText("35th");
            setTotalPoints(points);

        } else if (points >= 5 && points < 10) {
            mImageLevelView.setImageResource(R.drawable.two_leaves);
            mLevelView.setText("Level 3: Sprout");
            mRankMonthView.setText("30th");
            mRankSemesterView.setText("14th");
            mRankAllTimeView.setText("25th");
            setTotalPoints(points);

        } else if (points >= 10 && points < 15) {
            mImageLevelView.setImageResource(R.drawable.four_leaves);
            mLevelView.setText("Level 4: Toddler Tree");
            mRankMonthView.setText("20th");
            mRankSemesterView.setText("9th");
            mRankAllTimeView.setText("20th");
            setTotalPoints(points);

        } else if (points >= 15) {
            mImageLevelView.setImageResource(R.drawable.tree);
            mLevelView.setText("Level 5: Teen Tree");
            mRankMonthView.setText("2nd");
            mRankSemesterView.setText("2nd");
            mRankAllTimeView.setText("5th");
            setTotalPoints(points);
        }
    }

}

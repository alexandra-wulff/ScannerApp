package com.example.tagcoffee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mRecyclerView = findViewById(R.id.recyclerView_tag);

        new FirebaseDatabaseHelper().readTags(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<NfcTag> nfcTags, List<String> keys) {
                new RecyclerView_Config().setConfig(mRecyclerView, MainActivity.this, nfcTags, keys);
            }

            @Override
            public void DataIsInserted() {
            }

            @Override
            public void DataIsUpdated() {
            }

            @Override
            public void DataIsDeleted() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (new FirebaseDatabaseHelper().getUser() != null) {
            mRecyclerView = findViewById(R.id.recyclerView_tag);
            new FirebaseDatabaseHelper().readTags(new FirebaseDatabaseHelper.DataStatus() {
                @Override
                public void DataIsLoaded(List<NfcTag> nfcTags, List<String> keys) {
                    new RecyclerView_Config().setConfig(mRecyclerView, MainActivity.this, nfcTags, keys);
                }

                @Override
                public void DataIsInserted() {
                }

                @Override
                public void DataIsUpdated() {
                }


                @Override
                public void DataIsDeleted() {
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        FirebaseUser user = mAuth.getCurrentUser();
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        if (user != null) {
            menu.getItem(0).setVisible(false);   // LOGIN / REGISTER
            menu.getItem(1).setVisible(true);    // LOGOUT
            menu.getItem(2).setVisible(true);    // SCAN NEW TAG
            menu.getItem(3).setVisible(true);    // PROFILE
            menu.getItem(4).setVisible(true);    // HISTORY
        } else {
            Toast.makeText(this, "NOT LOGGED IN", Toast.LENGTH_LONG).show();
            menu.getItem(0).setVisible(true);    // LOGIN / REGISTER
            menu.getItem(1).setVisible(false);   // LOGOUT
            menu.getItem(2).setVisible(false);   // SCAN NEW TAG
            menu.getItem(3).setVisible(false);   // PROFILE
            menu.getItem(4).setVisible(false);   // HISTORY
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            menu.getItem(0).setVisible(false);   // LOGIN / REGISTER
            menu.getItem(1).setVisible(true);    // LOGOUT
            menu.getItem(2).setVisible(true);    // SCAN NEW TAG
            menu.getItem(3).setVisible(true);    // PROFILE
            menu.getItem(4).setVisible(true);    // HISTORY
        } else {
            menu.getItem(0).setVisible(true);    // LOGIN / REGISTER
            menu.getItem(1).setVisible(false);   // LOGOUT
            menu.getItem(2).setVisible(false);   // SCAN NEW TAG
            menu.getItem(3).setVisible(false);   // PROFILE
            menu.getItem(4).setVisible(false);   // HISTORY
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_in:
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.sign_out:
                mAuth.signOut();
                invalidateOptionsMenu();
                RecyclerView_Config.logout();
                return true;
            case R.id.scan_tag:
                startActivity(new Intent(this, TagScannerActivity.class));
                return true;
            case R.id.user_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            case R.id.user_history:
                startActivity(new Intent(this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

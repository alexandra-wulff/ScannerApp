package com.nifn.android.tagscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private static FirebaseUser user;

    private String TAG_LOG = "TAG_LOG";

    NfcAdapter mNfcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance();
        reference = mDatabase.getReference("users");
        mAuth = FirebaseAuth.getInstance();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(mNfcAdapter.EXTRA_TAG)){
            Toast.makeText(this, "NFC intent received", Toast.LENGTH_LONG).show();

            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(parcelables != null && parcelables.length >0){
                String tagContent = readTextFromMessage((NdefMessage) parcelables[0]);

                findInformationDatabase(tagContent);

            } else {
                Toast.makeText(this, "No NDEF messages found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void findInformationDatabase(final String tag){
        final MediaPlayer coinSound = MediaPlayer.create(this, R.raw.coinsound);
        final Query q = reference.orderByChild(tag);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.hasChild(tag)){
                        String tagKey = ds.child(tag).getKey();
                        Long tagVal = Long.parseLong(ds.child(tag).child("points").getValue().toString());
                        String newTagVal = Long.toString(updatePoints(tagVal));
                        reference.child(ds.getKey()).child(tagKey).child("points").setValue(newTagVal)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG_LOG, "SUCCESSFUL UPDATE OF TAGPOINTS");
                                        coinSound.start();
                                    }
                                });
                        Log.d(TAG_LOG, "key " + tagKey + ", before value " + tagVal + ", now value " + newTagVal);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private long updatePoints(long currentPoints){
        long updatePoints = currentPoints + 1;
        Log.d(TAG_LOG, "Points updated, before: " + currentPoints +", after: " + updatePoints);
        return updatePoints;
    }


    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableForegroundDispatchSystem();
    }

    private void enableForegroundDispatchSystem(){
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};

        if(mNfcAdapter!=null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
        }

    }

    private void disableForegroundDispatchSystem(){
        mNfcAdapter.disableForegroundDispatch(this);
    }

    public String getTextFromNdefRecord(NdefRecord ndefRecord){
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize +1, payload.length - languageSize -1 , textEncoding);

        } catch (UnsupportedEncodingException e){
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

    private String readTextFromMessage(NdefMessage ndefMessage){
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if(ndefRecords != null && ndefRecords.length>0){
            NdefRecord ndefRecord = ndefRecords[0];
            String tagContent = getTextFromNdefRecord(ndefRecord);
            return tagContent;
        } else {
            Toast.makeText(this, "No NDEF records found!", Toast.LENGTH_LONG).show();
            return null;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        FirebaseUser user = mAuth.getCurrentUser();
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        if (user != null) {
            menu.getItem(0).setVisible(false);  //LOGIN / REGISTER
            menu.getItem(1).setVisible(true);   //Log-out
        } else {
            Toast.makeText(this, "NOT LOGGED IN", Toast.LENGTH_LONG).show();
            menu.getItem(0).setVisible(true);   //LOGIN / REGISTER
            menu.getItem(1).setVisible(false);  //Log-out
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            menu.getItem(0).setVisible(false); // LOGIN / REGISTER
            menu.getItem(1).setVisible(true);  // Log-out
        } else {
            menu.getItem(0).setVisible(true);  // LOGIN / REGISTER
            menu.getItem(1).setVisible(false); // Log-out
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_in:
                startActivity(new Intent(this, SignInActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}

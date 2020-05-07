package com.example.tagcoffee;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class TagScannerActivity extends AppCompatActivity {

    private String TAG_LOG = "TAG_LOG";

    private NfcAdapter mNfcAdapter;
    private TextView mHeadline;
    private ImageView mScanCircleImage;
    private ProgressBar mProgressBar;
    private Button mBack_btn;

    private FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private String mUserId;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReferenceUserTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_scanner);

        onResume();

        mFirebaseDatabaseHelper = new FirebaseDatabaseHelper();
        mUserId = mFirebaseDatabaseHelper.getUser().getUid();
        mDatabase = mFirebaseDatabaseHelper.getmDatabase();
        mReferenceUserTag = mDatabase.getReference("users/" + mUserId);

        mNfcAdapter = mNfcAdapter.getDefaultAdapter(this);
        mHeadline = findViewById(R.id.scanHeadline_TxtView);
        mScanCircleImage = findViewById(R.id.scanCircleImage_imgView);
        mProgressBar = findViewById(R.id.scan_progressBar);
        mBack_btn = findViewById(R.id.back_btn);

        mBack_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
                finish();
                return;
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null && parcelables.length > 0) {
                String currentTagId = readTextFromMessage((NdefMessage) parcelables[0]); //currentTagId is == tagKey
                validateTag(currentTagId, tag); // check if currentTagId exist in data base, if true update points +=1
            } else {
                NfcTag tagToAdd = createNewTag(tag); //empty tag, and thus no tagId
                mReferenceUserTag.child(tagToAdd.getId()).setValue(tagToAdd);
                Log.e(TAG_LOG, "Tag scanned is empty. New Tag created and added to database");
            }
        }
    }

    //Method reminds of updateTagInfo() from Tag Details (could be refactored to seperate method in FirebaseHelper)
    private void validateTag(final String currentTagId, final Tag tag) {
        mReferenceUserTag.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean tagExists = false;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tagChild : dataSnapshot.getChildren()) {
                    NfcTag nfcTag = tagChild.getValue(NfcTag.class);
                    if (currentTagId.equals(tagChild.getKey())) {
                        tagExists = true;
                        Log.d(TAG_LOG, "Bingo! Key found in database " + currentTagId);
                        updatePoint(nfcTag);
                        Toast.makeText(TagScannerActivity.this, "Tag recognised!", Toast.LENGTH_LONG).show();
                        break;
                    }

                }
                if (!tagExists) {
                    Log.d(TAG_LOG, "Tag is not in database" + currentTagId);
                    NfcTag tagToAdd = createNewTag(tag, currentTagId);
                    mReferenceUserTag.child(currentTagId).setValue(tagToAdd);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //When tag is not empty
    private NfcTag createNewTag(Tag tag, String tagId) {
        NdefMessage newMessage = createNdefMessage(tagId + "");
        writeNdefMessage(tag, newMessage);
        NfcTag newNfcTag = new NfcTag();
        newNfcTag.setTagName("Enter name");
        newNfcTag.setCoffeeCategory("Select coffee");
        newNfcTag.setPoints("0");
        newNfcTag.setId(tagId);

        return newNfcTag;
    }

    //When tag is completely empty
    private NfcTag createNewTag(Tag tag) {
        String tagId = mReferenceUserTag.push().getKey();
        NdefMessage newMessage = createNdefMessage(tagId + "");
        writeNdefMessage(tag, newMessage);
        NfcTag newNfcTag = new NfcTag();
        newNfcTag.setTagName("Enter name");
        newNfcTag.setCoffeeCategory("Select coffee");
        newNfcTag.setPoints("0");
        newNfcTag.setId(tagId);

        return newNfcTag;
    }

    private void updatePoint(NfcTag nfcTag) {
        String point = nfcTag.getPoints() == null ? "0" : nfcTag.getPoints();
        int points = Integer.parseInt(point) + 1;
        String updatedPoints = Integer.toString(points);
        nfcTag.setPoints(updatedPoints);
        mReferenceUserTag.child(nfcTag.getId()).setValue(nfcTag); //update points
        Toast.makeText(this, "1 point added to score :)", Toast.LENGTH_SHORT).show();

    }

    private void enableForegroundDispatchSystem() {
        Intent intent = new Intent(this, TagScannerActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
        }
    }

    private void disableForegroundDispatchSystem() {
        mNfcAdapter.disableForegroundDispatch(this);
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


    private void formatTag(Tag tag, NdefMessage ndefMessage) {
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if (ndefFormatable == null) {
                Log.e(TAG_LOG, "NfcTag is not Ndef formatable:(");
                return;
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();

        } catch (Exception e) {
            Log.e(TAG_LOG, e.getMessage());
        }

    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage) {
        try {
            if (tag == null) {  // checks if nfcTag is empty
                Toast.makeText(this, "NfcTag object cannot be null", Toast.LENGTH_LONG).show();
                return;
            }
            Ndef ndef = Ndef.get(tag);  // format nfcTag with Ndef format and write NdefMessage

            if (ndef == null) {
                formatTag(tag, ndefMessage);
            } else {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Log.e(TAG_LOG, "NfcTag is not writeable");
                    ndef.close();
                    return;
                }
                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Log.e(TAG_LOG, "NfcTag is written ");
                Log.e(TAG_LOG, "NfcTag write" + ndefMessage);
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "writeNdefMessage" + e.getMessage());
        }
    }

    private NdefRecord createTextRecord(String content) {
        try {
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");
            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);
            payload.write((byte) (languageSize & 0x1F));
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG_LOG, "createTextRecord" + e.getMessage());
        }
        return null;
    }

    private NdefMessage createNdefMessage(String content) {
        NdefRecord ndefRecord = createTextRecord(content);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
        return ndefMessage;
    }

    private String getTextFromNdefRecord(NdefRecord ndefRecord) {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG_LOG, "getTextFromNdefRecord" + e.getMessage());
        }
        return tagContent;

    }

    private String readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords != null && ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];
            String tagContent = getTextFromNdefRecord(ndefRecord);
            return tagContent;
        } else {
            Toast.makeText(this, "No Ndef records found", Toast.LENGTH_LONG).show();
            return null;
        }
    }
}

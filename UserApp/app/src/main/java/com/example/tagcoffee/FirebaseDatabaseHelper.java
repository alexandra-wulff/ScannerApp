package com.example.tagcoffee;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseHelper {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReferenceTags;
    private List<NfcTag> nfcTags = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public interface DataStatus {
        void DataIsLoaded(List<NfcTag> nfcTags, List<String> keys);

        void DataIsInserted();

        void DataIsUpdated();

        void DataIsDeleted();
    }

    public FirebaseDatabase getmDatabase() {
        return mDatabase;
    }

    public FirebaseUser getUser() {
        return mUser;
    }


    public FirebaseDatabaseHelper() {
        mDatabase = FirebaseDatabase.getInstance();
        mReferenceTags = mDatabase.getReference("users");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    public void readTags(final DataStatus dataStatus) {
        if (mUser != null) {
            String userId = getUser().getUid();
            DatabaseReference mReferenceUsersTags = mDatabase.getReference("users").child(userId);
            mReferenceUsersTags.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    nfcTags.clear();
                    List<String> keys = new ArrayList<>();
                    for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                        keys.add(keyNode.getKey());
                        NfcTag nfcTag = keyNode.getValue(NfcTag.class);
                        nfcTags.add(nfcTag);
                    }
                    dataStatus.DataIsLoaded(nfcTags, keys);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else if (mUser == null) {
            mReferenceTags.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    nfcTags.clear();
                    List<String> keys = new ArrayList<>();
                    for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                        keys.add(keyNode.getKey());
                        NfcTag nfcTag = keyNode.getValue(NfcTag.class);
                        nfcTags.add(nfcTag);
                    }
                    dataStatus.DataIsLoaded(nfcTags, keys);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void addTag(NfcTag nfcTag, final DataStatus dataStatus) {
        String userId = getUser().getUid();
        DatabaseReference mReferenceUserTag = mDatabase.getReference("users/" + userId);

        String key = mReferenceUserTag.push().getKey();
        nfcTag.setId(key); //newly added
        mReferenceTags.child(userId).child(key).setValue(nfcTag).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dataStatus.DataIsInserted();
            }
        });
    }

    public void deleteTag(String key, final DataStatus dataStatus) {
        String userId = getUser().getUid();
        mReferenceTags.child(userId).child(key).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dataStatus.DataIsDeleted();
            }
        });
    }


}

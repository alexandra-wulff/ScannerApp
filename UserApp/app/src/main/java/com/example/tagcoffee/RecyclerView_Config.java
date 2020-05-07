package com.example.tagcoffee;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RecyclerView_Config {

    private FirebaseAuth mAuth;
    private static FirebaseUser mUser;

    private Context mContext;
    private TagAdapter mTagAdapter;


    public void setConfig(RecyclerView recyclerView, Context context, List<NfcTag> nfcTags, List<String> keys) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mContext = context;
        mTagAdapter = new TagAdapter(nfcTags, keys);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mTagAdapter);

    }

    class TagItemView extends RecyclerView.ViewHolder {
        private TextView mTagName;
        private TextView mDateTime;
        private TextView mPoints;
        private TextView mCategory;
        private String key;

        public TagItemView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.tag_list_item, parent, false));

            mTagName = itemView.findViewById(R.id.tagName_txtView);
            mPoints = itemView.findViewById(R.id.points_txtView);
            mDateTime = itemView.findViewById(R.id.dateTime_txtView);
            mCategory = itemView.findViewById(R.id.category_txtView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUser != null) {
                        Intent intent = new Intent(mContext, TagDetailsActivity.class);
                        intent.putExtra("key", key);
                        intent.putExtra("tagName", mTagName.getText().toString());
                        intent.putExtra("category", mCategory.getText().toString());
                        mContext.startActivity(intent);

                    } else {
                        mContext.startActivity(new Intent(mContext, SignInActivity.class));
                    }

                }
            });
        }

        public void bind(NfcTag nfcTag, String key) {
            mTagName.setText(nfcTag.getTagName());
            mPoints.setText(nfcTag.getPoints() + " points");
            mDateTime.setText("Date: " + nfcTag.getDateTime());
            mCategory.setText("Coffee: " + nfcTag.getCoffeeCategory());
            this.key = key;
        }
    }

    class TagAdapter extends RecyclerView.Adapter<TagItemView> {
        private List<NfcTag> mTaglist;
        private List<String> mKeys;

        public TagAdapter(List<NfcTag> taglist, List<String> keys) {
            this.mTaglist = taglist;
            this.mKeys = keys;
        }

        @NonNull
        @Override
        public TagItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TagItemView(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TagItemView holder, int position) {
            holder.bind(mTaglist.get(position), mKeys.get(position));

        }

        @Override
        public int getItemCount() {
            return mTaglist.size();
        }
    }

    public static void logout() {
        mUser = null;
    }

}

package com.sulitous.mtc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.WaitingView> implements EventListener<DocumentSnapshot> {

    private final List<Child> mWaitingList;
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final FirebaseFirestore mRootRef;
    private final int i;
    private int childType = 0;

    ChildAdapter(Context context, FirebaseFirestore rootRef, int i) {
        this.mInflater = LayoutInflater.from(context);
        this.mWaitingList = new ArrayList<>();
        this.mContext = context;
        this.mRootRef = rootRef;
        this.i = i;
    }

    @Override
    public WaitingView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chlid_list_item, parent, false);
        return new WaitingView(mContext,view,mRootRef);
    }

    @Override
    public void onBindViewHolder(@NonNull WaitingView holder, int position) {
        holder.bindToView(mWaitingList.get(position),i,childType);
        holder.setClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (i == 0) {
                    if (isLongClick) {
                        Child child = mWaitingList.get(position);
                        Gson gson = new Gson();
                        String SChild = gson.toJson(child);
                        Intent editChildIntent = new Intent(mContext, AddChildActivity.class);
                        editChildIntent.putExtra("CHILD", SChild);
                        mContext.startActivity(editChildIntent);
                    } else {
                        mWaitingList.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWaitingList.size();
    }

    public void clear() {
        mWaitingList.clear();
    }

    public void getChildType(int i){
        this.childType = i;
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e == null) {
            if (documentSnapshot.getData() != null) {
                for (String key : documentSnapshot.getData().keySet()) {
                    mRootRef.collection("ChildDetails").document(key).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String key = documentSnapshot.getId();
                            Child child = documentSnapshot.toObject(Child.class);
                            assert child != null;
                            child.setKey(key);
                            mWaitingList.add(0, child);
                            notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }else {
            Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public class WaitingView extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        private final TextView mChildName,mChildFather,mChildAge,mChildPhone;
        private final FirebaseFirestore mRootRef;
        private String key;
        private final Context mContext;
        private RecyclerItemClickListener recyclerItemClickListener;
        private ImageView mSendChildView;
        private View mRootView;

        WaitingView(final Context context, View itemView, FirebaseFirestore rootRef) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            this.mRootRef = rootRef;
            this.mContext  = context;
            mChildName = itemView.findViewById(R.id.name);
            mChildFather = itemView.findViewById(R.id.father);
            mChildAge = itemView.findViewById(R.id.age);
            mChildPhone = itemView.findViewById(R.id.phone);
            mSendChildView = itemView.findViewById(R.id.send_child);
            mSendChildView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transferToUnderTreatment(context);
                }
            });
            mRootView = itemView.findViewById(R.id.list_layout);
        }

        private void transferToUnderTreatment(Context context) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Are Your Sure");
            alertDialogBuilder
                    .setMessage("Do you want to send this child to MTC")
                    .setCancelable(false)
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.shared_file_user), Context.MODE_PRIVATE);
                            String uid = sharedPreferences.getString(mContext.getString(R.string.uid_key_shared),"");
                            String center = sharedPreferences.getString(mContext.getString(R.string.centre_key_shared),"");
                            Map<String, Object> treatmentHashMap = new HashMap<>();
                            treatmentHashMap.put("treatment",1);
                            treatmentHashMap.put("transferred",FieldValue.serverTimestamp());
                            mRootRef.collection("ChildDetails").document(key).set(treatmentHashMap, SetOptions.merge());
                            Map<String, Object> pushHashMap = new HashMap<>();
                            pushHashMap.put(key, FieldValue.serverTimestamp());
                            mRootRef.collection("Treatment").document(uid).set(pushHashMap,SetOptions.merge());
                            mRootRef.collection("UnderTreatment").document(center).set(pushHashMap,SetOptions.merge());
                            Map<String,Object> deleteHashMap = new HashMap<>();
                            deleteHashMap.put(key, FieldValue.delete());
                            mRootRef.collection("WaitingList").document(uid).update(deleteHashMap);
                            recyclerItemClickListener.onClick(mSendChildView, getAdapterPosition(), false);
                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        void bindToView(Child child,int i,int childType) {
            key = child.getKey();
            mChildName.setText(child.getName());
            mChildFather.setText(child.getFather());
            mChildAge.setText(String.valueOf(child.getAge()));
            mChildPhone.setText(child.getPhone());
            if (i == 0){
                mSendChildView.setVisibility(View.VISIBLE);
            }else if (i == 1){
                mSendChildView.setVisibility(View.GONE);
                if (child.getTreatment() == 1){
                    mRootView.setBackgroundResource(R.drawable.red);
                }else if (child.getTreatment() == 2){
                    mRootView.setBackgroundResource(R.drawable.yellow);
                }else if (child.getTreatment() == 3) {
                    mRootView.setBackgroundResource(R.drawable.green);
                }
            }
            viewList(child,childType);
        }

        private void viewList(Child child, int childType) {
            switch (childType) {
                case 0:

                    break;
                case 1:
                    if (child.getTreatment() != 1) {
                        mRootView.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    if (child.getTreatment() != 2) {
                        mRootView.setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    if (child.getTreatment() != 3) {
                        mRootView.setVisibility(View.GONE);
                    }
                    break;
            }
        }

        void setClickListener(RecyclerItemClickListener recyclerItemClickListener) {
            this.recyclerItemClickListener = recyclerItemClickListener;
        }

        @Override
        public boolean onLongClick(View v) {
            recyclerItemClickListener.onClick(v, getAdapterPosition(), true);
            return true;
        }
    }
}

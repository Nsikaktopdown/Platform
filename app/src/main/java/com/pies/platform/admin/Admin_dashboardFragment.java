package com.pies.platform.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pies.platform.Login;
import com.pies.platform.Notification;
import com.pies.platform.R;
import com.pies.platform.admin.model.AdminAdapter;
import com.pies.platform.admin.model.Admin_Item;
import com.pies.platform.admin.model.Admin_data;
import com.pies.platform.admin.model.imageData;
import com.pies.platform.custom.DividerItemDecoration;
import com.pies.platform.custom.RoundFormation;
import com.pies.platform.model_users.Users;
import com.pies.platform.teachersActivity.Assignment;
import com.pies.platform.teachersActivity.Teachers_Dashboard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class Admin_dashboardFragment extends Fragment {
    private List<Admin_Item> movieList = new ArrayList<>();
    private RecyclerView recyclerView2;
    private AdminAdapter mAdapter;
    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]
    FirebaseAuth mFirebaseAuth;
    private static final String TAG = "ManagerHome";
    private FirebaseUser mFirebaseUser;
    private String aNam, teacherEmail;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView tName, tEmail;
    private String url;
    private String userId, thumb_url;
    DatabaseReference myRootref = FirebaseDatabase.getInstance().getReference();
    private ImageView adminImage;

    ProgressDialog progressDialog;
    public Admin_dashboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View root = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        adminImage = (ImageView) root.findViewById(R.id.admin_image);
        tName = (TextView) root.findViewById(R.id.admin_name);
        tEmail = (TextView) root.findViewById(R.id.admin_email);


        AdView mAdView = (AdView) root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser == null) {
                    // User is signed in
//                    Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
                    startActivity(new Intent(getActivity(), Login.class));
                }
                else{ String n = mFirebaseUser.getEmail();
                    tEmail.setText(n);
                   // Toast.makeText(getActivity(), mFirebaseUser.getUid().toString(), Toast.LENGTH_SHORT).show();
                    String url = mFirebaseUser.getUid().toString();
                    checkUser(url);
                    if( mFirebaseUser.getPhotoUrl() != null){
                        String thumb = mFirebaseUser.getPhotoUrl().toString();
                        setprofileImage(thumb);
                    }
                    if(mFirebaseUser == null){
                        profileUpdate(aNam);
                        String nM = mFirebaseUser.getDisplayName().toString();
                        tName.setText(aNam);

                    }
                    else if (mFirebaseUser.getDisplayName() != null){
                        String m = mFirebaseUser.getDisplayName().toString();
                        tName.setText(aNam);
                    }

                }
            }};

        CardView cardView = (CardView) root.findViewById(R.id.layer1);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),Admin_profile.class));
            }
        });

     // setprofileImage(thumb_url);
        mFirebaseAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Verifying...");

        progressDialog.setIndeterminate(false);
        recyclerView2 = (RecyclerView) root.findViewById(R.id.recycler_admin);
        mAdapter = new AdminAdapter(movieList);
       // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), 2);
        recyclerView2.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView2.setItemAnimator(new DefaultItemAnimator());
        recyclerView2.setHasFixedSize(true);
        recyclerView2.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        setHasOptionsMenu(true);
        recyclerView2.setAdapter(mAdapter);
        recyclerView2.addOnItemTouchListener(new Admin_dashboard.RecyclerTouchListener(getActivity(), recyclerView2, new Admin_dashboard.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                if(position == 0){
                    startActivity(new Intent(getActivity(), Create_Account.class));
                   // myRootref.child("all-users").setValue("meeeeeee");
                }
                else if(position == 1){
                    startActivity(new Intent(getActivity(), Assignment.class));
                }
                else if(position == 2){
                    startActivity(new Intent(getActivity(), MapsActivity.class));
                }
                else  if(position ==3){
                    startActivity(new Intent(getActivity(), Notification.class));
                }
                //Toast.makeText(getApplicationContext(), movie.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

prepareMovieData();

        return root;
    }

    private void prepareMovieData() {
        Admin_Item manager = new Admin_Item("Registration", "12","adding new personnel", getResources().getDrawable(R.drawable.ic_person_add_black_24dp));
        movieList.add(manager);

        manager = new Admin_Item("Assign a Task","", "task to be assign", getResources().getDrawable(R.drawable.assign_icon));
        movieList.add(manager);

        manager = new Admin_Item("New Home","", "registration of new homes", getResources().getDrawable(R.drawable.home_icon));
        movieList.add(manager);
        manager = new Admin_Item("Push Notification","", "Send notification messages to all users", getResources().getDrawable(R.drawable.ic_message_black_24dp));
        movieList.add(manager);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();


    }
    public  void setprofileImage(String imageurl){
        Picasso.with(getActivity())
                .load(imageurl)
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .error(R.drawable.ic_account_circle_black_24dp)
                .transform(new RoundFormation(50, 20))
                //.resizeDimen(R.dimen.list_detail_image_size, R.dimen.list_detail_image_size)
                .into(adminImage);
    }
    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void checkUser(String uid) {

        mDatabase.child("admin-Profile").child(uid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

     Admin_data item = dataSnapshot.getValue(Admin_data.class);
                if(item.getName() != null){
                    aNam = item.getName();
                   // Toast.makeText(getActivity(), aNam, Toast.LENGTH_SHORT).show();
                    tName.setText(aNam);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getActivity(), "url not founded", Toast.LENGTH_SHORT).show();
            }
        });
    }


public  void profileUpdate(String username){
    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build();

    mFirebaseUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task != null){

                Log.e(TAG, "Profile updated " );
            }
            else{

                Log.e(TAG, "Profile update failed");
            }
        }
    }).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.e(TAG, "profile update sucess");
        }
    });
}
}

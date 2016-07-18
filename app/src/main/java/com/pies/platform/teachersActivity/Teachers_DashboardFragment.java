package com.pies.platform.teachersActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pies.platform.Login;
import com.pies.platform.R;
import com.pies.platform.admin.Admin_dashboard;
import com.pies.platform.admin.Create_Account;
import com.pies.platform.admin.model.AdminAdapter;
import com.pies.platform.admin.model.Admin_Item;
import com.pies.platform.model_users.Users;
import com.pies.platform.teachersActivity.model.Teacher_Adapter;
import com.pies.platform.teachersActivity.model.teacher_item;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class Teachers_DashboardFragment extends Fragment {
    private List<teacher_item> movieList = new ArrayList<>();
    private RecyclerView recyclerView2;
    private Teacher_Adapter mAdapter;
    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]
    FirebaseAuth mFirebaseAuth;
    private static final String TAG = "ManagerHome";
    private FirebaseUser mFirebaseUser;
    private String teacherNam, teacherEmail;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView tName, tEmail;
    private String url;
    private String userId;
    DatabaseReference myRootref = FirebaseDatabase.getInstance().getReference();

    ProgressDialog progressDialog;
    public Teachers_DashboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_teachers__dashboard, container, false);



        tName = (TextView) root.findViewById(R.id.teacher_name);
        tEmail = (TextView) root.findViewById(R.id.teacher_email);

        mFirebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Verifying...");

        progressDialog.setIndeterminate(false);
        recyclerView2 = (RecyclerView) root.findViewById(R.id.recycler_teacher);
        mAdapter = new Teacher_Adapter(movieList);
        // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), 2);
        recyclerView2.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView2.setItemAnimator(new DefaultItemAnimator());
        recyclerView2.setHasFixedSize(true);
        //recyclerView2.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        setHasOptionsMenu(true);
        recyclerView2.setAdapter(mAdapter);
        recyclerView2.addOnItemTouchListener(new Admin_dashboard.RecyclerTouchListener(getActivity(), recyclerView2, new Admin_dashboard.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                if(position == 0){
                    //startActivity(new Intent(getActivity(), Create_Account.class));
                    // myRootref.child("all-users").setValue("meeeeeee");
                }
                else if(position == 1){

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
        teacher_item manager = new teacher_item("Make Objective", "12","", getResources().getDrawable(R.drawable.user_added));
        movieList.add(manager);

        manager = new teacher_item("Give Feedback","", "", getResources().getDrawable(R.drawable.assign_icon));
        movieList.add(manager);


        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mFirebaseUser = firebaseAuth.getCurrentUser();
            if (mFirebaseUser == null) {
                // User is signed in
//                    Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
                startActivity(new Intent(getActivity(), Login.class));
            }
            else{

                if(mFirebaseUser.getEmail()!= null){
                    String n = mFirebaseUser.getEmail();
                    tEmail.setText(n);
                }

                Toast.makeText(getActivity(), mFirebaseUser.getUid().toString(), Toast.LENGTH_SHORT).show();

            }
        }};
    }


    public void userProfile(String uid) {
        progressDialog.show();
        mDatabase.child("Teachers-Profile").child(uid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Users item = dataSnapshot.getValue(Users.class);
                String url = item.getUserType();



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

}

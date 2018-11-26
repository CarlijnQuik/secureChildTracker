package com.example.carli.mychildtrackerdisplay;

import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class FirebaseRepository {
    FirebaseUser user;
    DatabaseReference database;
    UserEntry userEntry;

    // initialize Firebase
    public FirebaseRepository(){
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.database = FirebaseDatabase.getInstance().getReference(user.getUid());
    }

    public FirebaseRepository(FirebaseUser user, DatabaseReference database) {
        this.user = user;
        this.database = database;
    }

    public UserEntry getUserEntry()
    {
        database.getRoot().addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                userEntry = (UserEntry) dataSnapshot.getValue(UserEntry.class);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                userEntry = (UserEntry) dataSnapshot.getValue(UserEntry.class);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return userEntry;
    }

}
package com.nazir.shoppingserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nazir.shoppingserver.Common.Common;
import com.nazir.shoppingserver.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";

    FirebaseDatabase database;
    DatabaseReference tableUserRef;

    MaterialEditText edtPhone, edtPassword;
    Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone = findViewById(R.id.editPhone);
        edtPassword = findViewById(R.id.editPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        //init Firebase
        database = FirebaseDatabase.getInstance();
        tableUserRef = database.getReference("user");

        signInUser();

    }

    private void signInUser(){

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog = new ProgressDialog(SignInActivity.this);
                mDialog.setMessage("Wait");
                mDialog.show();

                tableUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        //Check if user doesnt exist in database
                        if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {

                            //Get user information
                            mDialog.dismiss();


                            User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                            user.setPhone(edtPhone.getText().toString());
                            Log.d(TAG, "onDataChange: " + user.getIsstaff());

                            if (Boolean.parseBoolean(user.getIsstaff())) { //if isstaff true

                                if (user.getPassword().equals(edtPassword.getText().toString())) {

                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                    Common.cuurentUser = user;
                                    startActivity(intent);
                                    finish();

                                } else {
                                    mDialog.dismiss();

                                    Toast.makeText(SignInActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                mDialog.dismiss();
                                Toast.makeText(SignInActivity.this, " Please Log In with Staff", Toast.LENGTH_SHORT).show();

                            }
                        }else{
                            mDialog.dismiss();
                            Toast.makeText(SignInActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }
}

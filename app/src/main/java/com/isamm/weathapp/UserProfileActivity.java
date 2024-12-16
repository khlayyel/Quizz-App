package com.isamm.weathapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private TextView textViewWelcome,textViewFullName, textViewEmail, textViewDoB, textViewGender, textViewMobile, textViewshowscore;
    private ImageView imageView;
    private ProgressBar  progressBar;

    private FirebaseAuth authProfile;
    private String fullName, email, doB, gender, mobile;

    private ActionBar actionBar;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Home"); // Set title if ActionBar is not null
        }

        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewDoB = findViewById(R.id.textView_show_dob);
        textViewGender = findViewById(R.id.textView_show_gender);
        textViewMobile = findViewById(R.id.textView_show_mobile);
        textViewshowscore = findViewById(R.id.textView_show_score2);
        progressBar = findViewById(R.id.progress_bar);



        //SetOnclickListener on Button to open MainActivity
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, quiz.class);
                startActivity(intent);
            }
        });

        //SetOnclickListener on ImageView to open uploadProfilePicActivity
        imageView = findViewById(R.id.imageView_profile_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });


        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        if (firebaseUser == null){
            Toast.makeText(UserProfileActivity.this,"Something went wrong! User's details are not available at the moment  ",Toast.LENGTH_LONG).show();

        } else {
            checkIfEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }

    }

    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }
    //Users coming to UserProfileActivity after successful registration
    private void showAlertDialog() {
        //Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email not verrified");
        builder.setMessage("Please verify your email now. You can not login without email verification next time.");

        //Open Email Apps if User clicks/taps Continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //To email app in new window and not within our app
                startActivity(intent);

            }
        });

        //create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        //Extracting User Reference from Database for
        // registered users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB = readUserDetails.getDoB();
                    gender = readUserDetails.getGender();
                    mobile = readUserDetails.getMobile();
                    int lastScore = readUserDetails.getLastScore();

                    textViewWelcome.setText("Welcome, " + fullName + "!");
                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewDoB.setText(doB);
                    textViewMobile.setText(mobile);
                    textViewGender.setText(gender);
                    textViewshowscore.setText(String.valueOf(lastScore));

                    //Set user DP (After user has uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();

                    //ImageViewer setImagerURI() should not be used with regular URIs.
                    Picasso.with(UserProfileActivity.this).load(uri).into(imageView);

                    // Save the new last score in the database
                    readUserDetails.setLastScore(quiz.score);
                    referenceProfile.child(userID).setValue(readUserDetails);


                } else {
                    Toast.makeText(UserProfileActivity.this,"Something went wrong!",Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this,"Something went wrong!",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    //Creating ActionBar Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu,menu);
        return true;
    }
    //When any item menu is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_refresh){
            //Refresh Activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();

        } /* else if (id == R.id.menu_settings) {
            Toast.makeText(UserProfileActivity.this, "menu_settings",Toast.LENGTH_SHORT).show();
        }*/ else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UserProfileActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity1.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //Close UserProfileActivity
        } else {
            Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();

        }
        return true;
    }
}

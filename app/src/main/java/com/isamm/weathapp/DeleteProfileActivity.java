package com.isamm.weathapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private ProgressBar progressBar;
    private String userPwd;
    private Button buttonReAuthenticate , buttonDeleteUser;
    private static final String TAG = "DeleteProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Delete Your Profile"); // Set title if ActionBar is not null
        }

        progressBar = findViewById(R.id.progressBar);
        editTextUserPwd = findViewById(R.id.editText_delete_user_pwd);
        textViewAuthenticated = findViewById(R.id.textView_delete_user_authenticated);
        buttonDeleteUser = findViewById(R.id.button_delete_user);
        buttonReAuthenticate = findViewById(R.id.button_delete_user_authenticate);

        //Disable Delete User Button until User is authenticated
        buttonDeleteUser.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "Something went wrong!" + "User Details are not available at the moment.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticate(firebaseUser);
        }

    }



    //ReAuthenticate User before changing password
    private void reAuthenticate(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd = editTextUserPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteProfileActivity.this, "Passwor is needed",Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Please enter your current password to authenticate");
                    editTextUserPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    //ReAuthenticate User now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //Disable editText for Password.
                                editTextUserPwd.setEnabled(false);


                                //Enable Delete User Button.
                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                //Set TextView to show User is authenticated/verified
                                textViewAuthenticated.setText("You are authenticated/verified" + "You can Delete your profile and related data now" );
                                Toast.makeText(DeleteProfileActivity.this, "Password has been verified" + "You can delete your profile now. Be careful, this action is irreversible",Toast.LENGTH_SHORT).show();
                                //Update color of change password button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfileActivity.this, R.color.red));

                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        showAlertDialog();
                                    }
                                });

                            } else {
                                try {
                                    throw task.getException();

                                } catch(Exception e){
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

    }

    private void showAlertDialog() {
        //Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete User and Realated Data?");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible");

        //Open Email Apps if User clicks/taps Continue button
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUser(firebaseUser);

            }
        });

        //Return to User Profile Activity if User presses Cancel Button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Change the Button color of Continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        //Show the AlertDialog
        alertDialog.show();
    }

    private void deleteUser(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    deleteUserData();
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "User has been deleted!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfileActivity.this, MainActivity1.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch(Exception e){
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    //Delete all the data of user
    private void deleteUserData() {
        //Delete Display Pic
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: Photo Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });

        //Delete Data from RealTime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu,menu);
        return super.onCreateOptionsMenu(menu);
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
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_settings) {
            Toast.makeText(UploadProfilePicActivity.this, "menu_settings",Toast.LENGTH_SHORT).show();
        }*/ else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(DeleteProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(DeleteProfileActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfileActivity.this, MainActivity1.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //Close UserProfileActivity
        }else {
            Toast.makeText(DeleteProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }

}
package com.example.learning;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private Button createAcctButton;
    private ProgressBar progressBar;

    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth=FirebaseAuth.getInstance();
        loginButton=findViewById(R.id.email_sign_in_button);
        createAcctButton=findViewById(R.id.create_account_id);

        emailAddress=findViewById(R.id.email);
        password=findViewById(R.id.passwordId);
        progressBar=findViewById(R.id.login_progress);
        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                        password.getText().toString().trim());
            }
        });
    }

    private void loginEmailPasswordUser(String email, String password) {
        if(!TextUtils.isEmpty(email)
        &&!TextUtils.isEmpty(password)){
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            assert user != null;
                            final String currentUserId=user.getUid();

                            collectionReference.whereEqualTo("userid",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                            @Nullable FirebaseFirestoreException e) {
                                            if(e!=null)
                                                return;

                                            assert queryDocumentSnapshots != null;
                                            if(!queryDocumentSnapshots.isEmpty()){
                                                for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                                                    JournalApi journalApi=JournalApi.getInstance();
                                                    journalApi.setUserName(snapshot.getString("username"));
                                                    journalApi.setUserId(currentUserId);

                                                    //Direct To List Activity
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(LoginActivity.this,PostJournalActivity.class));
                                                }
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }
        else{
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this,"Enter email and password",Toast.LENGTH_LONG)
                    .show();
        }
    }
}

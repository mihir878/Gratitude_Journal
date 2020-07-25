package com.example.learning;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private static final String TAG ="PostJournalActivity" ;
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addImageView;
    private EditText titleEditText;
    private EditText thoughtsEditText;
    private TextView curretUserTextView;
    private ImageView imageView;

    private String currentUserName;
    private String currentUserId;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Connection To Firestore
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference=db.collection("Journal");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.post_progressBar);
        saveButton=findViewById(R.id.post_save_journal_button);
        titleEditText=findViewById(R.id.post_title);
        thoughtsEditText=findViewById(R.id.post_description);
        curretUserTextView=findViewById(R.id.post_username_textview);
        addImageView=findViewById(R.id.postCameraButton);
        imageView=findViewById(R.id.post_imageView);

        saveButton.setOnClickListener(this);
        addImageView.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if(JournalApi.getInstance()!=null){
            currentUserName=JournalApi.getInstance().getUserName();
            currentUserId=JournalApi.getInstance().getUserId();

            curretUserTextView.setText(currentUserName);
        }

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user=firebaseAuth.getCurrentUser();
                if(user!=null){

                }
                else{

                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.post_save_journal_button:
                saveJournal();
                //save The things
                break;
            case R.id.postCameraButton:
                //add photo from Gallery
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
                break;
        }
    }

    private void saveJournal() {
        final String title=titleEditText.getText().toString().trim();
        final String thought=thoughtsEditText.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(title)&&
        !TextUtils.isEmpty(thought)&&
        imageUri!=null){

            final StorageReference filepath=storageReference
                    .child("journal_images")
                    .child("My_image"+ Timestamp.now().getSeconds());//My_image_randomInteger

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Todo: create Journal object , invoke collection reference and save journal instance
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl=uri.toString();
                                    Journal journal=new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thought);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimestamp(new Timestamp(new Date()));
                                    journal.setUserName(currentUserName);
                                    journal.setUserId(currentUserId);

                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this,ListJournalActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: "+e.getMessage());

                                                }
                                            });
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE&&resultCode==RESULT_OK){
            if(data!=null){
                imageUri=data.getData();
                imageView.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}

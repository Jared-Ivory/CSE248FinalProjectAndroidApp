package com.apps.jivory.googlemaps.viewmodels;

import android.app.Application;

import com.apps.jivory.googlemaps.arch.FirebaseLiveDataHelper;
import com.apps.jivory.googlemaps.arch.Repository;
import com.apps.jivory.googlemaps.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.apps.jivory.googlemaps.models.Post;
import com.google.firebase.database.DataSnapshot;

public class MainViewModel extends AndroidViewModel {
    public static final String TAG  = "MainViewModel";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Repository repo;
    private FirebaseLiveDataHelper firebaseUserData;
    private FirebaseLiveDataHelper firebasePostsData;
    private FirebaseLiveDataHelper firebaseAllUsersData;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Grabs instance of the repo
        repo = Repository.getInstance();

        firebaseUserData = new FirebaseLiveDataHelper(repo.getUserReference());
        firebasePostsData = new FirebaseLiveDataHelper(repo.getPostsReference());
        firebaseAllUsersData = new FirebaseLiveDataHelper(repo.getAllUsersReference());
    }

    public void deleteUser(User user){
        repo.deleteUser(user);
    }

    public void updateUser(User user){
        repo.updateUser(user);
    }

    public void insertNewPost(Post post){
        repo.insertNewPost(post);
    }

    public void updateNewPost(Post post){
        repo.updatePost(post);
    }

    public LiveData<DataSnapshot> getUserData(){
        return firebaseUserData;
    }
    public LiveData<DataSnapshot> getPostData(){
        return firebasePostsData;
    }
    public LiveData<DataSnapshot> getAllUsersData() {return  firebaseAllUsersData;}

    public void deletePost(String postID){
        repo.deletePost(postID);
    }

    public String getFirebaseUserID(){
        return mUser.getUid();
    }

    public void logout(){
        mAuth.signOut();
    }

    public void updatePost(Post post){
        repo.updatePost(post);
    }
}
package com.apps.jivory.googlemaps.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.apps.jivory.googlemaps.R;
import com.apps.jivory.googlemaps.models.CurrentUser;
import com.apps.jivory.googlemaps.models.PostHashMap;
import com.apps.jivory.googlemaps.observers.PostsObserver;
import com.apps.jivory.googlemaps.observers.CurrentUserObserver;
import com.apps.jivory.googlemaps.fragments.EditPostFragment;
import com.apps.jivory.googlemaps.fragments.MapFragment;
import com.apps.jivory.googlemaps.fragments.PostsFragment;
import com.apps.jivory.googlemaps.fragments.UserFragment;
import com.apps.jivory.googlemaps.models.User;
import com.apps.jivory.googlemaps.models.UsersHashMap;
import com.apps.jivory.googlemaps.observers.UsersObserver;
import com.apps.jivory.googlemaps.viewmodels.MainViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;


import android.util.Log;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import com.apps.jivory.googlemaps.models.Post;
import com.google.firebase.database.DataSnapshot;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, EditPostFragment.EditPostFragmentListener, MapFragment.MapListener, UsersObserver.UsersListener, UserFragment.UserFragmentListener, CurrentUserObserver.UserListener, PostsObserver.PostsListener {
    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    public static Boolean mLocationPermissionsGranted = false;
    public static MainViewModel mainViewModel;


    public static GoogleApiClient googleApiClient;

    private CurrentUser currentUser;
    private PostHashMap posts;
    private UsersHashMap users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        initializeViews();
        getLocationPerms();
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }




    @Override
    protected void onStart() {
        super.onStart();
        /**
         * Start Listening for data changes.
         */
        createObservers();
    }

    private void initializeViews(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }




    private void createObservers(){
        // Observes the userdata provided from the database.
        LiveData<DataSnapshot> userData = mainViewModel.getUserData();
        userData.observe(this, new CurrentUserObserver(this));
        // Observes the postsdata provided from the database.
        LiveData<DataSnapshot> postsData = mainViewModel.getPostData();
        postsData.observe(this, new PostsObserver(this));
        // Observers
        LiveData<DataSnapshot> allUsersData = mainViewModel.getAllUsersData();
        allUsersData.observe(this, new UsersObserver(this));
    }

    private void removeObservers(){
        LiveData<DataSnapshot> userData = mainViewModel.getUserData();
        userData.removeObservers(this);
        LiveData<DataSnapshot> postsData = mainViewModel.getPostData();
        postsData.removeObservers(this);
        LiveData<DataSnapshot> allUsersData = mainViewModel.getAllUsersData();
        allUsersData.removeObservers(this);

        currentUser.removeAllOvservers();
        posts.removeAllOvservers();
        users.removeAllOvservers();
    }


    /**Overrided methods for the navigation menu */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        switch(item.getItemId()){
            case(R.id.nav_account):
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new UserFragment(currentUser)).commit();
                break;
            case(R.id.nav_map):
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapFragment(posts,users,currentUser.getUser()) ).commit();
                break;
            case(R.id.nav_posts):
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PostsFragment(posts, users, currentUser.getUser())).commit();
                break;
            case(R.id.nav_logout):
                mainViewModel.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /** **/

    /** Location permissions **/
    private void getLocationPerms(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionsGranted = true;
        } else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE );
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                for(int i: grantResults){
                    if(i != PackageManager.PERMISSION_GRANTED){
                        mLocationPermissionsGranted = false;
                        return;
                    }
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /** CreatePostFragment Result**/
    @Override
    public void onPostSaved(Post p) {
        if(p.getCreator()!=null) {
            mainViewModel.updateNewPost(p);
            Toast.makeText(this, "Updated Post", Toast.LENGTH_SHORT).show();
        } else{
            mainViewModel.insertNewPost(p);
            Toast.makeText(this, "Posted Event" + p.getTitle(), Toast.LENGTH_SHORT).show();
        }

    }

    /** UserFragment Result**/
    @Override
    public void onUserSave(User user) {
        Log.d(TAG, "onUserSave: " + user.toString());
        mainViewModel.updateUser(user);
    }

    @Override
    public void onUserDelete(User user) {
        removeObservers();


        mainViewModel.deleteUser(user);
        finish();
        mainViewModel.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }

    /** Listener for user data changes**/
    @Override
    public void onCurrentUserChanged(CurrentUser currentUser) {
        currentUser.getUser().setUSER_ID(mainViewModel.getFirebaseUserID());
        this.currentUser = currentUser;
        Log.d(TAG, "onCurrentUserChanged: ***"+ this.currentUser.toString());

        String fullname = currentUser.getUser().getFullname();
        TextView textViewName = findViewById(R.id.header_Username);
        textViewName.setText(fullname);
        String emailaddress = currentUser.getUser().getEmailaddress();
        TextView textViewEmail = findViewById(R.id.header_EmailAddress);
        textViewEmail.setText(emailaddress);
    }
    /** Listener for posts data changes **/
    @Override
    public void onPostsChanged(PostHashMap postHashMap) {
        this.posts = postHashMap;
    }

    /** **/
    @Override
    public void onUsersChanged(UsersHashMap users) {
        this.users = users;
    }

    @Override
    public void onMapPostUpdated(Post p) {
        mainViewModel.updatePost(p);
    }
    /**  **/



}

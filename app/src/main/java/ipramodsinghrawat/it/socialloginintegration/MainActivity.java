package ipramodsinghrawat.it.socialloginintegration;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,GetUserCallback.IGetUserResponse {

    private LoginButton mFacebookSignInButton;
    private CallbackManager mFacebookCallbackManager;
    private TwitterLoginButton twitterLoginButton;
    GoogleSignInClient mGoogleSignInClient;

    int RC_SIGN_IN = 1;
    private SignInButton gSignInButton;
    private Button gSignOutButton,twitterLogoutButton,facebookLogoutButton;

    TextView usrDtlsTV;
    ImageView userImageView;

    String loginService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);

        setContentView(R.layout.activity_main);

        usrDtlsTV = findViewById(R.id.usrDtlsTV);
        userImageView = findViewById(R.id.userImageView);

        // Set the dimensions of the sign-in button.
        gSignInButton = findViewById(R.id.g_sign_in_button);
        gSignInButton.setOnClickListener(this);
        gSignOutButton = findViewById(R.id.g_sign_out_button);
        gSignOutButton.setOnClickListener(this);
        gSignOutButton.setVisibility(View.GONE);

        mFacebookSignInButton = (LoginButton) findViewById(R.id.login_button);
        facebookLogoutButton = findViewById(R.id.facebook_logout_button);
        facebookLogoutButton.setVisibility(View.GONE);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLogoutButton = findViewById(R.id.twitter_logout_button);
        twitterLogoutButton.setOnClickListener(this);
        twitterLogoutButton.setVisibility(View.GONE);

        /*SignIn using Google*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUIwithGoogleUserData(account);

        /* SignIn using Facebook */
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookSignInButton.registerCallback(mFacebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {}

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(MainActivity.class.getCanonicalName(), error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

        );

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                requestEmailAddress(getApplicationContext(), result.data);
                //Toast.makeText(getApplicationContext(),"result.response: "+result.response, Toast.LENGTH_SHORT).show();
                Log.i("twitterResult", result.toString());
                loadTwitterAPI(result.data);
                twitterLoginButton.setVisibility(View.INVISIBLE);
                twitterLogoutButton.setVisibility(View.VISIBLE);
                loginService= "twitter";
                hideShowOtherButtons();
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

        this.checkforTwitterActiveLogIn();
        this.hideShowOtherButtons();
    }

    private void hideShowOtherButtons(){
        if(loginService != null){
            /* Note: hide other buttons */
            gSignInButton.setVisibility(View.GONE);
            mFacebookSignInButton.setVisibility(View.GONE);
            twitterLoginButton.setVisibility(View.GONE);

        }else{
            gSignInButton.setVisibility(View.VISIBLE);
            mFacebookSignInButton.setVisibility(View.VISIBLE);
            twitterLoginButton.setVisibility(View.VISIBLE);
            usrDtlsTV.setText("Sign In With Above Option ");
            userImageView.setImageBitmap(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }else if(TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE == requestCode) {
            Log.d("TwitterAuthConfig", String.valueOf(resultCode));
            // Pass the activity result to the login button.
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }else{
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUIwithGoogleUserData(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GoogleSigninTag", "signInResult:failed code=" + e.getStatusCode());
            updateUIwithGoogleUserData(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserRequest.makeUserRequest(new GetUserCallback(MainActivity.this).getCallback());
    }

    @Override
    public void onCompleted(FBUser user) {
        if (user != null) {
            usrDtlsTV.setText("Details From FB\nName: "+user.getName()+"\n"
                    +"personEmail: "+user.getEmail()+"\n"
                    +"personId: "+user.getId()+"\n");
            Glide.with(this)
                    .load(user.getPicture()) // the uri you got from Firebase
                    .centerCrop()
                    .into(userImageView); //Your imageView variable
            loginService = "facebook";

            hideShowOtherButtons();

            facebookLogoutButton.setOnClickListener(MainActivity.this);
            facebookLogoutButton.setVisibility(View.VISIBLE);
        }else{
            usrDtlsTV.setText("Sign In Again");
            userImageView.setImageBitmap(null);
            mFacebookSignInButton.setVisibility(View.VISIBLE);
            facebookLogoutButton.setVisibility(View.INVISIBLE);
        }
    }

    public void updateUIwithGoogleUserData(GoogleSignInAccount account){
        if (account != null) {
            gSignInButton.setVisibility(View.INVISIBLE);
            gSignOutButton.setVisibility(View.VISIBLE);
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            usrDtlsTV.setText("personName: "+personName+"\n"
                    +"personGivenName: "+personGivenName+"\n"
                    +"personFamilyName: "+personFamilyName+"\n"
                    +"personEmail: "+personEmail+"\n"
                    +"personId: "+personId+"\n");

            //userImageView.setImageURI(personPhoto);

            Glide.with(this)
                    .load(personPhoto) // the uri you got from Firebase
                    .centerCrop()
                    .into(userImageView); //Your imageView variable

            loginService= "google";
            hideShowOtherButtons();

        }else{
            usrDtlsTV.setText("Sign In Again");
            userImageView.setImageBitmap(null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.g_sign_in_button:
                googleSignIn();
                break;
            // ...
            case R.id.g_sign_out_button:
                googleSignOut();
                break;
            // ...
            case R.id.twitter_logout_button:
                twitterSignOut();
                break;
            // ...
            case R.id.facebook_logout_button:
                faceBookSignOut();
                break;
        }
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void googleSignOut() {
        Toast.makeText(getApplicationContext(),"Google SignOut ...", Toast.LENGTH_SHORT).show();

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        gSignInButton.setVisibility(View.VISIBLE);
                        gSignOutButton.setVisibility(View.GONE);
                        updateUIwithGoogleUserData(null);
                    }
                });
        finish();
        startActivity(getIntent());
    }
    /*Disconnect google accounts*/
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private void faceBookSignOut(){
        Toast.makeText(getApplicationContext(),"Facebook SignOut ...", Toast.LENGTH_SHORT).show();
        LoginManager.getInstance().logOut();
        loginService = null;
        hideShowOtherButtons();
        facebookLogoutButton.setVisibility(View.INVISIBLE);
    }

    private void twitterSignOut(){
        Toast.makeText(getApplicationContext(),"Twitter SignOut ...", Toast.LENGTH_SHORT).show();

        TwitterCore.getInstance().getSessionManager().clearActiveSession();

        twitterLoginButton.setVisibility(View.VISIBLE);
        twitterLogoutButton.setVisibility(View.INVISIBLE);

        finish();
        startActivity(getIntent());
    }

    private static void requestEmailAddress(final Context context, TwitterSession session) {
        new TwitterAuthClient().requestEmail(session, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                Toast.makeText(context, "Twitter User Email:"+result.data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTwitterAPI(TwitterSession twitterSession) {

        long userID = twitterSession.getUserId();
        // Call the MyTwitterApiClient for user/show session new
        new MyTwitterApiClient(twitterSession).getCustomService().show(userID)
                .enqueue(new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        usrDtlsTV.setText(
                                "TwitterUserDertails:\nName: "+result.data.name
                                        +"\nScreenName: "+result.data.screenName
                                        +"\nLocation: "+result.data.location
                                        +"\nFriends: "+result.data.friendsCount
                        );
                        Picasso.with(getBaseContext()).load(result.data.profileImageUrl).
                                resize(250,250)
                                .into(userImageView);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.e("Failed", exception.toString());
                    }
                });
    }

    public void checkforTwitterActiveLogIn(){

        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        if(session == null){
            twitterLoginButton.setVisibility(View.VISIBLE);
            twitterLogoutButton.setVisibility(View.INVISIBLE);
        }else{
            TwitterAuthToken authToken = session.getAuthToken();
            String token = authToken.token;
            String secret = authToken.secret;

            String username = session.getUserName();
            Long userid = session.getUserId();

            requestEmailAddress(getApplicationContext(), session);

            loadTwitterAPI(session);

            twitterLoginButton.setVisibility(View.INVISIBLE);
            twitterLogoutButton.setVisibility(View.VISIBLE);
            loginService= "twitter";
        }
    }

}

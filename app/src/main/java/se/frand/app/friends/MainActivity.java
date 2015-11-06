package se.frand.app.friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = MainActivity.class.getSimpleName();

    CallbackManager callbackManager;
    FriendsListAdapter friendsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_likes");

        friendsListAdapter = new FriendsListAdapter(this);
        ListView friendsList = (ListView) findViewById(R.id.friendslist);
        friendsList.setAdapter(friendsListAdapter);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(loginResult.getRecentlyDeniedPermissions().size() > 0) {
                    return;
                }
                populateList();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                exception.printStackTrace();
            }

        });
        if(AccessToken.getCurrentAccessToken() != null) {
            populateList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void populateList() {
        Bundle param = new Bundle();
        param.putString("fields", "likes{name,picture}");

        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                param,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /*handle the result*/
                        try {
                            JSONObject jsonResponse = response.getJSONObject();
                            if(jsonResponse == null) {
                                Log.v(LOG_TAG + " JSON response", response.getRawResponse());
                                throw new JSONException("Null response");
                            }
                            JSONObject likes = jsonResponse.getJSONObject("likes");
                            JSONArray friendsArray = likes.getJSONArray("data");
                            for (int i = 0; i < friendsArray.length(); i++) {
                                JSONObject jsonFriend = friendsArray.getJSONObject(i);
                                JSONObject picture = jsonFriend.getJSONObject("picture").getJSONObject("data");
                                URL url = new URL(picture.getString("url"));
                                Friend friend = new Friend(
                                        jsonFriend.getLong("id"),
                                        jsonFriend.getString("name"),
                                        url
                                );
                                friendsListAdapter.add(friend);
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Problem with json");
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            Log.e(LOG_TAG,"Bad or non existent url");
                            e.printStackTrace();
                        }
                    }
                },
                "v2.5"
        );
        request.executeAsync();
    }
}

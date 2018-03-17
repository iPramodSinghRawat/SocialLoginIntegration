package ipramodsinghrawat.it.socialloginintegration;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;

/**
 * Created by iPramodSinghRawat on 16/03/18.
 */

public class UserRequest {
    private static final String ME_ENDPOINT = "/me";

    public static void makeUserRequest(GraphRequest.Callback callback) {
        Bundle params = new Bundle();
        params.putString("fields", "picture,name,id,email,permissions");


        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                ME_ENDPOINT,
                params,
                HttpMethod.GET,
                callback
        );
        request.executeAsync();
    }
}

package ipramodsinghrawat.it.socialloginintegration;

/**
 * Created by iPramodSinghRawat on 16/03/18.
 */

import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class MyTwitterApiClient extends TwitterApiClient {
    public MyTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public GetUsersShowAPICustomService getCustomService() {
        return getService(GetUsersShowAPICustomService.class);
    }
}

interface GetUsersShowAPICustomService {
    @GET("/1.1/users/show.json")
    Call<User> show(@Query("user_id") long userId);

    /*
    * In retrofit v1 you need to write like this
    *
    * @GET("/1.1/users/show.json")
    * void show(@Query("user_id") long userId, Callback<User> callBack);
    *
    * */
}

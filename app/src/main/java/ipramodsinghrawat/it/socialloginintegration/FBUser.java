package ipramodsinghrawat.it.socialloginintegration;

/**
 * Created by iPramodSinghRawat on 16/03/18.
 */
import android.net.Uri;

public class FBUser {
    private final Uri picture;
    private final String name;
    private final String id;
    private final String email;
    private final String permissions;

    public FBUser(Uri picture, String name,
                String id, String email, String permissions) {
        this.picture = picture;
        this.name = name;
        this.id = id;
        this.email = email;
        this.permissions = permissions;
    }

    public FBUser(Uri picture, String name,
                String id, String email) {
        this.picture = picture;
        this.name = name;
        this.id = id;
        this.email = email;
        this.permissions = "";
    }

    public Uri getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPermissions() {
        return permissions;
    }
}

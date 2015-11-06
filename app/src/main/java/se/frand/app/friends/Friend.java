package se.frand.app.friends;

import java.net.URL;

/**
 * Created by victorfrandsen on 11/5/15.
 */
public class Friend {
    public long id;
    public String name;
    public URL imageURL;
    public Friend(long id, String name, URL url) {
        this.id = id;
        this.name = name;
        this.imageURL = url;
    }
}

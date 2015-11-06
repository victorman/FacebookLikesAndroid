package se.frand.app.friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.FacebookSdk;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by victorfrandsen on 11/5/15.
 */
public class FriendsListAdapter extends BaseAdapter {

    private ArrayList<Friend> list;
    private Context context;

    public FriendsListAdapter(Context context) {
        list = new ArrayList<Friend>();
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend = (Friend) getItem(position);

        LinearLayout view = (LinearLayout) convertView;

        if(view == null) {
            view = (LinearLayout) View.inflate(context,R.layout.friendlist_item,null);
        }

        final ImageView imageView = (ImageView) view.findViewById(R.id.friend_item_image_view);

        final ImageTask task = new ImageTask();
        task.executeOnExecutor(FacebookSdk.getExecutor(),friend.imageURL);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    imageView.setImageBitmap(task.get());
                } catch (InterruptedException | ExecutionException e) {
                    Log.d("getView thread", "can't get image");
                }
            }
        }).run();


        TextView nameView = (TextView) view.findViewById(R.id.friend_item_name);
        nameView.setText(friend.name);

        return view;
    }

    private class ImageTask extends AsyncTask<URL,Void,Bitmap> {

        private final String LOG_TAG = ImageTask.class.getSimpleName();

        @Override
        protected Bitmap doInBackground(URL... params) {
            Bitmap returnBitmap = null;
            try {
                returnBitmap = BitmapFactory.decodeStream(params[0].openConnection().getInputStream());
            } catch (IOException e) {
                Log.e(LOG_TAG,"Failed loading image");
            }
            return returnBitmap;
        }
    }

    public boolean add(Friend friend) {
        boolean res = list.add(friend);
        if (res) notifyDataSetChanged();
        return res;
    }
}

package com.example.hoshiko.myspotifi;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hoshiko.myspotifi.model.Song;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SongsAdapter extends ArrayAdapter<Song> {

    private static final String TAG = SongsAdapter.class.getSimpleName();


    public SongsAdapter(Activity context, ArrayList<Song> songs) {

        super(context, 0, songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.song_list_item, parent, false);
        }


        final Song currentSong = getItem(position);

        // Hiển thị tên bài hát
        TextView nameTextView = listItemView.findViewById(R.id.title);
        nameTextView.setText(currentSong.getTitle());

        // Hiển thị số lượng likes
        final TextView likedNumTextView = listItemView.findViewById(R.id.likeNum_txt);
        likedNumTextView.setText(currentSong.getNumLikes() + " Likes");

        // Hiển thị số lượt nghe
       TextView playedNumTextView = listItemView.findViewById(R.id.playNum_txt);
       playedNumTextView.setText(currentSong.getNumPlays() + " Lượt");

        // Set icon riêng biệt cho những bài hát yêu thích/không yêu thích
        final ImageView favoriteView = listItemView.findViewById(R.id.favorite_ic);
        if (currentSong.isFavorite()) {
            favoriteView.setImageResource(R.drawable.icon_baseline_favorite);
        } else {
            favoriteView.setImageResource(R.drawable.icon_favorite_border);
        }


        // Hiển thị hình ảnh đại diện cho bài hát
        ImageView iconView = listItemView.findViewById(R.id.list_item_icon);
        String urlImage = "http://yeulaptrinh.xyz/music_app/image/" + currentSong.getImage() + ".jpg";
        Picasso.get().load(urlImage).into(iconView);


        //Lắng nghe sự kiện trên favorite icon
        // Thay đổi icon nếu trạng thái khác hiện tại

        favoriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSong.isFavorite()) {
                    currentSong.setFavorite(false);

                    //Lưu ngay vào list song ở UserActivity
                    for(int i=0; i<UserActivity.songs.size(); i++){
                        if(UserActivity.songs.get(i).getId() == currentSong.getId()){
                            UserActivity.songs.get(i).setFavorite(false);
                        }
                    }

                    //Giảm like ảo & set image
                    currentSong.setNumLikes(currentSong.getNumLikes()-1);
                    likedNumTextView.setText(currentSong.getNumLikes() + " Likes");
                    favoriteView.setImageResource(R.drawable.icon_favorite_border);

                    // Giảm bớt like trên server (table: music)
                    unlikeSong(currentSong.getId());


                } else {
                    currentSong.setFavorite(true);

                    //Lưu ngay vào list song ở UserActivity
                    for(int i=0; i<UserActivity.songs.size(); i++){
                        if(UserActivity.songs.get(i).getId() == currentSong.getId()){
                            UserActivity.songs.get(i).setFavorite(true);
                        }
                    }

                    // Tăng like ảo & set image
                    currentSong.setNumLikes(currentSong.getNumLikes()+1);
                    likedNumTextView.setText(currentSong.getNumLikes() + " Likes");
                    favoriteView.setImageResource(R.drawable.icon_baseline_favorite);

                    // Tăng like lên server (table: music)
                   likeSong(currentSong.getId());
                }

            }
        });

        return listItemView;
    }

    private void unlikeSong(final int id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL("http://yeulaptrinh.xyz/music_app/API/unlike.php?id=" + Integer.toString(id));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode == 200) {
                       Log.d(TAG, "UNLIKE SUCCESSFUL");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });

        thread.start();
    }


    private void likeSong(final int id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL("http://yeulaptrinh.xyz/music_app/API/add_like.php?id=" + Integer.toString(id));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode == 200) {
                        Log.d(TAG, "LIKE SUCCESSFUL");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });

        thread.start();
    }

}


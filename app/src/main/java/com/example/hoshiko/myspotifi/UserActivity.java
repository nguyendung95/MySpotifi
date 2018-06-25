package com.example.hoshiko.myspotifi;
import android.content.BroadcastReceiver;import android.content.ComponentName;import android.content.Context;import android.content.Intent;import android.content.IntentFilter;import android.content.ServiceConnection;import android.content.SharedPreferences;import android.os.Bundle;import android.os.IBinder;import android.support.design.widget.FloatingActionButton;import android.support.v4.content.LocalBroadcastManager;import android.util.Log;import android.view.View;import android.support.design.widget.NavigationView;import android.support.v4.view.GravityCompat;import android.support.v4.widget.DrawerLayout;import android.support.v7.app.ActionBarDrawerToggle;import android.support.v7.app.AppCompatActivity;import android.view.MenuItem;import android.widget.AdapterView;import android.widget.ListView;import android.widget.TextView;import android.widget.Toast;import com.example.hoshiko.myspotifi.login.LoginActivity;import com.example.hoshiko.myspotifi.model.Song;import com.example.hoshiko.myspotifi.utils.Constants;import org.json.JSONArray;import org.json.JSONException;import org.json.JSONObject;import java.io.BufferedInputStream;import java.io.BufferedReader;import java.io.InputStream;import java.io.InputStreamReader;import java.io.OutputStream;import java.net.HttpURLConnection;import java.net.URL;import java.util.ArrayList;import java.util.List;public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener { private static final String TAG = "User Activity";/*Khai báo các view*/static FloatingActionButton playPauseButton;PlayerService mBoundServices;boolean mServiceBound = false;SongsAdapter songsAdapter;/*id Của người dùng hiện tại*/public static int  idUser;/* Danh sách các bài hát đã liked*/ArrayList<Integer> favoriteSongs  ;ArrayList<Integer> savedSongs;public static List<Song> songs;ListView songsListView;private ServiceConnection mServiceConnection = new ServiceConnection() {@Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) { PlayerService.MyBinder myBinder = (PlayerService.MyBinder) iBinder;mBoundServices = myBinder.getService();mServiceBound = true; }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBound = false;
        }
    };


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
            flipPlayPauseButton(isPlaying);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize view
        songsListView = findViewById(R.id.listview_songs);
        favoriteSongs = new ArrayList<>();
        savedSongs = new ArrayList<>();
        songs = new ArrayList<>();

        //Nhận USER_ID từ Login_Activity
        Intent intent = getIntent();
        idUser = intent.getIntExtra("USER_ID", 1);
        Log.i(TAG, "ID CUA NGUOI DUNG LA: " + idUser);
        String userName = intent.getStringExtra("NAME");
        String userEmail = intent.getStringExtra("EMAIL");


        // Set up  Drawer for User
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set up Navgation  cho User
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup for USER INFORMATION header
        View headerLayout = navigationView.getHeaderView(0); // 0-index header
        // Set name of user
        TextView mNameUser = headerLayout.findViewById(R.id.name_user);
        mNameUser.setText(userName);
        // Set email of user
        TextView mEmailUser = headerLayout.findViewById(R.id.email_user);
        mEmailUser.setText(userEmail);

        // Lắng nghe sự kiện trên Play Pause Button
        playPauseButton = findViewById(R.id.playing_btn);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mServiceBound) mBoundServices.togglePlayer();
            }
        });

        //Đưa dữ liệu các bài hát đã like từ db về
        fetchFavoriteSong(idUser);

        // Bắt đầu StreamingService từ ../API/getsong.php;
        fetchSongsFromWeb();


    }


    private void startStreamingService(String url) {

        Intent intent = new Intent(this, PlayerService.class);
        intent.putExtra("URL", url);
        intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    // Cho foreground service  ra đi
    private void stopStreamingService(){
        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, new IntentFilter("changePlayButton"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    // Chuyển đổi button Play/Pause dựa trên trạng thái
    public static void flipPlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    // Kết nối đến Server và bắt đầu get toàn bộ bài hát có trong mySQL
    private void fetchSongsFromWeb() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                InputStream inputStream = null;

                try {
                    // Getmusic Api dành riêng cho việc get toàn bộ danh sách các bài hát có trong data
                    URL url = new URL("http://yeulaptrinh.xyz/music_app/API/getmusic.php");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode == 200) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertInputStreamToString(inputStream);
                        parseIntoSong(response);
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

    // Sử dụng inputStream để chuyển qua Song
    // Add vào SongArray
    private void parseIntoSong(String data) {
        String[] dataArray = data.split("\\*");

        for (int i = 0; i < dataArray.length; i++) {
            String[] songArray = dataArray[i].split(",");
            Song song = new Song(songArray[0].trim(), songArray[1].trim(), songArray[2].trim(),
                    songArray[3].trim(), songArray[4].trim());
            songs.add(song);
        }

        populateSongsListView();
    }


    // Hiển thị toàn bộ bài hát vào Listview
    // Click vào mỗi item sẽ stream đc bài hát
    private void populateSongsListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLikedSong();
                for(int i=0; i<songs.size(); i++){
                    if(songs.get(i).isFavorite() == true) {
                        Log.d(TAG, "Bài hát d " + songs.get(i).isFavorite() + "favoite");
                    }
                }



                songsAdapter = new SongsAdapter(UserActivity.this, (ArrayList<Song>) songs);
                songsListView.setAdapter(songsAdapter);

                songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Song song = songs.get(position);
                        String songAddress = "http://yeulaptrinh.xyz/music_app/" + song.getTitle();

                        // Bắt đầu streamingSẻvice
                        startStreamingService(songAddress);
                        markedSongPlayed(song.getId());
                        Toast.makeText(UserActivity.this, "ITEM ON CLICKED!!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Thiết lập các bài hát đã được like
    private void setLikedSong(){
        // Thiết lập vào các danh sách những bài hát đã đc yêu thích
        for(int i=0; i<favoriteSongs.size(); i++){
            for(int j=0; j<songs.size(); j++){
                if(favoriteSongs.get(i) == songs.get(j).getId()){
                    songs.get(j).setFavorite(true);
                }
            }
        }
    }


    // Kết nối đến server để get toàn bộ thông tin về người dùng (favorite song) có trong DB
    private void fetchFavoriteSong(final int idUser) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Chuỗi nhận dữ liệu từ server
                String stringIntoJSON = "";
                int temp;
                OutputStream outputStream ;
                InputStream inputStream;
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL("http://yeulaptrinh.xyz/music_app/API/get_favorite_song.php?id=" + idUser);
                    String urlParams = "id=" + idUser;

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);

                    // Start POST entered data into mySQL
                    outputStream = urlConnection.getOutputStream();
                    outputStream.write(urlParams.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    inputStream = urlConnection.getInputStream();
                    while ((temp = inputStream.read()) != -1) {
                        stringIntoJSON += (char) temp;
                    }
                    inputStream.close();

                    //Chuyển qua JSON OBJECT VÀ ĐƯA DỮ LIỆU VỀ
                    parseIntoPrivateSong(stringIntoJSON);

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


    public void parseIntoPrivateSong(String JSONString) {
        String error = null;
        try {

            JSONArray root = new JSONArray(JSONString);
            for (int i = 0; i < root.length(); i++) {
                JSONObject favorite_song = root.getJSONObject(i);
                String idUser = favorite_song.getString("id_user");
                int idSong = favorite_song.getInt("id_song");

                // Thêm bài hát ưa thích vào mảng
                Log.d(TAG, "BAI HAT DA LIKE " + idSong);
                favoriteSongs.add(idSong);
                Log.d(TAG, "BAI HAT DA LIKE trong list la " + favoriteSongs.get(0));

            }

        } catch (JSONException e) {
            e.printStackTrace();
            error = "Exception: " + e.getMessage();
            Log.d(TAG, error);
        }

    }

    private void markedSongPlayed(final int chosenId) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL("http://yeulaptrinh.xyz/music_app/API/add_play.php?id=" + Integer.toString(chosenId));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode == 200) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertInputStreamToString(inputStream);
                        Log.i(TAG, "Played Song ID: " + response);

                        parseIntoSong(response);
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

    //Convert inputStream to String (../API/getSong.php)
    public static String convertInputStreamToString(InputStream inputStream) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        if (inputStream != null) {
            inputStream.close();
        }
        return result;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        }  else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            // Đăng xuất
            logOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logOut() {

        stopStreamingService();
        saveFavoriteSong();

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("logged");
        editor.apply();
        finish();
    }

    // Lưu các bài hát đã được liked vào mảng và gọi api để lưu lên server
    private void saveFavoriteSong() {

        // Lưu lại toàn bộ bài hát của những bài hát đã like
        for (int i=0; i < songs.size(); i++) {
                if (songs.get(i).isFavorite()) {
                    final boolean add = savedSongs.add(songs.get(i).getId());
                    Log.d(TAG, "Saved song: " + songs.get(i).getId() +"___" + add);
                }
        }

        // Upload data lên server
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String data ="";
                int temp;
                OutputStream outputStream = null;
                InputStream inputStream = null;
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL("http://yeulaptrinh.xyz/music_app/API/add_favorite_song.php") ;
                    String urlParams = "id="+idUser+"&favorite="+savedSongs;

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);

                    // Start POST entered data into mySQL
                    outputStream = urlConnection.getOutputStream();
                    outputStream.write(urlParams.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    inputStream = urlConnection.getInputStream();
                    while ((temp = inputStream.read()) != -1){
                        data += (char) temp;
                    }

                    inputStream.close();
                    Log.d(TAG, "DATA FROM MYSQL IS (upload favorited song ): " + data);

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

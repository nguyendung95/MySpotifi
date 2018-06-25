package com.example.hoshiko.myspotifi;


import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;



import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.example.hoshiko.myspotifi.utils.Constants;

import java.io.IOException;

public class PlayerService extends Service {

    MediaPlayer mediaPlayer = new MediaPlayer();
    private final IBinder mBinder = new MyBinder();

    private MediaSessionCompat mediaSession;

    public class MyBinder extends Binder {
        PlayerService getService (){
            return PlayerService.this;
        }
    }

    public PlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mediaSession = new MediaSessionCompat(this, "MEDIA SESSION");

        if (null == intent || null == intent.getAction ()) {
            String source = null == intent ? "intent" : "action";
            Log.e ("FORE", source + " was null, flags=" + flags + " bits=" + Integer.toBinaryString (flags));
            //return START_REDELIVER_INTENT;
        }

        if(intent.getStringExtra("URL") != null ){
            Log.d("FORE", "String Extra = " + intent.getStringExtra("URL"));
            Log.d("FORE", "ACTION OF INTENT = " + intent.getAction() );
            playStream(intent.getStringExtra("URL"));
        }

            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                Log.i("FORE", "START FOREGROUND...");
                showNotification();
            } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
                Log.i("FORE", "PREV PRESSED...");
            } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
                Log.i("FORE", "NEXT PRESSED...");
            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                Log.i("FORE", "PLAY PRESSED...");
                togglePlayer();

            } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
                stopPlayer();
                stopForeground(true);
                stopSelf();
            }


        return START_REDELIVER_INTENT;

    }

    private void showNotification(){
        Intent notificationIntent = new Intent (this, UserActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent (this, PlayerService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent (this, PlayerService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent (this, PlayerService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Intent stopIntent = new Intent(this, PlayerService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pStopIntent = PendingIntent.getService(this, 0, stopIntent ,PendingIntent.FLAG_CANCEL_CURRENT);


        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_headset);

        int playPauseButtonId = android.R.drawable.ic_media_play;
        if(mediaPlayer != null && mediaPlayer.isPlaying())
            playPauseButtonId = android.R.drawable.ic_media_pause;


        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, "CHANNEL_ID");

        notification
                .setContentTitle("Music Player")
                .setTicker("Playing music")
                .setContentText("Everything")
                .setSmallIcon(R.drawable.icon_headset)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))

                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                .setContentIntent(pendingIntent)
                .setOngoing(true)

                // Add a action button
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Pre", ppreviousIntent))
                .addAction(new NotificationCompat.Action(playPauseButtonId, "Play", pplayIntent))
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next",  pnextIntent))
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_rew, "Stop", pStopIntent))

                // Take advantage of MediaStyle features
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0));

            this.startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification.build());

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }


    public void playStream(String url) {
        if (mediaPlayer != null) {

            try {
                mediaPlayer.stop();
            } catch (Exception e) { }

            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    playPlayer();
                }
            });

            //When mediaPlayer complete the song, playIcon will appear.
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    UserActivity.flipPlayPauseButton(false);
                }
            });
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void pausePlayer() {
        try {
            mediaPlayer.pause();
           flipPlayPauseButton(false);
           showNotification();

        } catch (Exception e) {
            Log.d("EXCEPTION", "Failed to pause media player");
        }
    }

    public void playPlayer() {
        try {
            getAudioFocusAndPlay();
           flipPlayPauseButton(true);
           showNotification();

        } catch (Exception e) {
            Log.d("EXCEPTION", "Failed to play media player");
        }
    }


    public void stopPlayer(){

        try {
            if (mediaPlayer!= null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            }
        } catch (Exception e) {
            Log.e("FORE", e.getMessage(), e);
        }
    }


    public void flipPlayPauseButton(boolean isPlaying){
        // Communicate with main thread
        Intent intent = new Intent("changePlayButton");
        // Add data into intent
        intent.putExtra("isPlaying", isPlaying);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void togglePlayer() {
        try {
            if (mediaPlayer.isPlaying()) {
                pausePlayer();
            } else {
                playPlayer();
            }
        } catch (Exception e) {
            Log.d("EXCEPTION", "Failed to toggle media player");
        }
    }



    //Audio Focus Section
    private AudioManager audioManager ;
    private boolean playingBeforeInterruption = false;

    private AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
                        if(mediaPlayer.isPlaying()) {
                            playingBeforeInterruption = true;
                        } else {
                            playingBeforeInterruption = false;
                        }
                        pausePlayer();
                    }
                    else if (focusChange == AudioManager.AUDIOFOCUS_GAIN){
                        if(playingBeforeInterruption)
                        playPlayer();
                    }
                    else if (focusChange == AudioManager.AUDIOFOCUS_LOSS){
                        pausePlayer();
                        audioManager.abandonAudioFocus(afChangeListener);
                    }
                }
            };

    public void getAudioFocusAndPlay(){

        audioManager = (AudioManager) this.getBaseContext().getSystemService(Context.AUDIO_SERVICE);

        //Request audio focus
       int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        //check Request
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            mediaPlayer.start();
        }
    }
}

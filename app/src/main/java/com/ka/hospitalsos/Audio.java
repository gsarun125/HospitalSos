package com.ka.hospitalsos;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class Audio {

    private static MediaPlayer mediaPlayer;


    public static void playAudio(Context context) {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.sound);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                Log.d("playAudio", "Audio started successfully");

                // Schedule the stop operation after 30 seconds (30000 milliseconds)
                //   handler.postDelayed(stopPlaybackRunnable, 30000);
            } else {
                Log.e("playAudio", "Failed to create MediaPlayer");
            }
        } catch (Exception e) {
            Log.e("playAudio", "Exception while playing audio: " + e.getMessage());
        }
    }
    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public static void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("playAudio", "Audio stopped successfully");
        }
    }
}

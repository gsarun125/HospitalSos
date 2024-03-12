package com.ka.hospitalsos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class RefusedReceiver extends BroadcastReceiver {
    private static final Map<Integer, MediaPlayer> mediaPlayerMap = new HashMap<>();


    @Override
    public void onReceive(Context context, Intent intent) {
        Audio.stopAudio();


        Toast.makeText(context, "Refused", Toast.LENGTH_SHORT).show();
    }

    private void stopAudio() {
        // Iterate through MediaPlayer instances to find the one playing
        for (MediaPlayer mediaPlayer : mediaPlayerMap.values()) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayerMap.remove(mediaPlayer.hashCode());
                Log.d("RefusedReceiver", "Audio stopped successfully");
                return; // Stop after the first playing instance is found and stopped
            }
        }
        Log.d("RefusedReceiver", "No audio is playing");
    }

}

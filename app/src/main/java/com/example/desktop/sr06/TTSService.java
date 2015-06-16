package com.example.desktop.sr06;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Desktop on 6/13/2015.
 */
public class TTSService extends Service implements TextToSpeechStartupListener, Handler.Callback {

    final static String TAG = "TTSService";
    private LocalBinder localBinder = new LocalBinder();
    public TextToSpeech textToSpeech;
    Context context;

    Intent broadcastTTSCompleted = new Intent();

    IntentFilter ttsIntentFilter = new IntentFilter();

    public TTSService () {

    }

    @Override
    public void onSuccessfulInit(TextToSpeech tts) {
        Log.d(TAG,"Inside onSuccessfulInit");
        if(textToSpeech == null && tts !=null) {
            Log.d(TAG,"Inside onSuccessfulInit");
            textToSpeech = tts;
        } else if(tts == null ) {
            Log.d(TAG,"tts was null, calling onDestroy");
            onDestroy();
        } else if(textToSpeech != null ) {
            Log.d(TAG,"textToSpeech is not null");
        } else {
            Log.d(TAG,"Calling onDestroy");
            onDestroy();
        }
        setTtsListener();

        Intent i = new Intent();
        i.setAction("TTS_SUCCESSFUL_INITILIZATION");
        sendBroadcast(i);

    }


    @Override
    public void onFailedToInit() {
        Log.d(TAG, "onFailedToInit");

    }




    public class LocalBinder extends Binder {
        public TTSService getService() {
            return TTSService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        new TextToSpeechInitilizer(getApplicationContext(),Locale.getDefault(),this);
        return localBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"Inside onCreate");
        super.onCreate();
        //ttsIntentFilter.addAction("TTS_SUCCESSFUL_INITILIZATION");
        //registerReceiver(TTSInitReceiver, ttsIntentFilter);

    }


private void setTtsListener() {
    final TTSService callWithResult = this;
    Log.d(TAG,"Inside setTtsListener");

    if (Build.VERSION.SDK_INT >= 15) {
        int listnerResult = textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onStart(String utteranceId) {
                //Broadcast onStart of TTS
                Log.d(TAG,"Inside setTtsListener onStart " + utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                //Broadcast onDone
                Log.d(TAG, "Inside setTtsListener onDone " +utteranceId);
                broadcastTTSCompleted.setAction("TTS_DONE");
                sendBroadcast(broadcastTTSCompleted);

            }

            @Override
            public void onError(String utteranceId) {
                Log.d(TAG,"Inside setTtsListener onError " + utteranceId);
            }

            @Override
            public void onError(String utteranceId, int errorCode) {
                super.onError(utteranceId, errorCode);
                Log.d(TAG, "Inside setTtsListener onError WITH ERROR code " + errorCode);
            }
        });

        if(listnerResult != TextToSpeech.SUCCESS) {
            Log.d(TAG,"Failed to setTtsListener");
        }
    } else {
        Log.d(TAG,"SDK version is lower");
    }
}

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:

                HashMap<String,String> params = new HashMap<String ,String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,"Stream Notification");

                textToSpeech.speak("Speaking something now...", TextToSpeech.QUEUE_FLUSH,params);

        }
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //unregisterReceiver(broadcastTTSCompleted);
    }
}

package com.example.desktop.sr06;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Desktop on 6/14/2015.
 */
public class TextToSpeechInitilizer {
    TTSService ttsService;
    Context appContext;
    Locale locale;
    private TextToSpeech ttsObj;
    private static String TAG = "TextToSpeechInitilizer";

    public TextToSpeechInitilizer(Context context, Locale locale, TTSService ttsService) {
        this.ttsService = ttsService;
        this.appContext = context;
        this.locale = locale;
        createTextToSpeech(locale);
    }

    private void createTextToSpeech(Locale loc) {
        Log.d(TAG,"Inside createTextToSpeech");

        ttsObj = new TextToSpeech(appContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {

                    //Send TTS Success init broadcast
                    Log.d(TAG, "Inside onInit success ");
                    //ttsService.getApplication().sendBroadcast(i);
                    ttsService.onSuccessfulInit(ttsObj);
                    //ttsService.sendBroadcast(i,null);

                } else {
                    //Send TTS failed init broadcast
                    Intent i = new Intent("TTS_FAILED_INITILIZATION");
                    Log.d(TAG,"Inside onInit failed ");
                    ttsService.sendBroadcast(i);
                }
            }
        });

    }
}

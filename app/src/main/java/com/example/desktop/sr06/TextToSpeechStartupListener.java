package com.example.desktop.sr06;

import android.speech.tts.TextToSpeech;

/**
 * Created by Desktop on 6/14/2015.
 */
public interface TextToSpeechStartupListener {

    public void onSuccessfulInit(TextToSpeech tts);
    public void onFailedToInit();



}

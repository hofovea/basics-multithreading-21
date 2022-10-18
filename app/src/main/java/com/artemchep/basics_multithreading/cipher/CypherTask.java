package com.artemchep.basics_multithreading.cipher;

import android.icu.text.SymbolTable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class CypherTask implements Runnable{
    final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String textToCypher;
    private ICypher cypherCallback;
    private long startTime;

    public CypherTask(String textToCypher, long startTime) {
        this.textToCypher = textToCypher;
        this.startTime = startTime;
    }

    public void setCypherCallback(ICypher entity) {
        this.cypherCallback = entity;
    }

    @Override
    public void run() {
        Log.d("TAGGGGG", "updateUICallback: " + Thread.currentThread().getName());
        final String cypheredText = CipherUtil.encrypt(textToCypher);
        final long resultTime = SystemClock.elapsedRealtime() - startTime;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                cypherCallback.updateUICallback(cypheredText, resultTime);
            }
        });
        Log.d("TAGGGGG", Thread.currentThread().getName() + " is finished");
        Log.d("TAGGGGG", cypheredText);
    }
}

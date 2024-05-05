package com.example.replypad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Objects;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if(!Objects.requireNonNull(remoteInput).isEmpty()) {
            String replyText = Objects.requireNonNull(remoteInput.getCharSequence(MyService.KEY)).toString();

            try {

                OutputStreamWriter outputSW = new OutputStreamWriter(
                        context.openFileOutput(MainActivity.PATH,
                                Context.MODE_PRIVATE|Context.MODE_APPEND
                        )
                );
                PrintWriter printWriter = new PrintWriter(outputSW);
                printWriter.println(replyText);
                printWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        context.startForegroundService(new Intent(context, MyService.class));

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
package com.example.lenovo.gupshup;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ProgressBar;

public class InternetChecker extends BroadcastReceiver {

    private static final String TAG = "InternetChecker";

    private OnDialogShowListener listener;

    public void setListener(OnDialogShowListener listener) {
        this.listener = listener;
    }

    public InternetChecker() {

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info!=null && info.isConnectedOrConnecting()) {
            listener.dismissDialog();
        } else {
            listener.showDialog();
        }
    }

    public interface OnDialogShowListener {
        void showDialog();
        void dismissDialog();
    }
}

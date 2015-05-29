package com.youngsee.adplayer.receiver;

import com.youngsee.adplayer.activity.AdMainActivity;
import com.youngsee.adplayer.common.Actions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Actions.BOOT_ACTION)) {
            context.startActivity(new Intent(context, AdMainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}

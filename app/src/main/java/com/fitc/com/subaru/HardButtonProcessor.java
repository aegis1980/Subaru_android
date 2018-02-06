package com.fitc.com.subaru;

import android.content.Context;
import android.content.Intent;

/**
 * Created by jon robinson on 6/02/2018.
 */

public class HardButtonProcessor {

    private final Context mContext;

    public HardButtonProcessor(Context context){
        mContext = context;
    }

    public void processPress(char c){
        
        switch (c){
            case 'i':
                break;
            case 'm':
                gotoLauncherHome();
                break;
        }

    }

    private void gotoLauncherHome() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(i);
    }
}

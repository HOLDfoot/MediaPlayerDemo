package app.xiaoming.mediaplayer.module;

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import app.xiaoming.mediaplayer.R;

/**
 * Author    ZhuMingren
 * Date      2019/4/9
 * Time      下午2:59
 * DESC      MediaPlayerDemo
 */
public class RingToneModule {
    
    public RingToneModule(Activity activity) {
        init(activity);
    }
    
    public void init(final Activity activity) {
        Button btnRing = activity.findViewById(R.id.btn_play_ring);
        btnRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlarm(activity);
            }
        });
    }
    
    //提示音
    private void startAlarm(Context context) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (notification == null) return;
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }

}

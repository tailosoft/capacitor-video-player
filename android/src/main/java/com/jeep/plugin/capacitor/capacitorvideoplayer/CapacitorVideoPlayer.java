package com.jeep.plugin.capacitor.capacitorvideoplayer;

import android.content.Context;
import com.getcapacitor.JSObject;
import com.jeep.plugin.capacitor.capacitorvideoplayer.PickerVideo.PickerVideoFragment;

public class CapacitorVideoPlayer {

    private final Context context;

    CapacitorVideoPlayer(Context context) {
        this.context = context;
    }

    public String echo(String value) {
        return value;
    }

    public FullscreenExoPlayerFragment createFullScreenFragment(
        String videoPath,
        Float videoRate,
        Boolean exitOnEnd,
        Boolean loopOnEnd,
        Boolean pipEnabled,
        Boolean bkModeEnabled,
        String subTitle,
        String language,
        JSObject subTitleOptions,
        JSObject headers,
        String title,
        String smallTitle,
        String accentColor,
        Boolean chromecast,
        Boolean isTV,
        String playerId,
        Boolean isInternal,
        Long videoId,
        Boolean hideCloseButton
    ) {
        FullscreenExoPlayerFragment fsFragment = new FullscreenExoPlayerFragment();

        fsFragment.videoPath = videoPath;
        fsFragment.videoRate = videoRate;
        fsFragment.exitOnEnd = exitOnEnd;
        fsFragment.loopOnEnd = loopOnEnd;
        fsFragment.pipEnabled = pipEnabled;
        fsFragment.bkModeEnabled = bkModeEnabled;
        fsFragment.subTitle = subTitle;
        fsFragment.language = language;
        fsFragment.subTitleOptions = subTitleOptions;
        fsFragment.headers = headers;
        fsFragment.title = title;
        fsFragment.smallTitle = smallTitle;
        fsFragment.accentColor = accentColor;
        fsFragment.chromecast = chromecast;
        fsFragment.isTV = isTV;
        fsFragment.playerId = playerId;
        fsFragment.isInternal = isInternal;
        fsFragment.videoId = videoId;
        fsFragment.hideCloseButton = hideCloseButton;
        return fsFragment;
    }

    public PickerVideoFragment createPickerVideoFragment() {
        return new PickerVideoFragment();
    }
}

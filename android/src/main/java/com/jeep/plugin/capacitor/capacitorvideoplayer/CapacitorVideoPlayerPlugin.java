package com.jeep.plugin.capacitor.capacitorvideoplayer;

import android.Manifest;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.jeep.plugin.capacitor.capacitorvideoplayer.Notifications.MyRunnable;
import com.jeep.plugin.capacitor.capacitorvideoplayer.Notifications.NotificationCenter;
import com.jeep.plugin.capacitor.capacitorvideoplayer.PickerVideo.PickerVideoFragment;
import com.jeep.plugin.capacitor.capacitorvideoplayer.Utilities.FilesUtils;
import com.jeep.plugin.capacitor.capacitorvideoplayer.Utilities.FragmentUtils;
import java.util.HashMap;
import java.util.Map;

@CapacitorPlugin(
    name = "CapacitorVideoPlayer",
    permissions = { @Permission(alias = CapacitorVideoPlayerPlugin.MEDIAVIDEO, strings = { Manifest.permission.READ_EXTERNAL_STORAGE }) }
)
public class CapacitorVideoPlayerPlugin extends Plugin {

    // Permission alias constants
    private static final String PERMISSION_DENIED_ERROR = "Unable to access media videos, user denied permission request";

    static final String MEDIAVIDEO = "video";

    private CapacitorVideoPlayer implementation;
    private static final String TAG = "CapacitorVideoPlayer";
    private final int frameLayoutViewId = 256;
    private final int pickerLayoutViewId = 257;

    private Context context;
    private String videoPath;
    private String subTitlePath;
    private Boolean isTV;
    private String fsPlayerId;
    private String mode;
    private Boolean exitOnEnd = true;
    private Boolean loopOnEnd = false;
    private Boolean pipEnabled = true;
    private Boolean bkModeEnabled = true;
    private FullscreenExoPlayerFragment fsFragment;
    private PickerVideoFragment pkFragment;
    private FilesUtils filesUtils;
    private JSObject headers;
    private FragmentUtils fragmentUtils;
    private PluginCall call;
    private Float rateList[] = { 0.25f, 0.5f, 0.75f, 1f, 2f, 4f };
    private Float videoRate = 1f;
    private String title;
    private String smallTitle;
    private String accentColor;
    private Boolean chromecast = true;
    private Boolean isPermissions = false;

    @Override
    public void load() {
        // Get context
        this.context = getContext();
        implementation = new CapacitorVideoPlayer(this.context);
        this.filesUtils = new FilesUtils(this.context);
        this.fragmentUtils = new FragmentUtils(getBridge());
    }

    @PermissionCallback
    private void videosPermissionsCallback(PluginCall call) {
        if (getPermissionState(MEDIAVIDEO) == PermissionState.GRANTED) {
            isPermissions = true;
            initPlayer(call);
        } else {
            call.reject(PERMISSION_DENIED_ERROR);
        }
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    private boolean isVideosPermissions() {
        // required for build version >= 29
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (getPermissionState(MEDIAVIDEO) != PermissionState.GRANTED) {
                return false;
            }
        }
        return true;
    }

    @PluginMethod
    public void initPlayer(PluginCall call) {
        this.call = call;
        final JSObject ret = new JSObject();
        ret.put("method", "initPlayer");
        ret.put("result", false);
        // Check if running on a TV Device
        isTV = isDeviceTV(context);
        Log.d(TAG, "**** isTV " + isTV + " ****");
        String _mode = call.getString("mode");
        if (_mode == null) {
            ret.put("message", "Must provide a Mode (fullscreen/embedded)");
            call.resolve(ret);
            return;
        }
        mode = _mode;
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        videoRate = 1f;
        if (call.getData().has("rate")) {
            Float mRate = call.getFloat("rate");
            if (isInRate(rateList, mRate)) {
                videoRate = mRate;
            }
        }
        Boolean _exitOnEnd = true;
        if (call.getData().has("exitOnEnd")) {
            _exitOnEnd = call.getBoolean("exitOnEnd");
        }
        exitOnEnd = _exitOnEnd;
        Boolean _loopOnEnd = false;
        if (call.getData().has("loopOnEnd")) {
            _loopOnEnd = call.getBoolean("loopOnEnd");
        }
        if (!exitOnEnd) loopOnEnd = _loopOnEnd;
        Boolean _pipEnabled = true;
        if (call.getData().has("pipEnabled")) {
            _pipEnabled = call.getBoolean("pipEnabled");
        }
        pipEnabled = _pipEnabled;
        Boolean _bkModeEnabled = true;
        if (call.getData().has("bkmodeEnabled")) {
            _bkModeEnabled = call.getBoolean("bkmodeEnabled");
        }
        bkModeEnabled = _bkModeEnabled;

        if ("fullscreen".equals(mode)) {
            fsPlayerId = playerId;
            String url = call.getString("url");
            if (url == null) {
                ret.put("message", "Must provide an url");
                call.resolve(ret);
                return;
            }
            String subtitle = "";
            if (call.getData().has("subtitle")) {
                subtitle = call.getString("subtitle");
            }
            String language = "";
            if (call.getData().has("language")) {
                language = call.getString("language");
            }
            JSObject subTitleOptions = new JSObject();
            if (call.getData().has("subtitleOptions")) {
                subTitleOptions = call.getObject("subtitleOptions");
            }
            Boolean hideCloseButton = false;
            if (call.getData().has("hideCloseButton")) {
                hideCloseButton = call.getBoolean("hideCloseButton", false);
            }
            Boolean disableSeeking = false;
            if (call.getData().has("disableSeeking")) {
                disableSeeking = call.getBoolean("disableSeeking", false);
            }

            JSObject _headers = new JSObject();
            if (call.getData().has("headers")) {
                _headers = call.getObject("headers");
            }
            headers = _headers;
            String _title = "";
            if (call.getData().has("title")) {
                _title = call.getString("title");
            }
            title = _title;
            String _smallTitle = "";
            if (call.getData().has("smallTitle")) {
                _smallTitle = call.getString("smallTitle");
            }
            smallTitle = _smallTitle;
            String _accentColor = "";
            if (call.getData().has("accentColor")) {
                _accentColor = call.getString("accentColor");
            }
            accentColor = _accentColor;
            Boolean _chromecast = true;
            if (call.getData().has("chromecast")) {
                _chromecast = call.getBoolean("chromecast");
            }
            chromecast = _chromecast;
            AddObserversToNotificationCenter();
            Log.v(TAG, "display url: " + url);
            Log.v(TAG, "display subtitle: " + subtitle);
            Log.v(TAG, "display language: " + language);
            Log.v(TAG, "headers: " + headers);
            Log.v(TAG, "title: " + title);
            Log.v(TAG, "smallTitle: " + smallTitle);
            Log.v(TAG, "accentColor: " + accentColor);
            Log.v(TAG, "chromecast: " + chromecast);
            if (url.equals("internal") || url.contains("DCIM")) {
                // Check for permissions to access media video files
                if (!isVideosPermissions()) {
                    this.bridge.saveCall(call);
                    requestAllPermissions(call, "videosPermissionsCallback");
                }
            } else {
              isPermissions = true;
            }
            if (isPermissions) {
                // Got Permissions ;
                if (url.equals("internal")) {
                    createPickerVideoFragment(call);
                } else {
                    // get the videoPath
                    videoPath = filesUtils.getFilePath(url);
                    // get the subTitlePath if any
                    if (subtitle != null && subtitle.length() > 0) {
                        subTitlePath = filesUtils.getFilePath(subtitle);
                    } else {
                        subTitlePath = null;
                    }
                    Log.v(TAG, "*** calculated videoPath: " + videoPath);
                    Log.v(TAG, "*** calculated subTitlePath: " + subTitlePath);
                    if (videoPath != null) {
                        createFullScreenFragment(
                            call,
                            videoPath,
                            videoRate,
                            exitOnEnd,
                            loopOnEnd,
                            pipEnabled,
                            bkModeEnabled,
                            subTitlePath,
                            language,
                            subTitleOptions,
                            headers,
                            title,
                            smallTitle,
                            accentColor,
                            chromecast,
                            isTV,
                            playerId,
                            false,
                            null
                        , hideCloseButton, disableSeeking);
                    } else {
                        Map<String, Object> info = new HashMap<String, Object>() {
                            {
                                put("dismiss", "1");
                            }
                        };
                        NotificationCenter.defaultCenter().postNotification("playerFullscreenDismiss", info);
                        ret.put("message", "initPlayer command failed: Video file not found");
                        call.resolve(ret);
                        return;
                    }
                }
            }
        } else if ("embedded".equals(mode)) {
            ret.put("message", "Embedded Mode not implemented");
            call.resolve(ret);
            return;
        }
    }

    @PluginMethod
    public void isPlaying(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "isPlaying");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "isPlaying");
                            if (fsFragment != null) {
                                boolean playing = fsFragment != null && fsFragment.isPlaying();
                                ret.put("result", true);
                                ret.put("value", playing);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void play(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "play");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "play");
                            if (fsFragment != null) {
                                fsFragment.play();
                                boolean playing = fsFragment.isPlaying();
                                ret.put("result", true);
                                ret.put("value", true);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void pause(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "pause");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "pause");
                            if (fsFragment != null) {
                                fsFragment.pause();
                                ret.put("result", true);
                                ret.put("value", true);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void getDuration(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "getDuration");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "getDuration");
                            if (fsFragment != null) {
                                int duration = fsFragment.getDuration();
                                ret.put("result", true);
                                ret.put("value", duration);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void getCurrentTime(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "getCurrentTime");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "getCurrentTime");
                            if (fsFragment != null) {
                                int curTime = fsFragment == null ? 0 : fsFragment.getCurrentTime();
                                ret.put("result", true);
                                ret.put("value", curTime);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void setCurrentTime(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "setCurrentTime");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        Double value = call.getDouble("seektime");
        if (value == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a time in second");
            call.resolve(ret);
            return;
        }
        final int cTime = (int) Math.round(value);
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "setCurrentTime");
                            if (fsFragment != null) {
                                fsFragment.setCurrentTime(cTime);
                                ret.put("result", true);
                                ret.put("value", cTime);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void getVolume(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "getVolume");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "getVolume");
                            if (fsFragment != null) {
                                Float volume = fsFragment.getVolume();
                                ret.put("result", true);
                                ret.put("value", volume);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void setVolume(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "setVolume");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        Float volume = call.getFloat("volume");
        if (volume == null) {
            ret.put("result", false);
            ret.put("method", "setVolume");
            ret.put("message", "Must provide a volume value");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "setVolume");
                            if (fsFragment != null) {
                                fsFragment.setVolume(volume);
                                ret.put("result", true);
                                ret.put("value", volume);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void getMuted(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "getMuted");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "getMuted");
                            if (fsFragment != null) {
                                boolean value = fsFragment.getMuted();
                                ret.put("result", true);
                                ret.put("value", value);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void setMuted(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "setMuted");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        Boolean value = call.getBoolean("muted");
        if (value == null) {
            ret.put("result", true);
            ret.put("message", "Must provide a boolean true/false");
            call.resolve(ret);
            return;
        }
        final boolean bValue = value;
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "setMuted");
                            if (fsFragment != null) {
                                fsFragment.setMuted(bValue);
                                ret.put("result", true);
                                ret.put("value", bValue);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void getRate(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "getRate");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "getRate");
                            if (fsFragment != null) {
                                Float rate = fsFragment.getRate();
                                ret.put("result", true);
                                ret.put("value", rate);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void setRate(final PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "setRate");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        Float rate = call.getFloat("rate");
        if (rate == null) {
            ret.put("result", false);
            ret.put("method", "setRate");
            ret.put("message", "Must provide a volume value");
            call.resolve(ret);
            return;
        }
        if (isInRate(rateList, rate)) {
            videoRate = rate;
        } else {
            videoRate = 1f;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JSObject ret = new JSObject();
                            ret.put("method", "setRate");
                            if (fsFragment != null) {
                                fsFragment.setRate(videoRate);
                                ret.put("result", true);
                                ret.put("value", videoRate);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "Fullscreen fragment is not defined");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void stopAllPlayers(PluginCall call) {
        this.call = call;
        bridge
            .getActivity()
            .runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        JSObject ret = new JSObject();
                        ret.put("method", "stopAllPlayers");
                        if (fsFragment != null) {
                            fsFragment.pause();
                            ret.put("result", true);
                            ret.put("value", true);
                            call.resolve(ret);
                        } else {
                            ret.put("result", false);
                            ret.put("message", "Fullscreen fragment is not defined");
                            call.resolve(ret);
                        }
                    }
                }
            );
    }

    @PluginMethod
    public void unloadPlayer(PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "unloadPlayer");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (fsFragment != null) {
                                fsFragment.unload();
                                JSObject ret = new JSObject();
                                ret.put("result", true);
                                ret.put("method", "unloadPlayer");
                                ret.put("value", true);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "player is not loaded");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void expandPlayer(PluginCall call) {
        this.call = call;
        JSObject ret = new JSObject();
        ret.put("method", "expandPlayer");
        String playerId = call.getString("playerId");
        if (playerId == null) {
            ret.put("result", false);
            ret.put("message", "Must provide a PlayerId");
            call.resolve(ret);
            return;
        }
        if ("fullscreen".equals(mode) && fsPlayerId.equals(playerId)) {
            bridge
                .getActivity()
                .runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (fsFragment != null) {
                                fsFragment.expandPlayer();
                                JSObject ret = new JSObject();
                                ret.put("result", true);
                                ret.put("method", "expandPlayer");
                                ret.put("value", true);
                                call.resolve(ret);
                            } else {
                                ret.put("result", false);
                                ret.put("message", "player is not loaded");
                                call.resolve(ret);
                            }
                        }
                    }
                );
        } else {
            ret.put("result", false);
            ret.put("message", "player is not defined");
            call.resolve(ret);
        }
    }

    public boolean isDeviceTV(Context context) {
        //Since Android TV is only API 21+ that is the only time we will compare configurations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            return uiManager != null && uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
        }
        return false;
    }

    private Boolean isInRate(Float arr[], Float rate) {
        Boolean ret = false;
        for (Float el : arr) {
            if (el.equals(rate)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private void AddObserversToNotificationCenter() {
        NotificationCenter
            .defaultCenter()
            .addMethodForNotification(
                "playerItemPlay",
                new MyRunnable() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("fromPlayerId", this.getInfo().get("fromPlayerId"));
                        data.put("currentTime", this.getInfo().get("currentTime"));
                        notifyListeners("jeepCapVideoPlayerPlay", data);
                        return;
                    }
                }
            );
        NotificationCenter
            .defaultCenter()
            .addMethodForNotification(
                "playerItemPause",
                new MyRunnable() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("fromPlayerId", this.getInfo().get("fromPlayerId"));
                        data.put("currentTime", this.getInfo().get("currentTime"));
                        notifyListeners("jeepCapVideoPlayerPause", data);
                        return;
                    }
                }
            );
        NotificationCenter
            .defaultCenter()
            .addMethodForNotification(
                "playerItemReady",
                new MyRunnable() {
                    @Override
                    public void run() {
                        JSObject data = new JSObject();
                        data.put("fromPlayerId", this.getInfo().get("fromPlayerId"));
                        data.put("currentTime", this.getInfo().get("currentTime"));
                        notifyListeners("jeepCapVideoPlayerReady", data);
                        return;
                    }
                }
            );
        NotificationCenter
            .defaultCenter()
            .addMethodForNotification(
                "playerItemEnd",
                new MyRunnable() {
                    @Override
                    public void run() {
                        final JSObject data = new JSObject();
                        data.put("fromPlayerId", this.getInfo().get("fromPlayerId"));
                        data.put("currentTime", this.getInfo().get("currentTime"));
                        bridge
                            .getActivity()
                            .runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        FrameLayout frameLayoutView = getBridge().getActivity().findViewById(frameLayoutViewId);

                                        if (frameLayoutView != null) {
                                            ((ViewGroup) getBridge().getWebView().getParent()).removeView(frameLayoutView);
                                            fragmentUtils.removeFragment(fsFragment);
                                        }
                                        fsFragment = null;
                                        NotificationCenter.defaultCenter().removeAllNotifications();
                                        notifyListeners("jeepCapVideoPlayerEnded", data);
                                    }
                                }
                            );
                    }
                }
            );
        NotificationCenter
            .defaultCenter()
            .addMethodForNotification(
                "playerFullscreenDismiss",
                new MyRunnable() {
                    @Override
                    public void run() {
                        boolean ret = false;
                        final JSObject data = new JSObject();
                        if (Integer.valueOf((String) this.getInfo().get("dismiss")) == 1) ret = true;
                        data.put("dismiss", ret);
                        data.put("currentTime", this.getInfo().get("currentTime"));
                        bridge
                            .getActivity()
                            .runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        FrameLayout frameLayoutView = getBridge().getActivity().findViewById(frameLayoutViewId);

                                        if (frameLayoutView != null) {
                                            ((ViewGroup) getBridge().getWebView().getParent()).removeView(frameLayoutView);
                                            fragmentUtils.removeFragment(fsFragment);
                                        }
                                        fsFragment = null;
                                        NotificationCenter.defaultCenter().removeAllNotifications();
                                        notifyListeners("jeepCapVideoPlayerExit", data);
                                    }
                                }
                            );
                    }
                }
            );
        NotificationCenter
            .defaultCenter()
            .addMethodForNotification(
                "videoPathInternalReady",
                new MyRunnable() {
                    @Override
                    public void run() {
                        long videoId = (Long) this.getInfo().get("videoId");
                        // Get the previously saved call
                        FrameLayout pickerLayoutView = getBridge().getActivity().findViewById(pickerLayoutViewId);
                        if (pickerLayoutView != null) {
                            ((ViewGroup) getBridge().getWebView().getParent()).removeView(pickerLayoutView);
                            fragmentUtils.removeFragment(pkFragment);
                        }
                        pkFragment = null;
                        if (videoId != -1) {
                            Log.v(TAG, "§§§§ Notification videoPathInternalReady chromecast: " + chromecast);
                            createFullScreenFragment(
                                call,
                                videoPath,
                                videoRate,
                                exitOnEnd,
                                loopOnEnd,
                                pipEnabled,
                                bkModeEnabled,
                                null,
                                null,
                                null,
                                headers,
                                title,
                                smallTitle,
                                accentColor,
                                chromecast,
                                isTV,
                                fsPlayerId,
                                true,
                                videoId
                            , false, false);
                        } else {
                            Toast.makeText(context, "No Video files found ", Toast.LENGTH_SHORT).show();
                            Map<String, Object> info = new HashMap<String, Object>() {
                                {
                                    put("dismiss", "1");
                                }
                            };
                            NotificationCenter.defaultCenter().postNotification("playerFullscreenDismiss", info);
                        }
                    }
                }
            );
    }

    private void createFullScreenFragment(
        final PluginCall call,
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
        Boolean hideCloseButton,
        Boolean disableSeeking
    ) {
        Log.v(TAG, "§§§§ createFullScreenFragment chromecast: " + chromecast);

        fsFragment =
            implementation.createFullScreenFragment(
                videoPath,
                videoRate,
                exitOnEnd,
                loopOnEnd,
                pipEnabled,
                bkModeEnabled,
                subTitle,
                language,
                subTitleOptions,
                headers,
                title,
                smallTitle,
                accentColor,
                chromecast,
                isTV,
                playerId,
                isInternal,
                videoId
            , hideCloseButton, disableSeeking);
        bridge
            .getActivity()
            .runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        JSObject ret = new JSObject();
                        ret.put("method", "initPlayer");
                        FrameLayout frameLayoutView = getBridge().getActivity().findViewById(frameLayoutViewId);
                        if (frameLayoutView != null) {
                            ret.put("result", false);
                            ret.put("message", "FrameLayout for ExoPlayer already exists");
                        } else {
                            // Initialize a new FrameLayout as container for fragment
                            frameLayoutView = new FrameLayout(getActivity().getApplicationContext());
                            frameLayoutView.setId(frameLayoutViewId);
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            );
                            // Apply the Layout Parameters to frameLayout
                            frameLayoutView.setLayoutParams(lp);

                            ((ViewGroup) getBridge().getWebView().getParent()).addView(frameLayoutView);
                            fragmentUtils.loadFragment(fsFragment, frameLayoutViewId);
                            ret.put("result", true);
                        }
                        call.resolve(ret);
                    }
                }
            );
    }

    private void createPickerVideoFragment(final PluginCall call) {
        pkFragment = implementation.createPickerVideoFragment();

        bridge
            .getActivity()
            .runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        JSObject ret = new JSObject();
                        ret.put("method", "initPlayer");
                        FrameLayout pickerLayoutView = getBridge().getActivity().findViewById(pickerLayoutViewId);
                        if (pickerLayoutView != null) {
                            ret.put("result", false);
                            ret.put("message", "FrameLayout for VideoPicker already exists");
                        } else {
                            // Initialize a new FrameLayout as container for fragment
                            pickerLayoutView = new FrameLayout(getActivity().getApplicationContext());
                            pickerLayoutView.setId(pickerLayoutViewId);
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            );
                            // Apply the Layout Parameters to frameLayout
                            pickerLayoutView.setLayoutParams(lp);

                            ((ViewGroup) getBridge().getWebView().getParent()).addView(pickerLayoutView);
                            fragmentUtils.loadFragment(pkFragment, pickerLayoutViewId);
                            ret.put("result", true);
                        }
                        call.resolve(ret);
                    }
                }
            );
    }
}

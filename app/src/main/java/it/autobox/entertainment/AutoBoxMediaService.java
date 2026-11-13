package it.autobox.entertainment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * A real audio media browser for Android Auto:
 * searchable library of local music, playable self-generated audio sample,
 * MediaSessionCompat play/pause/next/previous/seek and a media notification.
 * Third-party streaming apps are opened independently on the phone.
 */
public final class AutoBoxMediaService extends MediaBrowserServiceCompat {
    public static final String ACTION_PLAY_ID = "it.autobox.entertainment.PLAY_ID";
    public static final String ACTION_PLAY = "it.autobox.entertainment.PLAY";
    public static final String ACTION_PAUSE = "it.autobox.entertainment.PAUSE";
    public static final String ACTION_NEXT = "it.autobox.entertainment.NEXT";
    public static final String ACTION_PREVIOUS = "it.autobox.entertainment.PREVIOUS";
    public static final String ACTION_STOP = "it.autobox.entertainment.STOP";
    public static final String EXTRA_MEDIA_ID = "media_id";

    private static final String ROOT = "root";
    private static final String LOCAL = "local";
    private static final String DEMO = "demo";
    private static final int NOTIFICATION_ID = 2084;
    private static final String CHANNEL_ID = "autobox_audio_playback";

    private MediaSessionCompat session;
    private MediaPlayer player;
    private final List<AutoBoxCatalog.Track> queue = new ArrayList<>();
    private int position = -1;
    private boolean prepared;
    private boolean foreground;
    private int state = PlaybackStateCompat.STATE_STOPPED;
    private AudioManager audioManager;
    private final AudioManager.OnAudioFocusChangeListener audioFocusListener = change -> {
        if (change == AudioManager.AUDIOFOCUS_LOSS ||
                change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            if (player != null && prepared && player.isPlaying()) pausePlayback();
        } else if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            if (player != null && prepared) player.setVolume(0.2f, 0.2f);
        } else if (change == AudioManager.AUDIOFOCUS_GAIN) {
            if (player != null && prepared) player.setVolume(1f, 1f);
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createChannel();
        session = new MediaSessionCompat(this, "AutoBox Audio");
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override public void onPlayFromMediaId(String id, Bundle extras) {
                playById(id);
            }
            @Override public void onPlay() {
                resumePlayback();
            }
            @Override public void onPause() {
                pausePlayback();
            }
            @Override public void onStop() {
                stopPlayback();
            }
            @Override public void onSkipToNext() {
                skip(1);
            }
            @Override public void onSkipToPrevious() {
                skip(-1);
            }
            @Override public void onSeekTo(long ms) {
                if (player != null && prepared) {
                    player.seekTo((int)Math.max(0L, Math.min(ms, Integer.MAX_VALUE)));
                    updatePlaybackState(state);
                }
            }
            @Override public void onPlayFromSearch(String search, Bundle extras) {
                String query = search == null ? "" : search.toLowerCase(java.util.Locale.ROOT);
                for (AutoBoxCatalog.Track t : AutoBoxCatalog.getLocalTracks(AutoBoxMediaService.this)) {
                    if (t.title.toLowerCase(java.util.Locale.ROOT).contains(query) ||
                        t.artist.toLowerCase(java.util.Locale.ROOT).contains(query)) {
                        playById(t.id);
                        return;
                    }
                }
                if (query.contains("autobox") || query.contains("demo")) {
                    playById(AutoBoxCatalog.DEMO_ID);
                } else {
                    updatePlaybackState(PlaybackStateCompat.STATE_ERROR);
                }
            }
        });
        session.setActive(true);
        setSessionToken(session.getSessionToken());
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
    }

    @Override public BrowserRoot onGetRoot(@NonNull String clientPackageName,
            int clientUid, Bundle rootHints) {
        // Required for Android Auto to discover the actual music catalogue.
        return new BrowserRoot(ROOT, null);
    }

    @Override public void onLoadChildren(@NonNull String parentId,
            @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        switch (parentId) {
            case ROOT:
                items.add(browserItem(LOCAL, "Musica sul Samsung",
                        "I brani audio salvati sul telefono"));
                items.add(browserItem(DEMO, "Ascolta AutoBox",
                        "Audio originale per provare i comandi"));
                break;
            case LOCAL:
                for (AutoBoxCatalog.Track t : AutoBoxCatalog.getLocalTracks(this)) {
                    items.add(songItem(t));
                }
                break;
            case DEMO:
                items.add(songItem(AutoBoxCatalog.getDemo(this)));
                break;
            default:
                break;
        }
        result.sendResult(items);
    }

    private MediaBrowserCompat.MediaItem browserItem(String id, String title, String summary) {
        return new MediaBrowserCompat.MediaItem(
                new MediaDescriptionCompat.Builder()
                    .setMediaId(id).setTitle(title).setSubtitle(summary).build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem songItem(AutoBoxCatalog.Track t) {
        return new MediaBrowserCompat.MediaItem(
                new MediaDescriptionCompat.Builder()
                    .setMediaId(t.id)
                    .setTitle(t.title).setSubtitle(t.artist)
                    .setMediaUri(t.uri).build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_PLAY_ID.equals(action)) {
                playById(intent.getStringExtra(EXTRA_MEDIA_ID));
            } else if (ACTION_PLAY.equals(action)) {
                resumePlayback();
            } else if (ACTION_PAUSE.equals(action)) {
                pausePlayback();
            } else if (ACTION_NEXT.equals(action)) {
                skip(1);
            } else if (ACTION_PREVIOUS.equals(action)) {
                skip(-1);
            } else if (ACTION_STOP.equals(action)) {
                stopPlayback();
            }
        }
        return START_NOT_STICKY;
    }

    private void playById(String mediaId) {
        if (mediaId == null) return;
        queue.clear();
        if (AutoBoxCatalog.DEMO_ID.equals(mediaId)) {
            queue.add(AutoBoxCatalog.getDemo(this));
        } else if (mediaId.startsWith(AutoBoxCatalog.LOCAL_PREFIX)) {
            queue.addAll(AutoBoxCatalog.getLocalTracks(this));
        }
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).id.equals(mediaId)) {
                loadTrack(i);
                return;
            }
        }
        updatePlaybackState(PlaybackStateCompat.STATE_ERROR);
    }

    private void loadTrack(int newPosition) {
        if (queue.isEmpty() || newPosition < 0 || newPosition >= queue.size()) return;
        releasePlayer();
        position = newPosition;
        prepared = false;
        AutoBoxCatalog.Track track = queue.get(position);
        session.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.id)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.durationMs)
                .build());
        updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING);
        showPlaybackNotification(true);

        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            player.setDataSource(this, track.uri);
            player.setOnPreparedListener(mp -> {
                prepared = true;
                if (!requestAudioFocus()) {
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                    showPlaybackNotification(false);
                    return;
                }
                mp.start();
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                showPlaybackNotification(true);
            });
            player.setOnCompletionListener(mp -> {
                if (queue.size() > 1 && position + 1 < queue.size()) {
                    loadTrack(position + 1);
                } else {
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                    showPlaybackNotification(false);
                }
            });
            player.setOnErrorListener((mp, what, extra) -> {
                updatePlaybackState(PlaybackStateCompat.STATE_ERROR);
                showPlaybackNotification(false);
                return true;
            });
            player.prepareAsync();
        } catch (Exception e) {
            releasePlayer();
            updatePlaybackState(PlaybackStateCompat.STATE_ERROR);
            showPlaybackNotification(false);
        }
    }

    private void resumePlayback() {
        if (player == null) {
            if (queue.isEmpty()) playById(AutoBoxCatalog.DEMO_ID);
            else loadTrack(Math.max(0, position));
        } else if (prepared) {
            if (!requestAudioFocus()) return;
            try {
                player.start();
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                showPlaybackNotification(true);
            } catch (Exception ignored) { }
        }
    }

    private void pausePlayback() {
        if (player != null && prepared) {
            try {
                if (player.isPlaying()) player.pause();
            } catch (Exception ignored) { }
        }
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
        showPlaybackNotification(false);
    }

    private void stopPlayback() {
        releasePlayer();
        queue.clear();
        position = -1;
        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
        if (foreground) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            foreground = false;
        }
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        audioManager.abandonAudioFocus(audioFocusListener);
    }

    private void skip(int delta) {
        if (queue.isEmpty()) {
            playById(AutoBoxCatalog.DEMO_ID);
            return;
        }
        int next = (position + delta + queue.size()) % queue.size();
        loadTrack(next);
    }

    private void updatePlaybackState(int newState) {
        state = newState;
        long ms = 0L;
        if (player != null && prepared) {
            try { ms = player.getCurrentPosition(); } catch (Exception ignored) { }
        }
        long actions = PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_STOP
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SEEK_TO;
        session.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(newState, ms,
                        newState == PlaybackStateCompat.STATE_PLAYING ? 1.0f : 0.0f,
                        SystemClock.elapsedRealtime())
                .build());
    }

    private boolean requestAudioFocus() {
        return audioManager != null &&
                audioManager.requestAudioFocus(audioFocusListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Audio AutoBox", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Riproduzione musicale di AutoBox");
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }
    }

    private PendingIntent command(String action, int requestCode) {
        Intent intent = new Intent(this, AutoBoxMediaService.class).setAction(action);
        return PendingIntent.getService(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void showPlaybackNotification(boolean playingOrBuffering) {
        AutoBoxCatalog.Track t = position >= 0 && position < queue.size()
                ? queue.get(position) : null;
        if (t == null) return;
        Intent open = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openIntent = PendingIntent.getActivity(
                this, 45, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_media_status)
                .setContentTitle(t.title)
                .setContentText(t.artist)
                .setContentIntent(openIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setOngoing(playingOrBuffering)
                .addAction(R.drawable.ic_media_status, "Precedente", command(ACTION_PREVIOUS, 12))
                .addAction(R.drawable.ic_media_status,
                        playingOrBuffering ? "Pausa" : "Riproduci",
                        command(playingOrBuffering ? ACTION_PAUSE : ACTION_PLAY, 13))
                .addAction(R.drawable.ic_media_status, "Successivo", command(ACTION_NEXT, 14))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(session.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2))
                .build();
        if (playingOrBuffering) {
            try {
                startForeground(NOTIFICATION_ID, notification);
                foreground = true;
            } catch (Exception e) {
                // If the OS disallows a foreground service start, preserve the media session.
                ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                        .notify(NOTIFICATION_ID, notification);
            }
        } else {
            if (foreground) {
                stopForeground(STOP_FOREGROUND_DETACH);
                foreground = false;
            }
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, notification);
        }
    }

    private void releasePlayer() {
        prepared = false;
        if (player != null) {
            try { player.reset(); player.release(); } catch (Exception ignored) { }
            player = null;
        }
    }

    @Override public void onDestroy() {
        releasePlayer();
        audioManager.abandonAudioFocus(audioFocusListener);
        if (foreground) stopForeground(STOP_FOREGROUND_REMOVE);
        if (session != null) {
            session.setActive(false);
            session.release();
        }
        super.onDestroy();
    }
}

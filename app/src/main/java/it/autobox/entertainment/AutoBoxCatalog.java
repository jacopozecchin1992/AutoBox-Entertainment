package it.autobox.entertainment;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Audio files belonging to the user; no access to third-party streaming app catalogs. */
public final class AutoBoxCatalog {
    private AutoBoxCatalog() { }
    public static final String DEMO_ID = "autobox:demo";
    public static final String LOCAL_PREFIX = "local:";

    public static final class Track {
        public final String id;
        public final String title;
        public final String artist;
        public final Uri uri;
        public final long durationMs;
        public Track(String id, String title, String artist, Uri uri, long durationMs) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.uri = uri;
            this.durationMs = durationMs;
        }
    }

    public static String audioPermission() {
        return Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    public static boolean canReadPhoneAudio(Context context) {
        return context.checkSelfPermission(audioPermission()) == PackageManager.PERMISSION_GRANTED;
    }

    public static List<Track> getLocalTracks(Context context) {
        List<Track> tracks = new ArrayList<>();
        if (!canReadPhoneAudio(context)) return tracks;
        String[] columns = {
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION
        };
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                columns, MediaStore.Audio.Media.IS_MUSIC + " != 0", null,
                MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC")) {
            if (cursor == null) return tracks;
            while (cursor.moveToNext() && tracks.size() < 200) {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                long duration = cursor.isNull(3) ? 0L : cursor.getLong(3);
                if (title == null || title.trim().isEmpty()) title = "Brano senza titolo";
                if (artist == null || artist.trim().isEmpty() || artist.equals("<unknown>")) {
                    artist = "Musica del telefono";
                }
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                tracks.add(new Track(LOCAL_PREFIX + id, title, artist, uri, duration));
            }
        } catch (SecurityException ignored) {
            // Permission could be revoked between checking it and querying the provider.
        } catch (Exception ignored) {
            // A broken media entry must not prevent Android Auto discovering the service.
        }
        return tracks;
    }

    public static Track getDemo(Context context) {
        File f = new File(context.getFilesDir(), "autobox_demo_originale.wav");
        if (!f.exists() || f.length() < 100000) {
            try {
                createOriginalAudio(f);
            } catch (IOException ignored) { }
        }
        return new Track(DEMO_ID, "Atmosfera AutoBox", "Audio originale di prova",
                Uri.fromFile(f), 18000L);
    }

    /**
     * Generate an original 18-second, low-volume ambient chord offline.
     * The WAV is for testing genuine play/pause/skip in Android Auto.
     * It is not third-party music and requires no external downloads.
     */
    private static void createOriginalAudio(File file) throws IOException {
        final int rate = 22050;
        final int seconds = 18;
        final int samples = rate * seconds;
        final int bytes = samples * 2;
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            out.write(new byte[]{'R','I','F','F'});
            le32(out, bytes + 36);
            out.write(new byte[]{'W','A','V','E','f','m','t',' '});
            le32(out, 16);
            le16(out, 1); // PCM
            le16(out, 1); // mono
            le32(out, rate);
            le32(out, rate * 2);
            le16(out, 2);
            le16(out, 16);
            out.write(new byte[]{'d','a','t','a'});
            le32(out, bytes);
            for (int i = 0; i < samples; i++) {
                double t = (double)i / rate;
                double attack = Math.min(1.0, t / 1.1);
                double release = Math.min(1.0, (seconds - t) / 1.4);
                double envelope = Math.max(0.0, attack * release);
                double tremolo = 0.88 + 0.12 * Math.sin(2.0 * Math.PI * 0.21 * t);
                double a = 0.36 * Math.sin(2.0 * Math.PI * 174.61 * t);
                double b = 0.30 * Math.sin(2.0 * Math.PI * 261.63 * t);
                double c = 0.22 * Math.sin(2.0 * Math.PI * 329.63 * t);
                double d = 0.12 * Math.sin(2.0 * Math.PI * 392.00 * t);
                short value = (short) (Math.max(-1.0, Math.min(1.0, (a+b+c+d) *
                              tremolo * envelope * 0.25)) * 32767.0);
                out.write(value & 0xff);
                out.write((value >>> 8) & 0xff);
            }
        }
    }
    private static void le16(BufferedOutputStream out, int v) throws IOException {
        out.write(v & 0xff); out.write((v >>> 8) & 0xff);
    }
    private static void le32(BufferedOutputStream out, int v) throws IOException {
        out.write(v & 0xff); out.write((v >>> 8) & 0xff);
        out.write((v >>> 16) & 0xff); out.write((v >>> 24) & 0xff);
    }
}

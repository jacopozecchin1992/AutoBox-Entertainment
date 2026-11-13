package it.autobox.entertainment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.Gravity;
import java.util.ArrayList;
import java.util.List;

/** Simple local-audio player access on the phone; Android Auto supplies its own media UI. */
public class AudioLibraryActivity extends Activity {
    private static final int AUDIO_PERMISSION_REQUEST = 73;
    private ListView listView;
    private TextView summary;
    private Button permissionButton;
    private final List<AutoBoxCatalog.Track> tracks = new ArrayList<>();
    private final int cyan = Color.rgb(71, 239, 213);
    private final int navy = Color.rgb(9, 22, 36);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(navy);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(navy);
        root.setPadding(28, 24, 28, 16);

        TextView title = new TextView(this);
        title.setText("AutoBox · Musica");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        summary = new TextView(this);
        summary.setTextColor(Color.rgb(172, 190, 205));
        summary.setTextSize(14);
        summary.setPadding(0, 12, 0, 18);
        root.addView(summary);

        permissionButton = new Button(this);
        permissionButton.setText("Consenti accesso ai brani del Samsung");
        permissionButton.setOnClickListener(v -> requestPermissions(
                new String[]{AutoBoxCatalog.audioPermission()}, AUDIO_PERMISSION_REQUEST));
        root.addView(permissionButton);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(controls);
        Button pause = makeControl("Pausa");
        pause.setOnClickListener(v -> send(AutoBoxMediaService.ACTION_PAUSE, null));
        controls.addView(pause, new LinearLayout.LayoutParams(0, 55, 1));
        Button play = makeControl("Riprendi");
        play.setOnClickListener(v -> send(AutoBoxMediaService.ACTION_PLAY, null));
        controls.addView(play, new LinearLayout.LayoutParams(0, 55, 1));
        Button next = makeControl("Avanti");
        next.setOnClickListener(v -> send(AutoBoxMediaService.ACTION_NEXT, null));
        controls.addView(next, new LinearLayout.LayoutParams(0, 55, 1));

        listView = new ListView(this);
        listView.setDividerHeight(1);
        listView.setBackgroundColor(Color.rgb(17, 37, 56));
        root.addView(listView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        TextView note = new TextView(this);
        note.setText("I brani locali sono disponibili anche in Android Auto. Spotify e gli altri servizi mantengono le proprie app.");
        note.setTextColor(Color.rgb(165, 190, 204));
        note.setTextSize(13);
        note.setPadding(0, 14, 0, 6);
        root.addView(note);
        setContentView(root);
        reload();
    }

    private Button makeControl(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.rgb(10, 30, 45));
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(cyan));
        return button;
    }

    @Override public void onResume() {
        super.onResume();
        if (listView != null) reload();
    }

    private void reload() {
        boolean permission = AutoBoxCatalog.canReadPhoneAudio(this);
        permissionButton.setVisibility(permission ? View.GONE : View.VISIBLE);
        tracks.clear();
        tracks.add(AutoBoxCatalog.getDemo(this));
        if (permission) tracks.addAll(AutoBoxCatalog.getLocalTracks(this));
        summary.setText(permission
                ? (tracks.size()-1) + " brani sul telefono · 1 audio originale di prova"
                : "Riproduci la demo oppure autorizza i brani audio presenti sul telefono.");

        List<String> labels = new ArrayList<>();
        for (AutoBoxCatalog.Track track : tracks) {
            labels.add(track.title + "\n" + track.artist);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, labels);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < tracks.size()) {
                send(AutoBoxMediaService.ACTION_PLAY_ID, tracks.get(position).id);
            }
        });
    }

    private void send(String action, String mediaId) {
        Intent intent = new Intent(this, AutoBoxMediaService.class).setAction(action);
        if (mediaId != null) intent.putExtra(AutoBoxMediaService.EXTRA_MEDIA_ID, mediaId);
        startService(intent);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == AUDIO_PERMISSION_REQUEST) reload();
    }
}

package it.autobox.entertainment;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.car.app.validation.HostValidator;

/**
 * Android Auto service: actual point-of-interest and saved-destination functionality.
 * Phone-only video and entertainment links are not exposed on the driving display.
 */
public class AutoBoxCarService extends CarAppService {
    @NonNull
    @Override public HostValidator createHostValidator() {
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
        }
        return new HostValidator.Builder(this)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build();
    }

    @NonNull
    @Override public Session onCreateSession() {
        return new Session() {
            @NonNull
            @Override public Screen onCreateScreen(@NonNull Intent intent) {
                return new PlacesScreen(getCarContext());
            }
        };
    }

    private static class PlacesScreen extends Screen {
        PlacesScreen(@NonNull CarContext carContext) {
            super(carContext);
        }

        @NonNull
        @Override public Template onGetTemplate() {
            SharedPreferences prefs = getCarContext().getSharedPreferences("autobox_prefs", MODE_PRIVATE);
            String home = prefs.getString("place_home", "").trim();
            String work = prefs.getString("place_work", "").trim();

            ItemList.Builder list = new ItemList.Builder();
            if (!home.isEmpty()) addSavedPlace(list, "Casa", home);
            if (!work.isEmpty()) addSavedPlace(list, "Lavoro", work);

            if (home.isEmpty() && work.isEmpty()) {
                list.addItem(new Row.Builder()
                    .setTitle("Salva Casa e Lavoro")
                    .addText("Apri AutoBox sul telefono e modifica gli indirizzi")
                    .build());
            }
            // Genuine location searches handled by an Android Auto navigation host.
            addSearch(list, "Cerca parcheggi", "parcheggio vicino a me");
            addSearch(list, "Cerca distributori", "distributore di carburante vicino a me");
            addSearch(list, "Cerca colonnine", "stazione ricarica auto elettrica vicino a me");

            return new ListTemplate.Builder()
                    .setTitle("AutoBox · Luoghi")
                    .setSingleList(list.build())
                    .build();
        }

        private void addSavedPlace(ItemList.Builder list, String title, String address) {
            String safe = address.length() > 140 ? address.substring(0, 140) + "…" : address;
            list.addItem(new Row.Builder()
                    .setTitle(title)
                    .addText(safe)
                    .setOnClickListener(() -> startNavigation(address))
                    .build());
        }

        private void addSearch(ItemList.Builder list, String title, String query) {
            list.addItem(new Row.Builder()
                    .setTitle(title)
                    .addText("Cerca con un navigatore compatibile")
                    .setOnClickListener(() -> startNavigation(query))
                    .build());
        }

        private void startNavigation(String query) {
            try {
                Intent intent = new Intent(CarContext.ACTION_NAVIGATE);
                intent.setData(Uri.parse("geo:0,0?q=" + Uri.encode(query)));
                getCarContext().startCarApp(intent);
            } catch (Exception e) {
                CarToast.makeText(getCarContext(), "Navigatore non disponibile", CarToast.LENGTH_SHORT).show();
            }
        }
    }
}

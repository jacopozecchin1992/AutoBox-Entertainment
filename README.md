# AutoBox Entertainment Universale 3.1

AutoBox è un **launcher personalizzabile sul Samsung** e un **lettore audio Android Auto** che espone una libreria musicale tramite \`MediaBrowserServiceCompat\` / \`MediaSessionCompat\`. L'app è universale: nessuna scritta o vincolo per un particolare modello di automobile.

## Modalità coppia Android Auto 3.1

La Home rimane **identica alla 3.0**: due riquadri personalizzabili, cinque layout 30/70, 50/50, 70/30, Full SX e Full DX, e menu Home/App/Streaming.

Il nuovo pulsante **Coppia Android Auto** apre una finestra dedicata alle combinazioni guida + audio effettivamente previste dal sistema:

- Navigazione: **Waze** oppure **Google Maps**.
- Musica: **Spotify**, **YouTube Music** oppure **AutoBox Musica**.

Dopo **Salva coppia**, la scelta aggiorna e salva i due riquadri della Home sul Samsung. Il pannello mostra come aprire manualmente le due app sul display della Kia e tornare alla dashboard di Android Auto.

**Cosa non può fare**: AutoBox non apre automaticamente le finestre di altre app *dentro Android Auto*, non ne controlla la dashboard, non impone rapporto 30/70/50/50/70/30 sul display della Kia e non integra i contenuti di Spotify nel proprio lettore. Le app reali funzionano nel sistema Android Auto tramite le rispettive app ufficiali, anche con il telefono bloccato.

Fonti: https://developer.android.com/training/cars/platforms/android-auto e https://developers.google.com/cars/design/create-apps/media-apps/overview

## Sul Samsung — Home 3.0 personalizzabile

In cima sono presenti solo **Home, App, Streaming**.

La Home ha due riquadri **Sinistro** e **Destro**. Il pulsante **⚙ Personalizza Home** consente di scegliere separatamente:
Waze, Google Maps, Spotify, YouTube Music, WhatsApp, Messaggi, Telefono, AutoBox Musica.

Le scelte vengono **salvate** sul telefono. I valori iniziali sono Waze e Spotify. Seleziona una fra **cinque modalità**:

- **30/70**, **50/50**, **70/30** per l'anteprima dei due riquadri interni;
- **Full SX**, **Full DX** per mostrare un solo riquadro di AutoBox;
- **Apri ... a tutto schermo** sul singolo riquadro, per lanciare direttamente l'**app Android ufficiale** selezionata;
- **Dashboard intera** per nascondere le barre del Samsung e vedere la sola app AutoBox; si esce con il pulsante dedicato o con Indietro.

**Nota tecnica:** i riquadri sono delle anteprime e scorciatoie personalizzabili, NON finestre reali di altre app. Un'app Android non può incorporare indiscriminatamente le schermate di Waze, Spotify o WhatsApp in una WebView. Per la vera visualizzazione di **due app Android affiancate**, usa la funzione **Schermo diviso / Multi Window** del Samsung dalla schermata delle app recenti, scegliendo due app compatibili.

Il pulsante **Split Samsung** spiega come affiancarle. GPS e destinazioni restano disponibili dalla Home; gli indirizzi sono inviati a Waze sul Samsung. Le pagine **App** e **Streaming** mantengono tutti i collegamenti diretti alle app installate. Lo streaming video è destinato soltanto al telefono e esclusivamente durante la sosta.

## Sul display Android Auto — telefono con schermo spento

Android Auto **non** consente a un APK normale di sostituire la dashboard di sistema con l'interfaccia Waze+Spotify personalizzabile né di imporre un split-screen 30/70 o Full SX/DX. La schermata è disegnata e controllata dall'host Android Auto. Le app ammesse si utilizzano individualmente tramite l'interfaccia del sistema.

AutoBox dichiara il servizio multimediale **MediaBrowserServiceCompat** e una **MediaSessionCompat** con navigazione della libreria, play/pausa/avanti/indietro e notifica media. La riproduzione locale viene mantenuta in un servizio in primo piano con supporto al blocco schermo. Le raccolte sono:

- **Musica sul Samsung** (file audio salvati sul telefono, previo permesso);
- **Ascolta AutoBox** (audio originale di prova incluso).

Per **Waze** e **Spotify** sul display dell'auto, usa le **loro app Android Auto ufficiali**. Se il display supporta la dashboard di Android Auto, può mostrarle affiancate secondo le modalità e le scelte del sistema. Non è una funzione controllabile da AutoBox.

### Test dell'APK senza Play Console

1. Installa l'APK sul Samsung. **Attenzione:** le build di GitHub Actions sono debug e la firma può cambiare fra compilazioni. Per un errore di firma è possibile che sia necessario disinstallare la versione precedente, perdendo gli indirizzi salvati.
2. Prova la Home 3.0, salva i due riquadri e prova i cinque layout.
3. Entra in **App → AutoBox Musica** e prova l'audio demo; concede l'accesso ai file audio se vuoi ascoltare i tuoi brani.
4. Android Auto → impostazioni sviluppatore → **Origini sconosciute** (quando disponibile per le app media installate esternamente).
5. Android Auto → **Personalizza Avvio app** → cerca **AutoBox · Musica**. Ricollega il telefono, avvia il lettore, quindi blocca lo schermo del Samsung per verificare la riproduzione con i controlli dell'auto.

La corretta compilazione e firma dell'APK non garantiscono che sia visibile su tutti i veicoli: è necessario il test sul sistema reale.

Documentazione ufficiale Android: 
- https://developer.android.com/training/cars/media
- https://developer.android.com/training/cars/media/create-media-browser
- https://developer.android.com/design/ui/cars/guides/app-types/create-media-apps

## Build

Il workflow GitHub Actions usa Java 17, Android SDK 35 e Gradle 8.10.2 per compilare \`assembleDebug\`, verificare manifest e firma, e pubblicare APK + anteprima HTML. Package: \`it.autobox.entertainment\`.

# AutoBox Entertainment Universale 3.3

AutoBox è un **launcher personalizzabile sul Samsung** e un **lettore audio Android Auto** che espone una libreria musicale tramite \`MediaBrowserServiceCompat\` / \`MediaSessionCompat\`. L'app è universale: nessuna scritta o vincolo per un particolare modello di automobile.

## AutoBox 3.3 — Launcher per un Android AI Box

Questa versione aggiunge **codice nativo Android** oltre alla Home originale. La funzione è prevista per l'installazione su un **dispositivo con Android completo**, ad esempio un AI Box compatibile con la vettura, e non sostituisce Android Auto sul display della Kia tramite il normale collegamento Samsung.

Nella Home è disponibile il pulsante **Modalità Box**:
- **Avvia modalità Box**: amplia il launcher e attiva l'interfaccia immersiva, mantenendo visibili menu, riquadri, proporzioni e dock;
- **Tutte le app**: cerca e apre le applicazioni che dichiarano un launcher Android o Leanback; l'elenco viene letto nativamente dal PackageManager e non richiede permesso `QUERY_ALL_PACKAGES`;
- **Personalizza Home**: include ora anche le altre applicazioni installate e scoperte dal catalogo, oltre agli 8 collegamenti già presenti; le scelte sono salvate sul dispositivo;
- **Abilita scelta Home**: mostra una conferma e, solo con azione esplicita, rende disponibile AutoBox tra i launcher HOME del dispositivo. Si apre quindi la schermata di sistema per scegliere l'app Home; **non viene imposto come predefinito**;
- **Disabilita scelta Home**: toglie AutoBox dall'elenco dei launcher selezionabili. Su un telefono, lasciare questa opzione spenta.

Queste funzionalità sfruttano le API pubbliche di Android (`CATEGORY_HOME`, `activity-alias`, `PackageManager`). L'**Android AI Box deve essere un hardware Android completo**, con supporto al lancio delle app e, per le coppie di app reali, alle finestre multiple di sistema. Non tutte le app accettano Multi Window. I cinque layout (30/70, 50/50, 70/30, Full SX e Full DX) continuano ad agire sui **riquadri del launcher AutoBox** e **non incorporano le finestre di Waze e Spotify**.

### Limite che rimane invariato

Un APK installato sul Samsung e collegato alla Kia tramite **Android Auto** non può assumere il ruolo di launcher del sistema dell'auto né proiettare arbitrariamente il proprio desktop su quello schermo. Per usare AutoBox 3.3 come launcher a pieno schermo direttamente in auto occorre installarlo su un AI Box realmente compatibile, separato dal Samsung. Su Android Auto il servizio musica AutoBox continua a usare il proprio MediaBrowserService e lo schermo è gestito dall'host Google.

Documentazione Android:
- https://developer.android.com/reference/android/content/Intent#CATEGORY_HOME
- https://developer.android.com/reference/android/content/pm/PackageManager#setComponentEnabledSetting(android.content.ComponentName,%20int,%20int)
- https://developer.android.com/training/cars/platforms/android-auto

## Aprire due vere app sul Samsung — versione 3.2

La 3.1 permetteva soltanto di salvare un abbinamento e visualizzava i due riquadri grafici di AutoBox; non poteva mostrare le vere app all'interno della Home.

La 3.2 aggiunge un pulsante **Apri la coppia sul Samsung**. Scegli prima le due app tramite **Personalizza Home**, poi:

1. Tocca **Apri la coppia sul Samsung**.
2. Tocca **1 · Affianca [prima app] ad AutoBox**: AutoBox chiede ad Android di aprire l'app scelta in una finestra adiacente usando gli intent flag pubblici `FLAG_ACTIVITY_LAUNCH_ADJACENT | FLAG_ACTIVITY_NEW_TASK`.
3. Se AutoBox rimane nella metà libera, tocca **2 · Apri [seconda app] nell'altra metà** per provare a sostituirlo con la seconda app.
4. Il posizionamento non è garantito da Android: se non riesce, apri entrambe da **App recenti → icona della prima app → Apri in visualizzazione a schermo diviso → seleziona la seconda app**.
5. Tocca il **divisore con tre puntini** e **stella / Crea coppia di app** per salvare la coppia nella schermata Home o nel pannello Edge. Dopo averla salvata, Samsung consente di riaprire entrambe con un solo tocco.

Queste operazioni aprono le app **sullo schermo del telefono**, fuori dalla Home AutoBox (i riquadri restano scorciatoie visive, non app incorporate). La richiesta di lancio affiancato è sperimentale e deve essere verificata sul Samsung Galaxy S25 Ultra: il sistema potrebbe ignorarla oppure alcune app potrebbero non consentire multi-window. Non richiede permessi speciali e non modifica le regole di Android Auto.

Fonti ufficiali:
- https://developer.android.com/develop/ui/views/layout/support-multi-window-mode
- https://www.samsung.com/us/support/answer/ANS10002022/

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

# AutoBox Entertainment Universale 2.9.0

AutoBox è un **launcher sul Samsung** e una **vera app audio su Android Auto**.

## Menu semplificato 2.9.0

Il menu nella parte alta dell'app Samsung contiene **solo Home, App e Streaming**.
Sono stati rimossi il pulsante **Musica** e i sottotitoli descrittivi dalle schermate **Applicazioni** e **Streaming**.
La Home resta dedicata a Waze e Spotify, con il cruscotto a schermo intero e le proporzioni 7:3 / 5:5 / 3:7.
La musica non scompare: Spotify rimane nella Home e nella pagina App; **AutoBox Musica** è ancora accessibile dalla pagina App e dalla dashboard per riprodurre brani locali. La distribuzione e l'interfaccia Android Auto restano invariate rispetto alla versione audio.

## AutoBox Drive — dashboard

La schermata Samsung ha un nuovo cruscotto a due pannelli **in stile AI Box**:
- Navigazione, destinazioni, indirizzi Casa/Lavoro e velocità GPS a sinistra;
- Musica AutoBox (play, pausa, precedente, successivo) a destra, con collegamenti diretti a Spotify e YouTube Music;
- Layout 7:3, 5:5 e 3:7 selezionabili e salvati sul telefono, disponibili affiancati quando lo schermo è abbastanza ampio (es. telefono orizzontale);
- Barra inferiore con Maps, Waze, Spotify e WhatsApp;
- Pannello di guida «Split Samsung» per utilizzare **due vere app Android** tramite One UI Multi Window.

La mappa visibile nel nuovo cruscotto è **schematica**, non una cartografia live. Il pulsante Maps o l'inserimento della destinazione apre direttamente Google Maps Android. Il cruscotto divide **due componenti di AutoBox**: non può incorporare arbitrariamente l'interfaccia di Spotify, Netflix o Maps. Se desideri due vere app Samsung affiancate, usa lo schermo diviso One UI: Apri App recenti → icona Google Maps → Apri in visualizzazione a schermo diviso → Spotify; usa il divisore per le proporzioni e salva eventualmente la coppia.

**Android Auto:** il servizio multimediale di AutoBox continua a funzionare con MediaBrowserServiceCompat e MediaSessionCompat, e Android Auto decide il layout. Se supporta la modalità dashboard, può affiancare la musica AutoBox a Google Maps; l'APK non impone split screen, ratio, né pannelli video all'auto.

---

## Samsung: tutte le scorciatoie originali

La schermata AutoBox mantiene GPS, destinazione, Casa/Lavoro e apertura **diretta** delle app ufficiali installate: Google Maps, Waze, Spotify, YouTube Music, WhatsApp, Telefono, Messaggi, YouTube, Netflix, Disney+ e Prime Video. Se un'app non è installata, AutoBox mostra un avviso; non apre un sito web.

È stato aggiunto il pulsante **AutoBox Musica**, che apre il lettore locale. Da questa schermata puoi concedere accesso alla musica sul Samsung, scegliere un brano e utilizzare i controlli audio. I contenuti video sul telefono vanno usati esclusivamente da parcheggiati.

## Android Auto: media browser e MediaSession

La versione 2.5.0 **non usa Car App Library per le schermate del display**, ma il servizio MediaBrowserServiceCompat / MediaSessionCompat ufficialmente supportato nella categoria media. Android Auto mostra l'interfaccia standard di navigazione musicale con:

- **Musica sul Samsung**: fino a 200 file audio locali indicizzati dal sistema e autorizzati dall'utente;
- **Ascolta AutoBox**: un audio ambient originale di prova incluso, riproducibile anche senza permesso per la musica;
- i comandi Riproduci, Pausa, Precedente, Successivo, Ricerca e posizione di riproduzione;
- notifica musicale e servizio in primo piano durante l'audio.

Android Auto **non visualizza un launcher arbitrario di app terze**, né Netflix, Disney+, Prime Video e YouTube. Spotify e YouTube Music devono essere utilizzati tramite le rispettive app Android Auto; AutoBox non legge né riproduce il loro catalogo protetto.

### Prova senza Google Play Console

La modalità sviluppatore **Origini sconosciute** in Android Auto si applica alle vere app **media** (non alle app costruite con Android for Cars App Library). Per testare:

1. Installa l'APK v2.5.0 sul Galaxy S25 Ultra. Se il precedente debug APK ha firma diversa, potrebbe essere necessaria la disinstallazione (salva prima Casa/Lavoro perché i dati locali verrebbero cancellati).
2. Apri AutoBox sul Samsung e tocca **AutoBox Musica**. L'audio demo funziona senza permessi; se hai MP3, tocca **Consenti accesso ai brani del Samsung**.
3. In Android Auto sul Samsung abilita **modalità sviluppatore** e dal menu sviluppatore seleziona **Origini sconosciute**.
4. In **Android Auto → Personalizza Avvio app** cerca **AutoBox · Musica**.
5. Ricollega Android Auto; apri AutoBox nella sezione musicale e prova **Ascolta AutoBox** e **Musica sul Samsung**.
6. Prova esclusivamente da fermo. Se non compare, invia uno screenshot di **Personalizza Avvio app** e dei permessi/app installate per esaminare il comportamento.

Non è garantita la comparsa su qualsiasi sistema multimediale; un APK compilato non sostituisce il test reale.

Documentazione Android ufficiale:
- https://developer.android.com/training/cars/media/auto
- https://developer.android.com/training/cars/media/create-media-browser
- https://developer.android.com/training/cars/testing

## Compilazione

GitHub Actions esegue Gradle e controlla manifest, archivio e firma dell'APK prima di pubblicare la build direttamente in GitHub Releases. Gli APK debug vengono firmati da GitHub Actions e potrebbero avere certificati diversi fra esecuzioni.

Progetto: it.autobox.entertainment. Licenze dei contenuti musicali sul telefono rimangono ai rispettivi proprietari; AutoBox non offre servizi streaming di terze parti.

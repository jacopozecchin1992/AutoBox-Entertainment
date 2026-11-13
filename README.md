# AutoBox Entertainment Universale

AutoBox è un'app Android per smartphone con schermata dedicata a GPS, destinazioni, comunicazioni e intrattenimento.

## Funzioni sul Samsung

I pulsanti del launcher aprono direttamente **le applicazioni Android installate**, senza aprire i rispettivi siti nel browser: Google Maps, Waze, Spotify, YouTube Music, WhatsApp, SMS, telefono, YouTube, Netflix, Disney+ e Prime Video. Se manca un'app, AutoBox mostra un avviso e non reindirizza al sito. Le app video devono essere usate solo quando il veicolo è parcheggiato.

## Funzioni su Android Auto

Il servizio Android Auto attuale è una piccola app **POI / Luoghi** con gli indirizzi Casa e Lavoro salvati sul telefono e ricerche di parcheggi, distributori e colonnine. Non è una superficie di mirroring e non permette di mostrare video o aprire le app video dalla schermata dell'auto.

**Una installazione APK da GitHub non è sufficiente per visualizzare le app Android for Cars App Library su una vettura reale.** L'opzione sviluppatore «Origini sconosciute» non si applica a questa tipologia di app. Per provarla sull'auto, caricare l'APK su **Google Play Console > Condivisione interna delle app (Internal app sharing)**, quindi **installarla sul telefono usando il link Google Play**, non un'installazione sideload da GitHub.

Documentazione ufficiale:
- https://developer.android.com/training/cars/testing
- https://support.google.com/googleplay/android-developer/answer/9844679

### Passaggi per test Android Auto

1. Avere accesso a Google Play Console con un'app configurata per il package `it.autobox.entertainment`.
2. Aprire la pagina [Internal app sharing](https://play.google.com/console/about/internalappsharing/) e caricare l'APK debug della release GitHub.
3. Aprire il **link generato da Google Play** sul Galaxy e installare l'app da quel link. Se Android segnala una firma differente, disinstallare la precedente versione sideload, dopo aver annotato gli indirizzi salvati.
4. Collegare il Galaxy a Android Auto e aprire **Personalizza Avvio app**. Cercare *AutoBox · Luoghi*.
5. Verificare l'esperienza e i requisiti Google prima di procedere verso distribuzione pubblica.

Le app video non si aprono sul display Android Auto tramite AutoBox. Il logo dell'app resta universale.

## Compilazione

GitHub Actions esegue `gradle :app:assembleDebug`, verifica APK e firma digitale e pubblica l'APK nelle Releases. La compilazione garantisce un artefatto valido, ma non dimostra la compatibilità con una specifica automobile.

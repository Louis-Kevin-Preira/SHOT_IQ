# Créer l'APK de SHOT IQ

Ce dossier contient deux choses : l'application web à la racine, et un projet Android dans `android/` qui l'embarque. L'app Android n'est pas une réécriture, c'est une enveloppe : elle affiche les fichiers de `android/app/src/main/assets/www` dans une WebView servie sous `https://appassets.androidplatform.net/`, donc dans un contexte sécurisé. Tes séances survivent aux mises à jour, la voix et le maintien de l'écran fonctionnent, et l'application ne demande qu'une permission, la vibration. Aucune permission réseau : elle ne peut communiquer avec personne.

Quatre méthodes, de la plus simple à la plus complète. **La méthode 2 est celle que je recommande** : rien à installer, et tu gardes le code.

---

## Avant toute chose

Si tu as modifié `index.html`, recopie l'application web dans les ressources Android :

```bash
cd android
./synchroniser-web.sh        # sur Windows : synchroniser-web.bat
```

La méthode 2 le fait toute seule à chaque compilation.

---

## Méthode 1 — PWABuilder, sans rien installer

La plus rapide. Elle part de l'application en ligne, pas de ce dossier.

1. Mets l'application en ligne : va sur `app.netlify.com/drop` et glisse le dossier (celui qui contient `index.html`) dans la page. Tu obtiens une adresse en `https`.
2. Va sur `pwabuilder.com`, colle l'adresse, lance l'analyse.
3. Choisis **Android**, puis **Generate Package**.
4. Tu reçois une archive avec un `.apk` signé, un `.aab` pour le Play Store, et le fichier de signature.

Conserve précieusement la clé fournie : sans elle, tu ne pourras plus publier de mise à jour de la même application.

Deux limites : tu ne contrôles pas le code produit, et l'application ainsi générée vérifie ton adresse en ligne au démarrage sur certaines configurations. Les méthodes suivantes produisent une app entièrement autonome.

---

## Méthode 2 — GitHub Actions, compilation dans le cloud

Gratuit, sans rien installer, et l'APK se reconstruit à chaque modification.

1. Crée un compte sur `github.com`, puis un dépôt vide.
2. Envoie ce dossier dedans, en gardant la structure : `.github` doit être à la racine, à côté de `index.html` et de `android/`.

```bash
cd /chemin/vers/ce/dossier
git init
git add .
git commit -m "SHOT IQ"
git branch -M main
git remote add origin https://github.com/TON_COMPTE/TON_DEPOT.git
git push -u origin main
```

3. Sur GitHub, ouvre l'onglet **Actions**. La compilation démarre toute seule ; tu peux aussi la lancer à la main avec **Run workflow**.
4. Cinq à dix minutes plus tard, une coche verte apparaît. Ouvre l'exécution et télécharge l'artefact **shot-iq-apk-test**, qui contient `app-debug.apk`.

La recette recopie l'application web dans les ressources avant de compiler : tu n'as jamais à y penser.

---

## Méthode 3 — Android Studio

Si tu veux modifier l'application et la tester sur un appareil branché.

1. Installe Android Studio, qui apporte le SDK et Gradle.
2. **Open**, puis sélectionne le dossier `android/` — pas la racine.
3. Laisse Gradle se synchroniser au premier ouverture, le temps qu'il télécharge ses dépendances.
4. **Build**, **Build Bundle(s) / APK(s)**, **Build APK(s)**.
5. L'APK apparaît dans `android/app/build/outputs/apk/debug/`.

Pour l'installer directement sur un téléphone branché en USB avec le débogage activé : bouton **Run**.

---

## Méthode 4 — En ligne de commande

Pour qui préfère le terminal. Il faut Java 17 et le SDK Android.

```bash
# 1. Java 17
sudo apt install openjdk-17-jdk          # Debian, Ubuntu

# 2. Outils en ligne de commande du SDK Android
mkdir -p ~/android-sdk/cmdline-tools && cd ~/android-sdk/cmdline-tools
# télécharge "Command line tools only" depuis developer.android.com/studio
unzip commandlinetools-linux-*.zip && mv cmdline-tools latest
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# 3. Composants nécessaires
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
yes | sdkmanager --licenses

# 4. Compilation
cd /chemin/vers/ce/dossier/android
./synchroniser-web.sh
gradle assembleDebug          # ou : ./gradlew assembleDebug si tu ajoutes le wrapper
```

L'APK sort dans `app/build/outputs/apk/debug/app-debug.apk`.

---

## Installer l'APK sur le téléphone

Transfère le fichier sur l'appareil, puis ouvre-le depuis l'application Fichiers. Android demandera l'autorisation d'installer des applications provenant de cette source : accorde-la au gestionnaire de fichiers, puis confirme.

Si un APK de la même application est déjà installé avec une signature différente, désinstalle-le d'abord.


---

## L'installation échoue par-dessus une version déjà présente

C'est le piège le plus courant, et il a une cause précise : **Android refuse d'installer une application par-dessus une autre du même nom si les deux ne sont pas signées par la même clé.**

Par défaut, chaque compilation dans le cloud fabrique une clé de débogage neuve. La première installation passe, toutes les suivantes échouent avec « Application non installée » ou « conflit avec un paquet existant ».

### La solution, déjà en place

Le projet contient une clé fixe dans `android/keystore/shotiq.jks`, utilisée par toutes les compilations, en test comme en version. Tous les APK produits à partir de ce dossier sont donc signés de la même façon et s'installent les uns par-dessus les autres.

Le numéro de version monte aussi tout seul : il reprend le numéro d'exécution GitHub, ce qui évite l'autre cause de refus, celle où Android considère que tu tentes d'installer une version plus ancienne.

### Ce qu'il faut faire une fois, maintenant

Les APK que tu as déjà installés portent une signature aléatoire. Il faut donc les désinstaller une dernière fois avant de passer aux nouveaux.

**Avant de désinstaller, exporte tes données.** Ouvre l'application, va dans Réglages, section Sauvegarde, bouton **Sauvegarder** : tu obtiens un fichier JSON. La désinstallation efface tout ce que l'application a stocké, séances et historique compris.

Ensuite : désinstalle SHOT IQ depuis les réglages Android, installe le nouvel APK, puis dans Réglages, **Restaurer**, et choisis ton fichier de sauvegarde.

À partir de là, chaque nouvel APK s'installera par-dessus le précédent sans rien perdre.

### À propos de cette clé

Elle est incluse dans le dossier, mot de passe compris, pour que tout fonctionne sans configuration. C'est un choix assumé pour une application personnelle distribuée de la main à la main.

Si tu publies un jour sur le Play Store, remplace-la par une clé que tu gardes privée :

```bash
keytool -genkeypair -v -keystore android/keystore/shotiq.jks \
  -alias shotiq -keyalg RSA -keysize 2048 -validity 10950
```

Mets alors les mots de passe dans des secrets GitHub plutôt que dans `build.gradle`, et ne versionne plus le fichier `.jks`. Attention : une fois une application publiée, cette clé devient irremplaçable. La perdre, c'est ne plus jamais pouvoir mettre à jour l'application sous le même nom.

---

## Vérifier que deux APK partagent la même signature

Si un doute subsiste, compare leurs empreintes :

```bash
keytool -printcert -jarfile app-debug.apk | grep SHA256
```

Deux APK qui s'installent l'un sur l'autre affichent la même ligne SHA256. Celle de la clé fournie commence par `BC:22:24:36`.

---

## APK de test et APK de version

Les deux sont désormais signés par la clé du projet, donc interchangeables à l'installation. L'artefact **shot-iq-apk-test** est celui à utiliser au quotidien ; **shot-iq-apk-version** est compilé sans les outils de débogage, un peu plus léger et plus rapide.

Pour le Play Store, il faudra remplacer la clé fournie par une clé privée à toi, comme expliqué plus haut.

---

## Mettre à jour l'application

Modifie `index.html` à la racine, puis :

- **Méthode 2** : commit et push, l'APK se reconstruit tout seul.
- **Méthodes 3 et 4** : lance `./synchroniser-web.sh` depuis `android/`, puis recompile.

Le `versionCode` se met à jour tout seul en compilation cloud : il suit le numéro d'exécution GitHub. En compilation locale, il vaut 1 par défaut ; tu peux le forcer avec `VERSION_CODE=7 gradle assembleDebug`.

---

## Ce que la version Android change

Au lancement, Android affiche le logo sur fond navy pendant le chargement de la WebView, puis l'animation Fable enchaîne : aucun écran blanc entre les deux.

Le bouton retour ferme d'abord ce qui est ouvert dans l'application — une fiche, le mode mains libres, la séance en cours — avant de quitter.

Les rappels restent ceux du navigateur embarqué et ne se déclenchent pas quand l'application est fermée. Le bandeau de rappel à l'ouverture, lui, fonctionne. Des notifications planifiées demanderaient du code natif que cette enveloppe ne contient pas.

---

## Et sur iPhone

Il n'existe pas d'équivalent de l'APK. Publier sur l'App Store demande un Mac avec Xcode et un compte développeur Apple à 99 dollars par an. En attendant, l'ajout à l'écran d'accueil depuis Safari donne le même résultat à l'usage : plein écran, icône, fonctionnement hors ligne.

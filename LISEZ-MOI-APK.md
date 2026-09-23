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

## APK de test et APK de version

L'APK de test est signé avec une clé de débogage commune à tous les projets Android. Il s'installe et fonctionne normalement, mais il ne peut pas être publié sur le Play Store.

Pour une version publiable, crée ta clé une seule fois :

```bash
keytool -genkey -v -keystore shotiq.jks -keyalg RSA -keysize 2048 -validity 10000 -alias shotiq
```

Puis décommente le bloc `signingConfigs` dans `android/app/build.gradle` et la ligne `signingConfig signingConfigs.release` juste en dessous, et compile avec `gradle assembleRelease`.

Sauvegarde le fichier `.jks` et ses mots de passe ailleurs que dans le dépôt : perdre cette clé signifie ne plus jamais pouvoir mettre à jour l'application publiée. Le `.gitignore` fourni exclut déjà les fichiers `.jks` et `.keystore`.

---

## Mettre à jour l'application

Modifie `index.html` à la racine, puis :

- **Méthode 2** : commit et push, l'APK se reconstruit tout seul.
- **Méthodes 3 et 4** : lance `./synchroniser-web.sh` depuis `android/`, puis recompile.

Pense à incrémenter `versionCode` et `versionName` dans `android/app/build.gradle` à chaque version distribuée, sinon Android refusera l'installation par-dessus la précédente.

---

## Ce que la version Android change

Au lancement, Android affiche le logo sur fond navy pendant le chargement de la WebView, puis l'animation Fable enchaîne : aucun écran blanc entre les deux.

Le bouton retour ferme d'abord ce qui est ouvert dans l'application — une fiche, le mode mains libres, la séance en cours — avant de quitter.

Les rappels restent ceux du navigateur embarqué et ne se déclenchent pas quand l'application est fermée. Le bandeau de rappel à l'ouverture, lui, fonctionne. Des notifications planifiées demanderaient du code natif que cette enveloppe ne contient pas.

---

## Et sur iPhone

Il n'existe pas d'équivalent de l'APK. Publier sur l'App Store demande un Mac avec Xcode et un compte développeur Apple à 99 dollars par an. En attendant, l'ajout à l'écran d'accueil depuis Safari donne le même résultat à l'usage : plein écran, icône, fonctionnement hors ligne.

# SHOT IQ

Carnet d'entraînement au tir, par Fable.

Séances guidées en trois blocs avec chronos, saisie tir par tir, mode mains libres, annonces vocales, carte de chaleur, programme d'un mois et rapports de séance.

Application web, sans dépendance, sans serveur applicatif. Elle fonctionne hors ligne et garde toutes les données sur l'appareil.

---

## Contenu

| Fichier | Rôle |
|---|---|
| `index.html` | L'application entière |
| `manifest.webmanifest` | Nom, icônes et mode d'affichage pour l'installation |
| `sw.js` | Service worker : mise en cache et fonctionnement hors ligne |
| `icons/` | Icônes 192, 512, maskable, Apple, favicons, glyphe simplifié |
| `media/` | Ouverture Fable, logo et nom de l'application |
| `lancer.py` | Petit serveur local pour essayer et installer depuis ton ordinateur |
| `android/` | Projet Android pour produire un APK, voir `LISEZ-MOI-APK.md` |
| `.github/` | Recette qui compile l'APK dans le cloud |

---

## L'essayer tout de suite

Ouvre `index.html` d'un double-clic. Tout fonctionne : séances, chronos, voix, mains libres, historique. Deux réserves seulement dans ce mode, liées aux règles des navigateurs sur les fichiers locaux : l'application ne peut pas s'installer, et l'animation Fable laisse la place à l'animation de repli.

## La mettre en ligne

Il faut une adresse en `https` pour que l'installation, le hors ligne et la vidéo fonctionnent partout.

- **Netlify Drop** : va sur `app.netlify.com/drop` et glisse ce dossier dans la page. Adresse obtenue en quelques secondes, sans compte.
- **GitHub Pages** : dépose les fichiers dans un dépôt, puis active Pages sur la branche principale.
- **Cloudflare Pages**, **Vercel** : même principe.

Aucun code serveur ici : n'importe quel hébergement de fichiers statiques convient.

## L'installer comme une application

Une fois en ligne, ouvre l'adresse et installe-la. Elle obtient alors sa propre fenêtre, son icône, et démarre sans réseau.

- **Android, Chrome** : menu `⋮` puis **Installer l'application**.
- **iPhone, Safari** : bouton **Partager** puis **Sur l'écran d'accueil**.
- **Ordinateur, Chrome ou Edge** : icône d'installation à droite de la barre d'adresse.

Firefox et Safari sur ordinateur n'installent pas les applications web ; l'app reste utilisable dans un onglet.

Pour essayer l'installation sans rien mettre en ligne, lance `python3 lancer.py` depuis ce dossier : il sert le projet sur `http://localhost:8000`, ce qui suffit aux navigateurs pour proposer l'installation.

---

## L'écran de démarrage

L'ouverture se fait en deux temps : l'animation Fable avec son son, puis le logo et le nom de l'application. L'ensemble ne peut pas être interrompu, c'est la signature du créateur.

Si le navigateur refuse de lancer une vidéo sonore — c'est le cas de la plupart d'entre eux tant que l'utilisateur n'a pas interagi avec la page — l'application rejoue aussitôt la version muette, sans rien demander. Le son passe en revanche dans l'application installée. Un rechargement dans la même session ne rejoue pas l'ouverture, et un système réglé sur animations réduites va directement à l'écran de marque.

| Fichier | Rôle |
|---|---|
| `media/splash-integral.mp4` | L'animation avec le son |
| `media/splash-muet.mp4` | La même, sans piste audio, utilisée en repli |
| `media/brand-logo.png` | Le logo affiché après l'animation |
| `media/brand-word.png` | Le nom affiché après l'animation |

Le fichier lu est `media/splash-integral.mp4`, à vitesse d'origine. Une version accélérée de trois secondes et demie est fournie dans `media/splash.mp4` : pour l'utiliser, remplace le nom du fichier dans la ligne `v.src=` de `index.html`.

---

## Les tutoriels

À la première ouverture de chaque fonction — le déroulé d'une séance, le mode mains libres, la carte des tirs, le rapport, le programme, les progrès — l'application propose un guide court. Deux boutons : suivre le guide, ou passer. Dans les deux cas la fonction reste accessible et la proposition ne revient plus.

Les Réglages listent les six tutoriels : tu peux en rejouer un à la demande, ou tout réinitialiser.

## Raccourcis clavier

| Touche | Action |
|---|---|
| `1` à `4` | Changer d'onglet |
| `Espace` | Démarrer ou mettre en pause le bloc |
| `M` ou `←` | Tir marqué |
| `R` ou `→` | Tir raté |
| `S` | Ficelle, en mode Avancé |
| `Z` | Annuler le dernier tir |
| `Entrée` | Valider le bloc |
| `H` | Mode mains libres |
| `Échap` | Quitter le mode mains libres ou la séance |

---

## Tes données

Tout est stocké dans le navigateur de l'appareil. Rien ne part sur un réseau.

Deux conséquences : vider les données du navigateur efface l'historique, et chaque appareil garde le sien. Les boutons **Sauvegarder** et **Restaurer** dans les Réglages servent à faire une copie ou à passer d'un appareil à l'autre. L'export **CSV** ouvre tes séances dans Excel.

---

## Modifier l'application

Tout tient dans `index.html` : styles en haut, application en bas, aucune compilation. Après une modification, incrémente la version en tête de `sw.js` (`shot-iq-v10`), sinon les appareils déjà installés garderont l'ancienne copie en cache.

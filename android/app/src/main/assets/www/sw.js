/* SHOT IQ — service worker
   Stratégie : « stale-while-revalidate » sur la coquille de l'application.
   L'app fonctionne hors ligne dès la première visite ; une nouvelle version
   est récupérée en arrière-plan et prend effet au rechargement suivant. */

const VERSION = "shot-iq-v12";
const SHELL = [
  "./",
  "./index.html",
  "./manifest.webmanifest",
  "./icons/icon-192.png",
  "./icons/icon-512.png",
  "./icons/icon-maskable-512.png",
  "./icons/apple-touch-icon.png",
  "./icons/favicon-32.png",
  "./media/splash-integral.mp4",
  "./media/splash-muet.mp4",
  "./media/brand-logo.png",
  "./media/brand-word.png",
  "./media/splash-poster.jpg"
];

self.addEventListener("install", (e) => {
  e.waitUntil(
    caches.open(VERSION)
      .then((c) => c.addAll(SHELL))
      .then(() => self.skipWaiting())
      .catch(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (e) => {
  e.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== VERSION).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (e) => {
  const req = e.request;
  if (req.method !== "GET") return;
  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  e.respondWith(
    caches.open(VERSION).then(async (cache) => {
      const cached = await cache.match(req, { ignoreSearch: true });
      const network = fetch(req)
        .then((res) => {
          if (res && res.ok) cache.put(req, res.clone());
          return res;
        })
        .catch(() => null);

      if (cached) {
        network.catch(() => {});
        return cached;
      }
      const res = await network;
      if (res) return res;
      // hors ligne et rien en cache : on renvoie la page d'accueil pour les navigations
      if (req.mode === "navigate") {
        const home = await cache.match("./index.html");
        if (home) return home;
      }
      return new Response("Hors ligne", { status: 503, statusText: "Hors ligne" });
    })
  );
});

self.addEventListener("message", (e) => {
  if (e.data === "skipWaiting") self.skipWaiting();
});

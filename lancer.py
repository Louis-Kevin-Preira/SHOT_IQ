#!/usr/bin/env python3
"""
SHOT IQ — petit serveur local.

Un service worker (donc l'installation et le mode hors ligne) refuse de
fonctionner depuis un fichier ouvert directement (file://). Ce script sert le
dossier sur http://localhost:8000 et ouvre le navigateur.

Usage :  python3 lancer.py        puis Ctrl+C pour arrêter
"""
import http.server
import socketserver
import webbrowser
import os
import sys

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8000
os.chdir(os.path.dirname(os.path.abspath(__file__)))


class Handler(http.server.SimpleHTTPRequestHandler):
    extensions_map = {
        **http.server.SimpleHTTPRequestHandler.extensions_map,
        ".webmanifest": "application/manifest+json",
        ".js": "text/javascript",
    }

    def end_headers(self):
        # pas de cache côté navigateur : le service worker gère le hors ligne
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()

    def log_message(self, *args):
        pass


with socketserver.TCPServer(("", PORT), Handler) as httpd:
    url = f"http://localhost:{PORT}/index.html"
    print(f"SHOT IQ tourne sur {url}")
    print("Installe-le depuis l'icône d'installation de la barre d'adresse.")
    print("Ctrl+C pour arrêter.")
    try:
        webbrowser.open(url)
    except Exception:
        pass
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nArrêté.")

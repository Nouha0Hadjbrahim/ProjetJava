# App Java Edayetna

##  Overview

**Edayetna** est une application Java développée dans le cadre du module *Projet Intégré : Développement Web Java* à **Esprit School of Engineering**.  
Elle a pour objectif de gérer une plateforme de mise en relation entre **artisans tunisiens** et **clients**.  
Les artisans peuvent y vendre des produits, des matériaux de création et proposer des ateliers en ligne.  

Le projet utilise **JavaFX** pour l’interface graphique, **JDBC** pour la base de données et s’appuie sur une architecture modulaire MVC.

---

##  Features

- Authentification sécurisée (email/mot de passe, Google OAuth, FaceID pour l’admin)
- Interface back-office avec :
  - Tableau de bord
  - Gestion des utilisateurs
  - Gestion des produits et matériaux
  - Réservations aux ateliers
  - Gestion des commandes et des réclamations
- Affichage dynamique avec pagination
- Génération de PDF (factures, reclamations)
- Intégration d’un calendrier Google (commandes par date)
- Système de notifications (via sockets email , whatsapp)
- Contrôles de saisie robustes avec messages d'erreurs stylisés
- Passage à la commande avec paiement intégré (Stripe)
- Réservation directe d’une session pour les ateliers en ligne
 Interface Front Office – Client
-L'interface client d’Edayetna a été pensée pour offrir une expérience fluide, 
 chaleureuse et intuitive. Elle met en valeur l’artisanat tunisien avec un design esthétique,
 une navigation claire, et des interactions modernes:
    *Expérience utilisateur
      -Accès rapide aux produits et ateliers
      -Navigation fluide avec animations
      -Thème visuel cohérent basé sur le marron artisanal et des tons chauds
      -Icônes et éléments stylisés pour améliorer l’UX
      -Réclamations facilement accessibles
      -Intégration du panier et de l'historique d’achats
---

## Tech Stack

### Frontend
- **JavaFX**
- **CSS personnalisé** pour un design moderne et responsive

### Backend
- **Java 17**
- **JDBC + MySQL**
- **Maven**

### Autres outils
- **Google OAuth 2.0**
- **Google Calendar API**
- **Twilio API** pour les notifications SMS
- **QR Code** pour les materiaux 
- **Email** pour les envoies des emails
- **Whatsapp API** pour notifier l'admin sur whatsapp
- **Stripe** permet le payment en ligne
- **Map** affichr la localisation
- **Météo** afficher la météo dans le dashboard 
- **Python (face recognition)** pour la connexion FaceID
- **GitHub** pour la gestion du code source

---

## Arborescence principale
ProjetJava/
│
├── .idea/
├── src/
│ └── main/
│   ├── java/
│   | ├── controller/
│   │   ├── model/
│   │   └── utils/
│   └── resources/
│     ├── assets/
│     ├── views/
│     ├── styles/
│     └── utils/
├── pom.xml
└── README.md


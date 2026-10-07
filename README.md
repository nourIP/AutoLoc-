AutoLoc : Gestion de location de véhicules multi-agences

Projet réalisé dans le cadre du module UP ASI : Architecture des Systèmes d'Information (ESPRIT).

Présentation

AutoLoc est une API REST qui permet de gérer la location de véhicules pour un réseau de plusieurs agences. Elle centralise le parc de véhicules, les clients, les réservations et les contrats de location.

Objectifs
Centraliser la gestion du parc automobile de toutes les agences
Suivre le statut de chaque véhicule (disponible, loué, etc.)
Faciliter la réservation et la location pour les clients
Donner aux responsables une vue sur l'activité de leur agence
Acteurs et cas d'utilisation

Client

Consulter les véhicules disponibles
Réserver ou annuler une réservation
Consulter ses locations

Agent d'agence

Enregistrer un client
Créer un contrat de location
Enregistrer le retour d'un véhicule

Responsable d'agence

Ajouter, modifier ou retirer un véhicule du parc
Suivre l'activité de son agence

Administrateur

Gérer les agences
Gérer les utilisateurs et leurs rôles
Technologies
Catégorie	Outils
Langage / Build	Java 17, Maven
Framework	Spring Boot, Spring Data JPA, Spring MVC
Base de données	MySQL 8.x
Outils	Lombok, IntelliJ IDEA, Postman, Git / GitHub
Structure du projet
src/main/java/tn/esprit/autoloc
├── domain        # Entités (Vehicule, CategorieVehicule, StatutVehicule)
├── repository    # Accès aux données
├── service       # Logique métier
└── web
    ├── controller  # Endpoints REST
    └── dto         # Objets de transfert
Installation et lancement

Prérequis : JDK 17, MySQL 8.x, Maven, Git

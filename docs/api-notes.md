# AutoLoc — Notes sur Spring MVC, API REST et Documentation Swagger (Atelier 5)

## Section A. Endpoints REST exposés

L'ensemble des routes de l'API sont préfixées par /api via la propriété globale server.servlet.context-path=/api.

| Contrôleur | Méthode HTTP & Route relative | Service appelé | Code Succès | Codes d'Erreur | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ContratController | GET /contrats | IContratService.findAll() | 200 OK | — | Liste de tous les contrats |
| ContratController | GET /contrats/{id} | IContratService.findById(id) | 200 OK | 404 Not Found | Consultation d'un contrat par ID |
| ContratController | POST /contrats | IContratService.create(contrat)| 201 Created | 400 Bad Request | Création d'un contrat |
| ContratController | PUT /contrats/{id} | IContratService.update(id, contrat)| 200 OK | 400, 404 | Mise à jour des informations |
| ContratController | DELETE /contrats/{id} | IContratService.deleteById(id)| 204 No Content | 404 Not Found | Suppression (cascade sur paiements) |
| PaiementController | GET /paiements | IPaiementService.findAll() | 200 OK | — | Consultation de tous les paiements |
| PaiementController | GET /paiements/{id} | IPaiementService.findById(id)| 200 OK | 404 Not Found | Consultation d'un paiement |
| AgenceController | GET /agences | IAgenceService.findAll() | 200 OK | — | Liste des agences |
| AgenceController | GET /agences/{id} | IAgenceService.findById(id) | 200 OK | 404 Not Found | Détail d'une agence |
| AgenceController | POST /agences | IAgenceService.create(agence) | 201 Created | 400 Bad Request | Création (nom obligatoire) |
| AgenceController | PUT /agences/{id} | IAgenceService.update(id, agence)| 200 OK | 400, 404 | Modification d'une agence |
| AgenceController | DELETE /agences/{id} | IAgenceService.deleteById(id)| 204 No Content | 404 Not Found | Suppression d'une agence |
| ClientController | GET /clients | IClientService.findAll() | 200 OK | — | Liste des clients |
| ClientController | GET /clients/{id} | IClientService.findById(id) | 200 OK | 404 Not Found | Détail d'un client |
| ClientController | POST /clients | IClientService.create(client) | 201 Created | 400 Bad Request | Création (nom/email requis) |
| ClientController | PUT /clients/{id} | IClientService.update(id, client)| 200 OK | 400, 404 | Mise à jour d'un client |
| ClientController | DELETE /clients/{id} | IClientService.deleteById(id)| 204 No Content | 404 Not Found | Suppression d'un client |
| EmployeController | GET /employes | IEmployeService.findAll() | 200 OK | — | Liste des employés |
| EmployeController | GET /employes/{id} | IEmployeService.findById(id) | 200 OK | 404 Not Found | Détail d'un employé |
| EmployeController | POST /employes | IEmployeService.create(employe)| 201 Created | 400 Bad Request | Création d'un employé |
| EmployeController | PUT /employes/{id} | IEmployeService.update(id, employe)| 200 OK | 400, 404 | Modification d'un employé |
| EmployeController | DELETE /employes/{id} | IEmployeService.deleteById(id)| 204 No Content | 404 Not Found | Suppression d'un employé |
| EquipementController| GET /equipements | IEquipementService.findAll() | 200 OK | — | Liste des équipements |
| EquipementController| GET /equipements/{id} | IEquipementService.findById(id)| 200 OK | 404 Not Found | Détail d'un équipement |
| EquipementController| POST /equipements | IEquipementService.create(eq)| 201 Created | 400 Bad Request | Création (libellé requis) |
| EquipementController| PUT /equipements/{id} | IEquipementService.update(id, eq)| 200 OK | 400, 404 | Modification d'un équipement |
| EquipementController| DELETE /equipements/{id}| IEquipementService.deleteById(id)| 204 No Content| 404 Not Found | Suppression d'un équipement |
| MaintenanceController| GET /maintenances | IMaintenanceService.findAll()| 200 OK | — | Liste des interventions |
| MaintenanceController| GET /maintenances/{id}| IMaintenanceService.findById(id)| 200 OK | 404 Not Found | Détail d'une intervention |
| MaintenanceController| POST /maintenances | IMaintenanceService.create(m)| 201 Created | 400 Bad Request | Création (date cohérente) |
| MaintenanceController| PUT /maintenances/{id} | IMaintenanceService.update(id, m)| 200 OK | 400, 404 | Modification d'une maintenance |
| MaintenanceController| DELETE /maintenances/{id}| IMaintenanceService.deleteById(id)| 204 No Content| 404 Not Found | Suppression d'une maintenance |
| ReservationController| GET /reservations | IReservationService.findAll()| 200 OK | — | Liste des réservations |
| ReservationController| GET /reservations/{id}| IReservationService.findById(id)| 200 OK | 404 Not Found | Détail d'une réservation |
| ReservationController| POST /reservations | IReservationService.create(res)| 201 Created | 400 Bad Request | Création d'une réservation |
| ReservationController| PUT /reservations/{id}| IReservationService.update(id, res)| 200 OK | 400, 404 | Modification d'une réservation |
| ReservationController| DELETE /reservations/{id}| IReservationService.deleteById(id)| 204 No Content| 404 Not Found | Suppression d'une réservation |
| VehiculeController | GET /vehicules | IVehiculeService.findAll() | 200 OK | — | Liste de la flotte |
| VehiculeController | GET /vehicules/{id} | IVehiculeService.findById(id)| 200 OK | 404 Not Found | Détail d'un véhicule |
| VehiculeController | POST /vehicules | IVehiculeService.create(veh) | 201 Created | 400 Bad Request | Création (tarif journalier positif)|
| VehiculeController | PUT /vehicules/{id} | IVehiculeService.update(id, veh)| 200 OK | 400, 404 | Modification d'un véhicule |
| VehiculeController | DELETE /vehicules/{id}| IVehiculeService.deleteById(id)| 204 No Content | 404 Not Found | Suppression d'un véhicule |

---

## Section B. Annotations Jackson et sérialisation des associations

| Relation | Annotation retenue | Côté | Justification |
| :--- | :--- | :--- | :--- |
| Contrat ↔ Paiement | @JsonManagedReference("contrat-paiements") | Contrat.paiements (Parent) | Permet d'inclure la liste des paiements dans le JSON du contrat lors de la lecture et de la création imbriquée. |
| Contrat ↔ Paiement | @JsonBackReference("contrat-paiements") | Paiement.contrat (Enfant) | Évite la récursion infinie (StackOverflowError) en rompant le cycle de sérialisation retour vers le contrat. |
| Agence ↔ Vehicule | @JsonIgnore | Agence.vehicules | L'agence ne sérialise pas sa flotte complète par défaut afin d'alléger la réponse JSON. |
| Client ↔ Reservation | @JsonIgnore | Client.reservations | Les réservations d'un client sont omises lors de la consultation du profil client pour éviter les boucles. |
| Vehicule ↔ Reservation| @JsonIgnore | Vehicule.reservations | Évite le cycle lors de l'affichage du véhicule. |

### Différence entre @JsonBackReference et @JsonIgnore :
* @JsonBackReference est conçu pour fonctionner en binôme avec @JsonManagedReference dans les compositions parent/enfant. Il ignore la référence arrière lors de la sérialisation, mais réassigne automatiquement le parent à l'enfant lors de la désérialisation.
* @JsonIgnore ignore simplement le champ dans les deux sens (sérialisation et désérialisation), sans restaurer automatiquement le lien bidirectionnel.

---

## Section C. Complémentarité Postman et Swagger

| Outil | Rôle principal | Apport concret dans l'Atelier 5 |
| :--- | :--- | :--- |
| **Postman** | Outil de test fonctionnel et d'automatisation. | Permet d'enchaîner des scénarios de test complets (ex: créer un contrat avec paiements, récupérer l'ID généré via des scripts, vérifier les codes HTTP 201, 204, 400, 404 et tester la cascade de suppression). |
| **Swagger UI (SpringDoc)**| Documentation interactive générée depuis le code. | Offre un contrat d'interface vivant et navigable (/api/swagger-ui.html), visualisant les schémas de données, les routes et permettant aux équipes frontend d'expérimenter directement les endpoints. |

---

## Section D. Analyse de qualité SonarQube for IDE

| Anomalie détectée | Règle / Explication | Correction apportée |
| :--- | :--- | :--- |
| **Exposition directe des entités JPA dans les contrôleurs** | Risque de couplage fort entre le schéma de base et l'API publique, et exposition de colonnes internes. | Documenté comme état transitoire de l'Atelier 5. L'introduction des DTO au Chapitre 10 viendra découpler l'API du modèle persistant. |
| **Méthodes sans documentation Swagger** | Règle de complétude de documentation API. | Ajout exhaustif des annotations @Tag, @Operation, @ApiResponses, @ApiResponse et @Parameter sur l'ensemble des 9 contrôleurs. |
| **Gestion non unifiée des exceptions HTTP** | Réponses d'erreur disparates et non structurées. | Centralisation via @RestControllerAdvice dans GlobalExceptionHandler retournant une structure d'erreur claire (statut, message, horodatage). |

---

## Section E. Questions de compréhension

1. **Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur à appeler ?**  
   C'est le DispatcherServlet (le Front Controller de Spring MVC) qui intercepte chaque requête HTTP. Il interroge ensuite le composant HandlerMapping, qui maintient une table de correspondance entre les URL/méthodes HTTP et les annotations @RequestMapping, @GetMapping, etc. déclarées sur les contrôleurs.

2. **Pourquoi ContratController dépend-il de IContratService et non de ContratRepository ni de ContratServiceImpl ?**  
   * Il ne dépend pas du repository pour respecter l'architecture en couches : un contrôleur ne doit pas accéder directement aux données, mais déléguer la logique et les validations à la couche métier.
   * Il dépend de l'interface IContratService (et non de la classe concrète ContratServiceImpl) pour respecter le principe d'inversion des dépendances (IoC / SOLID), favorisant l'indépendance et la testabilité.

3. **Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?**  
   * POST renvoie **201 Created** pour signifier qu'une nouvelle ressource a été matérialisée en base avec succès.
   * DELETE renvoie **204 No Content** car l'action de suppression a réussi et il n'y a plus aucun contenu à retourner au client.
   * Une ressource absente renvoie **404 Not Found** car il s'agit d'une erreur côté client (URL ou identifiant inexistant). Une erreur **500 Internal Server Error** signale un bogue interne ou une défaillance serveur non anticipée.

4. **Pourquoi PaiementController n'expose-t-il que des GET ? Quelle réponse reçoit un POST /api/paiements ?**  
   Parce que la relation Contrat ↔ Paiement est une relation de composition stricte. Les paiements n'ont pas d'existence autonome en écriture : leur création et suppression sont pilotées par le cycle de vie du contrat. Un POST /api/paiements se heurte à l'absence de mapping et reçoit une réponse HTTP **405 Method Not Allowed** (ou 404).

5. **Quels risques présente l'exposition directe des entités JPA ? Pourquoi Contrat.paiements utilise-t-il @JsonManagedReference alors que Agence.vehicules utilise @JsonIgnore ?**  
   * L'exposition directe lie l'API au schéma relationnel interne, expose des données confidentielles potentielles et peut déclencher des chargements paresseux involontaires (LazyInitializationException) ou des boucles infinies de sérialisation.
   * Contrat.paiements est une composition où l'on souhaite voir et créer les paiements imbriqués dans le contrat (@JsonManagedReference + @JsonBackReference). À l'inverse, une agence pouvant gérer des centaines de véhicules, inclure systématiquement toute la flotte dans le JSON de l'agence surchargerait inutilement le réseau ; on utilise donc @JsonIgnore sur Agence.vehicules.

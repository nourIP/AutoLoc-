# AutoLoc — Notes sur la couche Service, IoC et Injection de Dépendances (Atelier 4)

## Section A. Modes d'injection comparés

| Critère | Injection par Constructeur (Recommandé) | Injection par Attribut (@Autowired) | Injection par Setter |
| :--- | :--- | :--- | :--- |
| **Champ inal possible ?** | **Oui**, garantit l'immutabilité du bean. | **Non**, viole l'immutabilité. | **Non**, modifiable à tout moment. |
| **Dépendance visible dans la signature ?** | **Oui**, le contrat d'instanciation est explicite. | **Non**, dépendances cachées. | **Oui**, via les méthodes set...(). |
| **Utilisation avec 
ew hors conteneur (ex. TU) ?** | **Oui**, instanciable facilement en passant des mocks. | **Non**, risque de NullPointerException sans conteneur. | **Partiellement**, dépendances optionnelles. |
| **Signalé par SonarQube / SonarLint ?** | **Non**, respecte les standards Clean Code. | **Oui** (Règle S6813 : *Use constructor injection instead of field injection*). | Possible selon la configurabilité. |

---

## Section B. Couche Service : Dépendances et Justification

| Service | Dépendances injectées | Mode d'injection | Justification architecturale |
| :--- | :--- | :--- | :--- |
| ContratServiceImpl | IContratRepository | Constructeur (@RequiredArgsConstructor) | Centralise la logique métier des contrats, refuse un montant total négatif et coordonne la cascade vers les paiements. |
| PaiementServiceImpl | IPaiementRepository | Constructeur (@RequiredArgsConstructor) | Service en lecture seule (indById, indAll). Aucune création/modification directe : le paiement est géré via la composition avec Contrat. |
| AgenceServiceImpl | IAgenceRepository | Constructeur (@RequiredArgsConstructor) | Valide les champs obligatoires (nom d'agence non vide) et assure le CRUD complet de l'agence. |
| ClientServiceImpl | IClientRepository | Constructeur (@RequiredArgsConstructor) | Contrôle l'unicité et la présence des attributs d'identification (nom, email obligatoire) et initialise la date d'inscription. |
| EmployeServiceImpl | IEmployeRepository | Constructeur (@RequiredArgsConstructor) | Vérifie les données des employés (nom, prénom) et affecte l'agence de rattachement. |
| EquipementServiceImpl | IEquipementRepository | Constructeur (@RequiredArgsConstructor) | Empêche la création d'équipements sans libellé obligatoire. |
| MaintenanceServiceImpl| IMaintenanceRepository| Constructeur (@RequiredArgsConstructor) | Garantit la cohérence chronologique (date de fin non antérieure à date de début) des entretiens. |
| ReservationServiceImpl| IReservationRepository| Constructeur (@RequiredArgsConstructor) | Valide la période de réservation (dateFin >= dateDebut) et gère les statuts. |
| VehiculeServiceImpl | IVehiculeRepository | Constructeur (@RequiredArgsConstructor) | Interdit un tarif journalier négatif et permet le suivi de la flotte. |

---

## Section C. Diagnostic des messages d'erreur du conteneur Spring

### Message 1
* **Description** : Parameter 0 of constructor in ContratController required a bean of type 'IContratService' that could not be found.
* **Causes probables** :
  1. L'implémentation ContratServiceImpl a oublié l'annotation de stéréotype @Service (ou @Component).
  2. La classe ContratServiceImpl est située dans un package non scanné par @SpringBootApplication (en dehors de l'arborescence racine 	n.esprit.autoloc).
* **Correction** : Ajouter @Service sur la classe ContratServiceImpl et s'assurer qu'elle réside dans 	n.esprit.autoloc.service.

### Message 2
* **Description** : Parameter 0 of constructor in NotificationController required a single bean, but 2 were found: emailNotificateur, smsNotificateur.
* **Cause** : Deux beans implémentent la même interface cible sans indication de priorité pour Spring.
* **Corrections possibles** :
  1. Poser @Primary sur la classe du bean préféré (ex. @Primary @Service public class EmailNotificateur ...).
  2. Utiliser @Qualifier("emailNotificateur") à l'injection au niveau du paramètre du constructeur du contrôleur pour choisir explicitement le bean.

### Message 3
* **Description** : The dependencies of some of the beans in the application context form a cycle: clientServiceImpl -> reservationServiceImpl -> clientServiceImpl.
* **Raison de l'impossibilité avec l'injection par constructeur** : Pour instancier ClientServiceImpl, Spring doit d'abord instancier ReservationServiceImpl. Mais pour instancier ce dernier, Spring a besoin de ClientServiceImpl. Aucun des deux objets ne peut donc être créé, provoquant une BeanCurrentlyInCreationException.
* **Piste de refactoring** : Extraire la logique ou responsabilité commune dans un troisième service ou utiliser des événements applicatifs (ApplicationEventPublisher).

---

## Section D. Analyse de qualité SonarQube for IDE (SonarLint)

| Anomalie détectée | Règle SonarQube / Explication | Correction apportée |
| :--- | :--- | :--- |
| **Injection par champ (@Autowired private IContratRepository ...)** | java:S6813 : L'injection par champ masque les dépendances, empêche les tests unitaires purs et ne permet pas d'avoir des champs inal. | Utilisation de champs private final avec @RequiredArgsConstructor de Lombok pour injection par constructeur. |
| **Lancer une exception générique (	hrow new RuntimeException(...))** | java:S112 : Les exceptions génériques (RuntimeException, Exception) nuisent à la précision du diagnostic et à la gestion centralisée des erreurs HTTP. | Création et utilisation d'une exception métier dédiée : ResourceNotFoundException. |
| **Absence de garde sur l'ID lors du create()** | Règle de bonne pratique de persistance : un appel à save() avec un identifiant non nul déclenche un merge() (UPDATE), risquant d'écraser des données existantes. | Ajout de la garde : if (entite.getId() != null) throw new IllegalArgumentException("...");. |

---

## Section E. Questions de compréhension

1. **Où est le 
ew de ContratServiceImpl et qui l'exécute ?**  
   Il n'y a aucun 
ew ContratServiceImpl() écrit dans le code applicatif. C'est le conteneur Spring IoC (ApplicationContext) qui instancie dynamiquement le bean par réflexion lors de la phase de démarrage, après avoir scanné l'annotation @Service.

2. **Pourquoi un contrôleur dépendra-t-il de IContratService et non de ContratServiceImpl ?**  
   Pour respecter le principe d'Inversion des Dépendances (le D de SOLID) : les modules de haut niveau doivent dépendre d'abstractions et non de classes concrètes. Cela permet un couplage faible et facilite le remplacement ou le bouchonnage (mock) du service lors des tests.

3. **Pourquoi un service singleton doit-il rester sans état ? Donner un exemple de bug.**  
   Un service de scope singleton est partagé par tous les threads traitant simultanément les requêtes HTTP. S'il possède un attribut modifiable (ex: private Contrat contratCourant), deux requêtes concurrentes écraseront mutuellement cette variable, provoquant des corruptions de données majeures (Race Condition).

4. **Pourquoi update() charge-t-il l'entité existante avant d'appeler save() au lieu de lui passer directement l'objet reçu ?**  
   Passer directement l'objet reçu écraserait toutes les colonnes non renseignées dans la requête HTTP avec des valeurs 
ull. De plus, dans le cas d'associations avec orphanRemoval = true (comme Contrat.paiements), cela supprimerait involontairement tous les paiements rattachés au contrat.

5. **Quelle différence entre @Component et @Bean ? Entre @Primary et @Qualifier ? Pourquoi IPaiementService n'expose-t-il ni création, ni modification, ni suppression ?**  
   * @Component s'applique sur une classe pour en faire un bean détecté automatiquement par scan, tandis que @Bean s'applique sur une méthode au sein d'une classe @Configuration pour enregistrer un objet configuré manuellement.
   * @Primary désigne le candidat par défaut lorsque plusieurs beans partagent la même interface ; @Qualifier désigne explicitement le bean souhaité par son nom à l'endroit précis de l'injection.
   * IPaiementService n'expose aucune opération de mutation car Paiement est lié au Contrat par une relation de composition stricte : le cycle de vie d'un paiement est entièrement gouverné par son contrat parent.

# AutoLoc — Notes sur la couche Repository et Qualité de code (Atelier 3)

## 1. Choix et justification des interfaces Repository

Conformément aux directives de l'Atelier 3, l'ensemble des 9 interfaces Repository du package 	n.esprit.autoloc.repository héritent de JpaRepository<T, ID> afin d'offrir l'API la plus complète et adaptée à une application de gestion relationnelle en entreprise.

| Interface | Entité | Hérite de | Justification architecturale |
| :--- | :--- | :--- | :--- |
| IAgenceRepository | Agence (ID: Long) | JpaRepository<Agence, Long> | CRUD complet, pagination pour l'affichage de la flotte par agence, et méthodes indAll retournant des List. |
| IClientRepository | Client (ID: Long) | JpaRepository<Client, Long> | Gestion complète du cycle de vie des clients, recherche par critères, pagination des listes de clients. |
| IContratRepository | Contrat (ID: Long) | JpaRepository<Contrat, Long> | Support de saveAndFlush() pour synchronisation immédiate avant facturation, tri par montant total (Sort), pagination (Pageable), gestion de la composition avec Paiement. |
| IEmployeRepository | Employe (ID: Long) | JpaRepository<Employe, Long> | CRUD complet pour les agents et managers rattachés aux agences. |
| IEquipementRepository | Equipement (ID: Long) | JpaRepository<Equipement, Long> | Consultation et gestion des options/équipements des véhicules (GPS, siège bébé, coffre de toit). |
| IMaintenanceRepository | Maintenance (ID: Long) | JpaRepository<Maintenance, Long> | Suivi des opérations d'entretien sur les véhicules et historique d'intervention. |
| IPaiementRepository | Paiement (ID: Long) | JpaRepository<Paiement, Long> | Permet la lecture directe et le décompte des paiements (même si la création/suppression passe préférentiellement par Contrat). |
| IReservationRepository | Reservation (ID: Long) | JpaRepository<Reservation, Long> | Recherche, suivi des réservations, pagination et tri par dates. |
| IVehiculeRepository | Vehicule (ID: Long) | JpaRepository<Vehicule, Long> | Recherche multi-critères, tri, pagination sur la flotte et consultation du statut/catégorie. |

---

## 2. Analyse de la hiérarchie Spring Data JPA

1. **Repository<T, ID>** : Interface marqueur vide. Utilisée uniquement pour exposer un sous-ensemble minimal de méthodes choisies à la main (ex: lecture seule).
2. **CrudRepository<T, ID>** : Fournit les 11 méthodes de persistance fondamentale (save, indById, deleteById...). Les méthodes de collection retournent un Iterable<T>.
3. **ListCrudRepository<T, ID>** (Spring Data 3) : Variante optimisée de CrudRepository où indAll(), indAllById(), saveAll() renvoient une List<T>.
4. **PagingAndSortingRepository<T, ID>** : Fournit le tri (Sort) et la pagination (Pageable / Page<T>). Depuis Spring Data 3, n'hérite plus de CrudRepository.
5. **JpaRepository<T, ID>** : L'interface reine pour JPA. Réunit ListCrudRepository, ListPagingAndSortingRepository, QueryByExampleExecutor et ajoute les opérations spécifiques au contexte de persistance : lush(), saveAndFlush(), getReferenceById() (proxy lazy).

> **Avertissement architectural sur les suppressions en lot :**  
> Les méthodes deleteAllInBatch() et deleteAllByIdInBatch() génèrent des requêtes directes SQL DELETE FROM table WHERE ... en contournant le contexte de persistance Hibernate. Par conséquent, les cascades (CascadeType.REMOVE) et orphanRemoval = true ne sont **pas** exécutées, ce qui risque de provoquer des violations de clés étrangères en base ou des données orphelines.

---

## 3. Rapport d'analyse statique et qualité (SonarQube for IDE / Clean Code)

| Anomalie détectée | Règle / Explication | Correction apportée |
| :--- | :--- | :--- |
| **Utilisation de @Data sur les entités JPA** | Risque majeur de boucle infinie (StackOverflowError) dans les méthodes equals, hashCode et 	oString lors d'associations bidirectionnelles (Agence ↔ Vehicule, Contrat ↔ Paiement...). | Remplacement systématique par les annotations Lombok ciblées : @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor. |
| **Absence de configuration explicite FetchType.LAZY sur @ManyToOne** | Par défaut en JPA, les relations @ManyToOne et @OneToOne ont une stratégie FetchType.EAGER, ce qui entraîne le problème de requêtage N+1 et le chargement inutile de données jointes en mémoire. | Définition explicite de etch = FetchType.LAZY sur tous les @ManyToOne (gence, client, ehicule, contrat) et @OneToOne. |
| **Cas de cascade manquants sur les relations de l'étude de cas** | Selon le cahier des charges AutoLoc, l'enregistrement d'un client doit pouvoir persister ses réservations (cascade = PERSIST), et la maintenance doit cascader la création du véhicule (cascade = PERSIST). | Ajout de cascade = CascadeType.PERSIST sur Client.reservations et Maintenance.vehicule. |
| **Convention de nommage des interfaces repository** | Respect de la convention d'architecture d'entreprise imposée : chaque repository doit être une interface préfixée par I située dans le package 	n.esprit.autoloc.repository. | Vérification et standardisation des 9 interfaces (IVehiculeRepository, IContratRepository, etc.). |
| **Absence de @Repository inutile** | Dans Spring Data JPA, les interfaces héritant de Repository bénéficient automatiquement de la détection de composants et de la génération dynamique de proxies. L'annotation @Repository sur l'interface est superflue. | Interfaces déclarées directement sans annotation superflue, garantissant un code propre (Clean Code). |

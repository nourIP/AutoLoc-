package tn.esprit.autoloc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.repository.IContratRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DemoInterfacesRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoInterfacesRunner.class);

    private final IContratRepository contratRepository;
    private final TransactionTemplate transactionTemplate;

    public DemoInterfacesRunner(IContratRepository contratRepository,
                                TransactionTemplate transactionTemplate) {
        this.contratRepository = contratRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) {
        // Préparation : 4 contrats avec des montants différents
        titre("0. saveAll : 4 contrats (JpaRepository renvoie une List)");
        List<Contrat> crees = contratRepository.saveAll(List.of(
                creerContrat("100.00"), creerContrat("400.00"),
                creerContrat("250.00"), creerContrat("300.00")));
        List<Long> ids = crees.stream().map(Contrat::getIdContrat).toList();
        log.info("Ids créés : {}", ids);

        // 1. Tri : Sort reçoit le nom de l'ATTRIBUT Java, pas celui de la colonne
        titre("1. findAll(Sort) : tri décroissant sur montantTotal");
        Sort tri = Sort.by("montantTotal").descending();
        for (Contrat c : contratRepository.findAll(tri)) {
            log.info("  montantTotal = {}", c.getMontantTotal());
        }

        // 2. Pagination : une page de 2 éléments, triée
        titre("2. findAll(Pageable) : pages de 2 éléments");
        Page<Contrat> page0 = contratRepository.findAll(PageRequest.of(0, 2, tri));
        log.info("Page {} : {} éléments, total = {}, pages = {}",
                page0.getNumber(), page0.getContent().size(),
                page0.getTotalElements(), page0.getTotalPages());
        Page<Contrat> page1 = contratRepository.findAll(PageRequest.of(1, 2, tri));
        log.info("Page {} : {} éléments", page1.getNumber(), page1.getContent().size());

        // 3. saveAndFlush : l'écriture part tout de suite, sans attendre la fin de la transaction
        titre("3. saveAndFlush : UPDATE immédiat");
        Long premierId = ids.get(0);
        transactionTemplate.executeWithoutResult(status -> {
            Contrat c = contratRepository.findById(premierId).orElseThrow();
            c.setMontantTotal(new BigDecimal("500.00"));
            log.info("Avant saveAndFlush");
            contratRepository.saveAndFlush(c);
            log.info("Après saveAndFlush : l'UPDATE est déjà parti");
        });

        // 4. getReferenceById : proxy paresseux, aucune requête avant l'accès aux données
        titre("4. getReferenceById : proxy paresseux");
        transactionTemplate.executeWithoutResult(status -> {
            Contrat reference = contratRepository.getReferenceById(premierId);
            log.info("Proxy obtenu : aucune requête SQL pour l'instant");
            log.info("Accès aux données : montantTotal = {}", reference.getMontantTotal());
        });

        // 5. Suppression en lot : contourne la cascade, MySQL refuse à cause de la clé étrangère
        titre("5. deleteAllByIdInBatch : limite des suppressions en lot");
        Contrat avecPaiement = creerContrat("600.00");
        avecPaiement.getPaiements().add(creerPaiement(avecPaiement));
        Long idAvecPaiement = contratRepository.save(avecPaiement).getIdContrat();
        try {
            contratRepository.deleteAllByIdInBatch(List.of(idAvecPaiement));
            log.info("Suppression en lot réussie");
        } catch (DataIntegrityViolationException e) {
            log.info("Refusé : la cascade ne s'applique pas, le paiement bloque la suppression");
        }

        // Nettoyage : deleteById passe par le contexte de persistance, donc la cascade s'applique
        titre("6. Nettoyage avec deleteById et deleteAllById");
        contratRepository.deleteById(idAvecPaiement);
        contratRepository.deleteAllById(ids);
        log.info("Nettoyage terminé");
    }

    private Contrat creerContrat(String montant) {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now());
        contrat.setMontantTotal(new BigDecimal(montant));
        contrat.setValide(true);
        return contrat;
    }

    private Paiement creerPaiement(Contrat contrat) {
        Paiement paiement = new Paiement();
        paiement.setMontant(new BigDecimal("100.00"));
        paiement.setDatePaiement(LocalDate.now());
        paiement.setModePaiement(ModePaiement.CARTE);
        paiement.setContrat(contrat);
        return paiement;
    }

    private void titre(String texte) {
        log.info("=========== {} ===========", texte);
    }
}
package tn.esprit.autoloc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IPaiementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class DemoCrudRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoCrudRunner.class);

    private final IContratRepository contratRepository;
    private final IPaiementRepository paiementRepository;
    private final TransactionTemplate transactionTemplate;

    public DemoCrudRunner(IContratRepository contratRepository,
                          IPaiementRepository paiementRepository,
                          TransactionTemplate transactionTemplate) {
        this.contratRepository = contratRepository;
        this.paiementRepository = paiementRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(String... args) {
        long contratsAvant = contratRepository.count();

        // 1. CREATE : id null -> persist -> INSERT
        titre("1. CREATE : save() d'un contrat neuf avec 2 paiements (cascade)");
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now());
        contrat.setMontantTotal(new BigDecimal("300.00"));
        contrat.setValide(true);
        contrat.getPaiements().add(creerPaiement(contrat, "100.00", ModePaiement.CARTE));
        contrat.getPaiements().add(creerPaiement(contrat, "200.00", ModePaiement.ESPECES));

        Contrat sauvegarde = contratRepository.save(contrat);
        Long idContrat = sauvegarde.getIdContrat();
        Long idPaiement = sauvegarde.getPaiements().get(0).getIdPaiement();
        log.info("id du contrat attribué par MySQL : {}", idContrat);

        // 2. READ : findById renvoie un Optional
        titre("2. READ : findById, existsById, findAll, count");
        Optional<Contrat> trouve = contratRepository.findById(idContrat);
        log.info("Contrat {} présent ? {}", idContrat, trouve.isPresent());
        Optional<Contrat> absent = contratRepository.findById(-1L);
        log.info("Contrat -1 présent ? {}", absent.isPresent());
        log.info("existsById({}) = {}", idContrat, contratRepository.existsById(idContrat));
        log.info("Nombre de contrats (findAll, une List) : {}", contratRepository.findAll().size());
        log.info("Nombre de paiements (count) : {}", paiementRepository.count());

        // 3. UPDATE : id renseigné -> merge -> SELECT puis UPDATE
        titre("3. UPDATE : save() sur des entités existantes");
        Contrat aModifier = contratRepository.findById(idContrat).orElseThrow();
        aModifier.setMontantTotal(new BigDecimal("350.00"));
        contratRepository.save(aModifier);

        Paiement paiement = paiementRepository.findById(idPaiement).orElseThrow();
        paiement.setMontant(new BigDecimal("150.00"));
        paiementRepository.save(paiement);

        // 4. orphanRemoval : retirer un paiement via le contrat
        titre("4. orphanRemoval : retrait d'un paiement via le Contrat");
        transactionTemplate.executeWithoutResult(status -> {
            Contrat c = contratRepository.findById(idContrat).orElseThrow();
            c.getPaiements().remove(0);
        });
        log.info("Paiements restants : {}", paiementRepository.count());

        // 5. DELETE : cascade REMOVE sur les paiements
        titre("5. DELETE : deleteById du contrat (cascade sur les paiements)");
        contratRepository.deleteById(idContrat);
        log.info("Contrats : {} (avant la démo : {})", contratRepository.count(), contratsAvant);

        // 6. deleteById sur un id qui n'existe plus : aucune exception (Spring Data 3)
        titre("6. deleteById sur un id inexistant");
        contratRepository.deleteById(idContrat);
        log.info("Aucune exception levée");
    }

    private Paiement creerPaiement(Contrat contrat, String montant, ModePaiement mode) {
        Paiement paiement = new Paiement();
        paiement.setMontant(new BigDecimal(montant));
        paiement.setDatePaiement(LocalDate.now());
        paiement.setModePaiement(mode);
        paiement.setContrat(contrat);
        return paiement;
    }

    private void titre(String texte) {
        log.info("=========== {} ===========", texte);
    }
}
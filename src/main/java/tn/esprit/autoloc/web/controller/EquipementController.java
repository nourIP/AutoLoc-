package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.service.IEquipementService;

import java.util.List;

@RestController
@RequestMapping("/equipements")
@RequiredArgsConstructor
@Tag(name = "Equipements", description = "Gestion des équipements")
public class EquipementController {

    private final IEquipementService equipementService;

    @GetMapping
    @Operation(summary = "Lister tous les équipements")
    @ApiResponse(responseCode = "200", description = "Liste des équipements récupérée")
    public List<Equipement> findAll() {
        return equipementService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un équipement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipement trouvé"),
            @ApiResponse(responseCode = "404", description = "Equipement introuvable")
    })
    public Equipement findById(@Parameter(description = "Identifiant de l'équipement") @PathVariable Long id) {
        return equipementService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un équipement")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipement créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides (ex. libellé vide)")
    })
    public Equipement create(@RequestBody Equipement equipement) {
        return equipementService.create(equipement);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un équipement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipement mis à jour"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Equipement introuvable")
    })
    public Equipement update(@Parameter(description = "Identifiant de l'équipement") @PathVariable Long id,
                             @RequestBody Equipement equipement) {
        return equipementService.update(id, equipement);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un équipement")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipement supprimé"),
            @ApiResponse(responseCode = "404", description = "Equipement introuvable")
    })
    public void deleteById(@Parameter(description = "Identifiant de l'équipement") @PathVariable Long id) {
        equipementService.deleteById(id);
    }
}

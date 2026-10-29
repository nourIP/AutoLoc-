package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.service.IClientService;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestion des clients")
public class ClientController {

    private final IClientService clientService;

    @GetMapping
    @Operation(summary = "Lister tous les clients")
    @ApiResponse(responseCode = "200", description = "Liste des clients récupérée")
    public List<Client> findAll() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client trouvé"),
            @ApiResponse(responseCode = "404", description = "Client introuvable")
    })
    public Client findById(@Parameter(description = "Identifiant du client") @PathVariable Long id) {
        return clientService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides (ex. nom ou email vide)")
    })
    public Client create(@RequestBody Client client) {
        return clientService.create(client);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client mis à jour"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Client introuvable")
    })
    public Client update(@Parameter(description = "Identifiant du client") @PathVariable Long id,
                         @RequestBody Client client) {
        return clientService.update(id, client);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client supprimé"),
            @ApiResponse(responseCode = "404", description = "Client introuvable")
    })
    public void deleteById(@Parameter(description = "Identifiant du client") @PathVariable Long id) {
        clientService.deleteById(id);
    }
}

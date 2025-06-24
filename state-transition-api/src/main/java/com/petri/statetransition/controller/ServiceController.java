package com.petri.statetransition.controller;

import com.petri.statetransition.dto.*;
import com.petri.statetransition.model.enums.ServiceState;
import com.petri.statetransition.service.ServiceManager;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Contrôleur REST pour la gestion des services
 */
@RestController
@RequestMapping("/api/v1/services")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private final ServiceManager serviceService;

    public ServiceController(ServiceManager serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * Crée un nouveau service
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<ServiceDTO>>> createService(@Valid @RequestBody CreateServiceDTO createServiceDTO) {
        logger.info("Demande de création d'un service: {}", createServiceDTO.name());

        return serviceService.createService(createServiceDTO)
                .map(serviceDTO -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Service créé avec succès", serviceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la création du service")));
    }

    /**
     * Met à jour un service existant
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<ServiceDTO>>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceDTO updateServiceDTO) {
        logger.info("Demande de mise à jour du service ID: {}", id);

        return serviceService.updateService(id, updateServiceDTO)
                .map(serviceDTO -> ResponseEntity.ok(ApiResponse.success("Service mis à jour avec succès", serviceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la mise à jour du service")));
    }

    /**
     * Récupère un service par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<ServiceDTO>>> getServiceById(@PathVariable Long id) {
        logger.debug("Demande de récupération du service ID: {}", id);

        return serviceService.findById(id)
                .map(serviceDTO -> ResponseEntity.ok(ApiResponse.success(serviceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Service non trouvé")));
    }

    /**
     * Récupère tous les services
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<ServiceDTO>>>> getAllServices() {
        logger.debug("Demande de récupération de tous les services");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(serviceService.findAll())));
    }

    /**
     * Récupère les services par état
     */
    @GetMapping("/state/{state}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<ServiceDTO>>>> getServicesByState(@PathVariable ServiceState state) {
        logger.debug("Demande de récupération des services dans l'état: {}", state);

        return Mono.just(ResponseEntity.ok(ApiResponse.success(serviceService.findByState(state))));
    }

    /**
     * Recherche des services avec critères
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<ServiceDTO>>>> searchServices(@RequestBody SearchCriteriaDTO criteria) {
        logger.debug("Demande de recherche de services avec critères");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(serviceService.searchServices(criteria))));
    }

    /**
     * Démarre un service (transition PRÊT -> EN_COURS)
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<ServiceDTO>>> startService(@PathVariable Long id) {
        logger.info("Demande de démarrage du service ID: {}", id);

        return serviceService.startService(id)
                .map(serviceDTO -> ResponseEntity.ok(ApiResponse.success("Service démarré avec succès", serviceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors du démarrage du service")));
    }

    /**
     * Termine un service (transition EN_COURS -> TERMINÉ)
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<ServiceDTO>>> completeService(@PathVariable Long id) {
        logger.info("Demande de finalisation du service ID: {}", id);

        return serviceService.completeService(id)
                .map(serviceDTO -> ResponseEntity.ok(ApiResponse.success("Service terminé avec succès", serviceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la finalisation du service")));
    }

    /**
     * Annule un service
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<ServiceDTO>>> cancelService(@PathVariable Long id) {
        logger.info("Demande d'annulation du service ID: {}", id);

        return serviceService.cancelService(id)
                .map(serviceDTO -> ResponseEntity.ok(ApiResponse.success("Service annulé avec succès", serviceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de l'annulation du service")));
    }

    /**
     * Supprime un service
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteService(@PathVariable Long id) {
        logger.info("Demande de suppression du service ID: {}", id);

        return serviceService.deleteService(id)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Service supprimé avec succès", null))))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la suppression du service")));
    }

    /**
     * Récupère les services actifs (non terminés)
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<ServiceDTO>>>> getActiveServices() {
        logger.debug("Demande de récupération des services actifs");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(
                serviceService.findAll()
                        .filter(service -> !service.isFinalState())
        )));
    }

    /**
     * Récupère les services pouvant être démarrés
     */
    @GetMapping("/ready")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<ServiceDTO>>>> getReadyServices() {
        logger.debug("Demande de récupération des services prêts");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(serviceService.findByState(ServiceState.PRET))));
    }
}
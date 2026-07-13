package learnMongoDb.learnSpringMongoDb.controller;

import jakarta.validation.Valid;
import learnMongoDb.learnSpringMongoDb.dto.AddressDto.AddressRequest;
import learnMongoDb.learnSpringMongoDb.dto.AddressDto.AddressResponse;
import learnMongoDb.learnSpringMongoDb.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AddressController
 *
 * REST API for managing a user's saved delivery addresses.
 *
 * Base path : /api/v1/addresses
 * Security  : All endpoints require a valid JWT (enforced by SecurityConfig).
 *             The userId is always resolved from the authenticated principal
 *             so users can only access / modify their own addresses.
 *
 * Endpoints:
 *   GET    /api/v1/addresses              → list all addresses for the user
 *   GET    /api/v1/addresses/{id}         → get one address
 *   POST   /api/v1/addresses              → create a new address
 *   PUT    /api/v1/addresses/{id}         → update an address
 *   DELETE /api/v1/addresses/{id}         → delete an address
 *   PATCH  /api/v1/addresses/{id}/default → set address as default
 */
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Extracts the userId (= username / email stored in JWT subject)
     * from the Spring Security authentication principal.
     *
     * The UserDetails username corresponds to the MongoDB User document id
     * set in JwtAuthFilter, so it is safe to use as the userId FK.
     */
    private String userId(UserDetails principal) {
        return principal.getUsername();
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/addresses
     * Returns all saved addresses for the authenticated user, newest first.
     */
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(addressService.getAddresses(userId(principal)));
    }

    /**
     * GET /api/v1/addresses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddress(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(addressService.getAddress(id, userId(principal)));
    }

    /**
     * POST /api/v1/addresses
     * Creates a new address. Returns 201 Created with the saved address.
     */
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        AddressResponse response = addressService.createAddress(userId(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/v1/addresses/{id}
     * Full update of an existing address.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable String id,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(addressService.updateAddress(id, userId(principal), request));
    }

    /**
     * DELETE /api/v1/addresses/{id}
     * Deletes an address. If it was the default, the next address is auto-promoted.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        addressService.deleteAddress(id, userId(principal));
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/v1/addresses/{id}/default
     * Promotes this address to the user's default delivery address.
     */
    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(addressService.setDefaultAddress(id, userId(principal)));
    }
}
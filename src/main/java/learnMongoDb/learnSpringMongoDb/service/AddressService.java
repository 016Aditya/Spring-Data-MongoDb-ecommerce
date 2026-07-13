package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.AddressDto.AddressRequest;
import learnMongoDb.learnSpringMongoDb.dto.AddressDto.AddressResponse;
import learnMongoDb.learnSpringMongoDb.entity.Address;
import learnMongoDb.learnSpringMongoDb.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AddressService
 *
 * Business logic for the standalone Address collection:
 *   - list / get / create / update / delete
 *   - setDefault  (promote one address, demote all others for that user)
 *
 * Ownership is always verified against the requesting userId so that
 * users can only read and modify their own addresses.
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .fullName(a.getFullName())
                .phoneNumber(a.getPhoneNumber())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .city(a.getCity())
                .state(a.getState())
                .zipCode(a.getZipCode())
                .country(a.getCountry())
                .defaultAddress(a.isDefaultAddress())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    /**
     * Clears defaultAddress = true on every address that currently holds
     * the default flag for this user (there should only be one, but we
     * bulk-clear to be safe).
     */
    private void clearCurrentDefault(String userId) {
        List<Address> currentDefaults = addressRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(Address::isDefaultAddress)
                .toList();

        if (!currentDefaults.isEmpty()) {
            currentDefaults.forEach(a -> a.setDefaultAddress(false));
            addressRepository.saveAll(currentDefaults);
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/addresses
     * Returns all addresses for the authenticated user.
     */
    public List<AddressResponse> getAddresses(String userId) {
        return addressRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /api/v1/addresses/{id}
     */
    public AddressResponse getAddress(String id, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"));
        return toResponse(address);
    }

    /**
     * POST /api/v1/addresses
     *
     * If this is the user's first address, it is automatically set as default
     * regardless of the request flag. Otherwise the flag from the request is used.
     */
    public AddressResponse createAddress(String userId, AddressRequest request) {
        boolean isFirst = addressRepository.countByUserId(userId) == 0;
        boolean makeDefault = isFirst || request.isDefaultAddress();

        if (makeDefault) {
            clearCurrentDefault(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .defaultAddress(makeDefault)
                .build();

        return toResponse(addressRepository.save(address));
    }

    /**
     * PUT /api/v1/addresses/{id}
     */
    public AddressResponse updateAddress(String id, String userId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"));

        if (request.isDefaultAddress() && !address.isDefaultAddress()) {
            clearCurrentDefault(userId);
            address.setDefaultAddress(true);
        } else if (!request.isDefaultAddress() && address.isDefaultAddress()) {
            // Prevent unsetting the only default — keep it as default silently
            long count = addressRepository.countByUserId(userId);
            if (count > 1) {
                address.setDefaultAddress(false);
            }
        }

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry() != null ? request.getCountry() : address.getCountry());

        return toResponse(addressRepository.save(address));
    }

    /**
     * DELETE /api/v1/addresses/{id}
     *
     * If the deleted address was the default and other addresses exist,
     * the most recently created remaining address is promoted to default.
     */
    public void deleteAddress(String id, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"));

        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserIdOrderByCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                remaining.get(0).setDefaultAddress(true);
                addressRepository.save(remaining.get(0));
            }
        }
    }

    /**
     * PATCH /api/v1/addresses/{id}/default
     * Promotes the given address to default and demotes all others.
     */
    public AddressResponse setDefaultAddress(String id, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"));

        if (!address.isDefaultAddress()) {
            clearCurrentDefault(userId);
            address.setDefaultAddress(true);
            addressRepository.save(address);
        }

        return toResponse(address);
    }
}
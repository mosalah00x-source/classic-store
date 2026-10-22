package com.classicstore.identity.controller;

import com.classicstore.identity.dto.AddressInput;
import com.classicstore.identity.dto.AddressResponse;
import com.classicstore.identity.security.UserPrincipal;
import com.classicstore.identity.service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AddressResponse>> getAddresses(@AuthenticationPrincipal UserPrincipal principal) {
        List<AddressResponse> addresses = addressService.getAddressesForUser(principal.getUserId());
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AddressResponse> createAddress(@AuthenticationPrincipal UserPrincipal principal,
                                                         @RequestBody AddressInput input) {
        AddressResponse created = addressService.createAddress(principal.getUserId(), input);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AddressResponse> getAddressById(@AuthenticationPrincipal UserPrincipal principal,
                                                          @PathVariable UUID addressId) {
        AddressResponse address = addressService.getAddressByIdAndUser(addressId, principal.getUserId());
        return ResponseEntity.ok(address);
    }
}

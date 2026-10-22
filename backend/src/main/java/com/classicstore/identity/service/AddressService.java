package com.classicstore.identity.service;

import com.classicstore.common.exception.BusinessException;
import com.classicstore.identity.domain.AddressEntity;
import com.classicstore.identity.dto.AddressInput;
import com.classicstore.identity.dto.AddressResponse;
import com.classicstore.identity.repository.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesForUser(UUID userId) {
        return addressRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AddressResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse createAddress(UUID userId, AddressInput input) {
        if (Boolean.TRUE.equals(input.getIsDefault())) {
            addressRepository.unsetDefaultForUser(userId);
            addressRepository.flush();
        }

        AddressEntity entity = new AddressEntity();
        entity.setUserId(userId);
        entity.setRecipientName(input.getRecipientName() != null ? input.getRecipientName() : "Recipient");
        entity.setPhone(input.getPhone() != null ? input.getPhone() : "");
        entity.setCountryCode(input.getCountryCode() != null ? input.getCountryCode() : "EG");
        entity.setGovernorateId(input.getGovernorateId());
        entity.setCityId(input.getCityId());
        entity.setDistrict(input.getDistrict());
        entity.setStreet(input.getStreet() != null ? input.getStreet() : "");
        entity.setBuilding(input.getBuilding());
        entity.setApartment(input.getApartment());
        entity.setPostalCode(input.getPostalCode());
        entity.setLatitude(input.getLatitude());
        entity.setLongitude(input.getLongitude());
        entity.setDefault(Boolean.TRUE.equals(input.getIsDefault()));

        AddressEntity saved = addressRepository.save(entity);
        return new AddressResponse(saved);
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressByIdAndUser(UUID addressId, UUID userId) {
        AddressEntity entity = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(
                        "NOT_FOUND",
                        "العنصر المطلوب غير موجود",
                        "Address not found",
                        HttpStatus.NOT_FOUND
                ));
        return new AddressResponse(entity);
    }
}

package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto getProfile(String email) { return toDto(getUser(email)); }

    @Transactional
    public UserDto updateProfile(String email, UpdateProfileRequest req) {
        User user = getUser(email);
        if (req.getFirstName() != null)    user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)     user.setLastName(req.getLastName());
        if (req.getPhone() != null)        user.setPhone(req.getPhone());
        if (req.getProfileImage() != null) user.setProfileImage(req.getProfileImage());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest req) {
        User user = getUser(email);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword()))
            throw new BadRequestException("Current password is incorrect");
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new BadRequestException("Passwords do not match");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password changed successfully");
    }

    public List<AddressDto> getAddresses(String email) {
        return addressRepository.findByUserId(getUser(email).getId())
            .stream().map(this::toAddressDto).toList();
    }

    @Transactional
    public AddressDto addAddress(String email, CreateAddressRequest req) {
        User user = getUser(email);
        if (req.isDefault()) {
            addressRepository.findByUserId(user.getId()).forEach(a -> {
                a.setDefaultAddress(false);
                addressRepository.save(a);
            });
        }
        Address address = Address.builder()
            .user(user)
            .addressType(Address.AddressType.valueOf(req.getAddressType()))
            .fullName(req.getFullName()).phone(req.getPhone())
            .addressLine1(req.getAddressLine1()).addressLine2(req.getAddressLine2())
            .city(req.getCity()).state(req.getState())
            .pincode(req.getPincode()).country(req.getCountry())
            .defaultAddress(req.isDefault())
            .build();
        return toAddressDto(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = getUser(email);
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(user.getId()))
            throw new ForbiddenException("Unauthorized");
        addressRepository.delete(address);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserDto toDto(User u) {
        return UserDto.builder()
            .id(u.getId()).firstName(u.getFirstName()).lastName(u.getLastName())
            .email(u.getEmail()).phone(u.getPhone()).profileImage(u.getProfileImage())
            .enabled(u.isEnabled())
            .roles(u.getRoles().stream().map(r -> r.getName().name()).toList())
            .createdAt(u.getCreatedAt()).build();
    }

    private AddressDto toAddressDto(Address a) {
        return AddressDto.builder()
            .id(a.getId()).addressType(a.getAddressType().name())
            .fullName(a.getFullName()).phone(a.getPhone())
            .addressLine1(a.getAddressLine1()).addressLine2(a.getAddressLine2())
            .city(a.getCity()).state(a.getState())
            .pincode(a.getPincode()).country(a.getCountry())
            .isDefault(a.isDefaultAddress())
            .build();
    }
}

package com.netflix.user.service;

import com.netflix.user.dto.ProfileDTO;
import com.netflix.user.model.UserProfile;
import com.netflix.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProfileService {
    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public List<ProfileDTO> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public ProfileDTO findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + id));
    }

    public ProfileDTO create(ProfileDTO dto) {
        var profile = new UserProfile();
        profile.setName(dto.name());
        profile.setAvatarUrl(dto.avatarUrl());
        profile.setKidProfile(dto.kidProfile());
        return toDto(repository.save(profile));
    }

    public ProfileDTO update(Long id, ProfileDTO dto) {
        var profile = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + id));
        profile.setName(dto.name());
        profile.setAvatarUrl(dto.avatarUrl());
        profile.setKidProfile(dto.kidProfile());
        return toDto(repository.save(profile));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ProfileDTO toDto(UserProfile profile) {
        return new ProfileDTO(profile.getId(), profile.getName(),
                profile.getAvatarUrl(), profile.isKidProfile());
    }
}

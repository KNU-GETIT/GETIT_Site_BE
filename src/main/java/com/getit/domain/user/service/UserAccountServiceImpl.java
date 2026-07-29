package com.getit.domain.user.service;

import com.getit.domain.user.dto.OAuthRegistrationResult;
import com.getit.domain.user.dto.OAuthUserRegistration;
import com.getit.domain.user.dto.UserAccount;
import com.getit.domain.user.entity.User;
import com.getit.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccountServiceImpl implements UserAccountService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuthRegistrationResult registerOrUpdateOAuthUser(OAuthUserRegistration registration) {
    return userRepository.findByProviderId(registration.providerId())
        .map(existing -> {
          existing.updateProfile(registration.name(), registration.profileImageUrl());
          return new OAuthRegistrationResult(UserAccount.from(existing), false);
        })
        .orElseGet(() -> {
          User created = userRepository.save(User.createGuest(
              registration.providerId(),
              registration.email(),
              registration.name(),
              registration.profileImageUrl()
          ));
          return new OAuthRegistrationResult(UserAccount.from(created), true);
        });
  }

  @Override
  public Optional<UserAccount> findActiveById(Long userId) {
    return userRepository.findById(userId)
        .filter(user -> !user.isDeleted())
        .map(UserAccount::from);
  }
}

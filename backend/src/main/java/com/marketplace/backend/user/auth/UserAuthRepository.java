package com.marketplace.backend.user.auth;

import com.marketplace.backend.models.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

  UserAuth findByUserId(Long userId);
}

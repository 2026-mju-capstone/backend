package com.zoopick.server.repository;

import com.zoopick.server.entity.User;
import com.zoopick.server.exception.DataNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    default User findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> DataNotFoundException.from("사용자", id));
    }

    Optional<User> findBySchoolEmail(String schoolEmail);

    default User findBySchoolEmailOrThrow(String schoolEmail) {
        return findBySchoolEmail(schoolEmail)
                .orElseThrow(() -> DataNotFoundException.from("사용자", schoolEmail));
    }

    Optional<User> findByNickname(String nickname);

    default User findByNicknameOrThrow(String nickname) {
        return findByNickname(nickname)
                .orElseThrow(() -> DataNotFoundException.from("사용자", nickname));
    }

    boolean existsByNickname(String nickname);


}
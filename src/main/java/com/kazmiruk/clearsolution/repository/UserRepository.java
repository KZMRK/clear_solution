package com.kazmiruk.clearsolution.repository;

import com.kazmiruk.clearsolution.model.entity.User;
import com.kazmiruk.clearsolution.model.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    default User getUserById(Long id) {
        return this.findById(id).orElseThrow(() ->
                new NotFoundException("User with id %d not found".formatted(id))
        );
    }
}

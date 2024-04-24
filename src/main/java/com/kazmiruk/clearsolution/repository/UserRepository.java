package com.kazmiruk.clearsolution.repository;

import com.kazmiruk.clearsolution.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

package com.kazmiruk.clearsolution.service;

import com.kazmiruk.clearsolution.mapper.UserMapper;
import com.kazmiruk.clearsolution.model.dto.UserDto;
import com.kazmiruk.clearsolution.model.entity.User;
import com.kazmiruk.clearsolution.model.exception.BadRequestException;
import com.kazmiruk.clearsolution.model.properties.UserProperties;
import com.kazmiruk.clearsolution.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserProperties userProperties;

    public void createUser(UserDto userRequest) {
        checkIfUserWithEmailAlreadyExists(userRequest.getEmail());
        int userAge = Period.between(userRequest.getDateOfBirth(), LocalDate.now()).getYears();
        if (userAge < userProperties.getAge()) {
            throw new BadRequestException(
                    "User must be %d years old or older".formatted(userProperties.getAge())
            );
        }
        User user = userMapper.toEntity(userRequest);
        userRepository.save(user);
    }

    private void checkIfUserWithEmailAlreadyExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(
                    "User with email '%s' already exists".formatted(email)
            );
        }
    }
}

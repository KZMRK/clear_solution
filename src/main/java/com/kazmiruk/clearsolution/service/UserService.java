package com.kazmiruk.clearsolution.service;

import com.kazmiruk.clearsolution.mapper.UserMapper;
import com.kazmiruk.clearsolution.model.dto.UserDto;
import com.kazmiruk.clearsolution.model.entity.User;
import com.kazmiruk.clearsolution.model.exception.BadRequestException;
import com.kazmiruk.clearsolution.model.properties.UserProperties;
import com.kazmiruk.clearsolution.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserProperties userProperties;

    @Transactional
    public UserDto createUser(UserDto userRequest) {
        checkIsUserEmailUnique(userRequest.getEmail());
        checkUserAge(userRequest.getDateOfBirth());
        User user = userMapper.toEntity(userRequest);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    private void checkIsUserEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(
                    "User with email '%s' already exists".formatted(email)
            );
        }
    }

    private void checkUserAge(LocalDate dateOfBirth) {
        int userAge = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (userAge < userProperties.getAge()) {
            throw new BadRequestException(
                    "User must be %d years old or older".formatted(userProperties.getAge())
            );
        }
    }

    @Transactional(readOnly = true)
    public Set<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toDto).collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.getUserById(id);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userRequest) {
        User user = userRepository.getUserById(id);
        if (!user.getEmail().equals(userRequest.getEmail())) {
            checkIsUserEmailUnique(userRequest.getEmail());
        }
        checkUserAge(userRequest.getDateOfBirth());
        userMapper.updateEntity(user, userRequest);
        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.getUserById(id);
        userRepository.delete(user);
    }

    public List<UserDto> getUsersBtBirthDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException(
                    "Date FROM ('%s') should be before date TO ('%s')".formatted(from, to)
            );
        }
        return userRepository.findAllByDateOfBirthBetween(from, to).stream()
                .map(userMapper::toDto)
                .toList();
    }
}

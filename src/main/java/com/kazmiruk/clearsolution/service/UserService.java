package com.kazmiruk.clearsolution.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.kazmiruk.clearsolution.mapper.UserMapper;
import com.kazmiruk.clearsolution.model.dto.UserDto;
import com.kazmiruk.clearsolution.model.entity.User;
import com.kazmiruk.clearsolution.model.exception.BadRequestException;
import com.kazmiruk.clearsolution.model.exception.FieldValidationException;
import com.kazmiruk.clearsolution.model.properties.UserProperties;
import com.kazmiruk.clearsolution.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    private final ObjectMapper objectMapper;

    private final Validator validator;

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
        updateUserEntity(userRequest, user);
        return userMapper.toDto(user);
    }

    private void updateUserEntity(UserDto userRequest, User targetUser) {
        if (!targetUser.getEmail().equals(userRequest.getEmail())) {
            checkIsUserEmailUnique(userRequest.getEmail());
        }
        checkUserAge(userRequest.getDateOfBirth());
        userMapper.updateEntity(targetUser, userRequest);
    }

    @Transactional
    public UserDto updateUser(Long id, JsonPatch userPatchRequest) {
        User user = userRepository.getUserById(id);
        UserDto userDto = userMapper.toDto(user);
        userDto = applyPatchToUserDto(userPatchRequest, userDto);
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
        if (!violations.isEmpty()) {
            throw new FieldValidationException(violations);
        }
        updateUserEntity(userDto, user);
        return userMapper.toDto(user);
    }

    @SneakyThrows
    private UserDto applyPatchToUserDto(JsonPatch userPathRequest, UserDto targetUser) {
        JsonNode patchedUser = userPathRequest.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patchedUser, UserDto.class);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.getUserById(id);
        userRepository.delete(user);
    }

    public List<UserDto> getUsersByBirthDateRange(LocalDate from, LocalDate to) {
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

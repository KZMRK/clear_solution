package com.kazmiruk.clearsolution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.kazmiruk.clearsolution.mapper.UserMapper;
import com.kazmiruk.clearsolution.model.dto.UserDto;
import com.kazmiruk.clearsolution.model.entity.User;
import com.kazmiruk.clearsolution.model.exception.BadRequestException;
import com.kazmiruk.clearsolution.model.properties.UserProperties;
import com.kazmiruk.clearsolution.repository.UserRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    private UserDto userRequest;

    private User user;

    private UserDto userResponse;



    @Mock
    private UserProperties userProperties;

    @BeforeEach
    public void init() {
        userRequest = UserDto.builder()
                .firstName("Dmytro")
                .lastName("Kazmiruk")
                .email("dima.kazmiruk.05@gmail.com")
                .dateOfBirth(LocalDate.of(2004, 10, 5))
                .build();
        user = User.builder()
                .id(1L)
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .dateOfBirth(userRequest.getDateOfBirth())
                .build();
        userResponse = UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .build();
    }

    @Test
    public void UserService_CreateUser_ReturnUserDto() {
        when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(false);
        when(userProperties.getAge()).thenReturn(18);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(Mockito.any(User.class))).thenReturn(userResponse);

        UserDto userResponse = userService.createUser(userRequest);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isNotNull();
        assertThat(userResponse.getFirstName()).isEqualTo(userRequest.getFirstName());
        assertThat(userResponse.getLastName()).isEqualTo(userRequest.getLastName());
        assertThat(userResponse.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(userResponse.getDateOfBirth()).isEqualTo(userRequest.getDateOfBirth());
    }

    @Test
    public void UserService_CreateUser_ThrowEmailExistsException() {
        when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> userService.createUser(userRequest)).hasMessage("User with email '%s' already exists".formatted(userRequest.getEmail()));
    }

    @Test
    public void UserService_CreateUser_ThrowAgeMustBeEqualOrOlder() {
        when(userProperties.getAge()).thenReturn(22);

        userRequest.setDateOfBirth(LocalDate.now());
        assertThatThrownBy(() -> userService.createUser(userRequest)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> userService.createUser(userRequest)).hasMessage("User must be %d years old or older".formatted(userProperties.getAge()));
    }

    @Test
    public void UserService_GetUserById_ReturnUserDto() {
        when(userRepository.getUserById(1L)).thenReturn(user);
        when(userMapper.toDto(Mockito.any(User.class))).thenReturn(userResponse);

        UserDto actualResponse = userService.getUserById(1L);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isNotNull();
        assertThat(actualResponse.getFirstName()).isEqualTo(userRequest.getFirstName());
        assertThat(actualResponse.getLastName()).isEqualTo(userRequest.getLastName());
        assertThat(actualResponse.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(actualResponse.getDateOfBirth()).isEqualTo(userRequest.getDateOfBirth());
    }

    @Test
    public void UserService_GetAllUsers_ReturnUserDtos() {
        List<User> users = List.of(user,  user, user);
        Set<UserDto> expectedResponses = users.stream().map(userMapper::toDto).collect(Collectors.toSet());

        when(userRepository.findAll()).thenReturn(users);

        Set<UserDto> actualResponses = userService.getAllUsers();

        assertThat(actualResponses).isNotEmpty();
        assertThat(actualResponses).size().isEqualTo(expectedResponses.size());
        assertThat(actualResponses).containsAll(expectedResponses);
    }

    @Test
    public void UserService_UpdateUser_ReturnUpdatedUserDto() {
        Long userId = 1L;
        when(userRepository.getUserById(userId)).thenReturn(user);
        userRequest.setFirstName("Oleh");
        userRequest.setAddress("Ukraine, Lviv");
        userResponse.setFirstName("Oleh");
        userResponse.setAddress("Ukraine, Lviv");
        when(userMapper.toDto(Mockito.any(User.class))).thenReturn(userResponse);

        UserDto actualResponse = userService.updateUser(userId, userRequest);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isNotNull();
        assertThat(actualResponse.getFirstName()).isEqualTo(userRequest.getFirstName());
        assertThat(actualResponse.getLastName()).isEqualTo(userRequest.getLastName());
        assertThat(actualResponse.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(actualResponse.getDateOfBirth()).isEqualTo(userRequest.getDateOfBirth());
        assertThat(actualResponse.getAddress()).isEqualTo(userRequest.getAddress());
    }

    @Test
    public void UserService_UpdateUserPatch_ReturnUpdatedUserDto() throws JsonPatchException, JsonProcessingException {
        Long userId = 1L;
        JsonPatch patchRequest = Mockito.mock();

        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(JsonNode.class))).thenReturn(Mockito.mock(ObjectNode.class));
        when(userRepository.getUserById(userId)).thenReturn(user);
        when(objectMapper.treeToValue(Mockito.any(TreeNode.class), Mockito.eq(UserDto.class))).thenReturn(userResponse);
        when(patchRequest.apply(Mockito.any())).thenReturn(mock(JsonNode.class));
        when(validator.validate(Mockito.any())).thenReturn(new HashSet<>());
        when(userMapper.toDto(Mockito.any(User.class))).thenReturn(userResponse);

        UserDto actualResponse = userService.updateUser(userId, patchRequest);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getId()).isNotNull();
        assertThat(actualResponse.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(actualResponse.getLastName()).isEqualTo(user.getLastName());
        assertThat(actualResponse.getEmail()).isEqualTo(user.getEmail());
        assertThat(actualResponse.getDateOfBirth()).isEqualTo(user.getDateOfBirth());
        assertThat(actualResponse.getAddress()).isEqualTo(user.getAddress());
    }

    @Test
    public void UserService_DeleteUser_ReturnVoid() {
        Long userId = 1L;
        when(userRepository.getUserById(userId)).thenReturn(user);

        assertAll(() -> userService.deleteUser(userId));
    }

    @Test
    public void UserService_GetUsersByBirthDateRange_ReturnUsers() {
        List<User> users = List.of(user, user, user);
        LocalDate from = LocalDate.parse("2004-10-05");
        LocalDate to = LocalDate.parse("2006-06-28");

        when(userMapper.toDto(Mockito.any(User.class))).thenReturn(userResponse);
        when(userRepository.findAllByDateOfBirthBetween(from, to)).thenReturn(users);

        List<UserDto> expectedResponses = users.stream()
                .map(userMapper::toDto)
                .toList();

        List<UserDto> actualResponses = userService.getUsersByBirthDateRange(from, to);

        assertThat(actualResponses).isNotEmpty();
        assertThat(actualResponses.size()).isEqualTo(expectedResponses.size());
        assertThat(actualResponses).containsAll(expectedResponses);
    }

    @Test
    public void UserService_GetUsersByBirthDateRange_ThrowFromShouldBeBeforeToException() {
        LocalDate from = LocalDate.parse("2004-10-05");
        LocalDate to = LocalDate.parse("2006-06-28");

        assertThatThrownBy(() -> userService.getUsersByBirthDateRange(to, from)).hasMessage("Date FROM ('%s') should be before date TO ('%s')".formatted(to, from));
        assertThatThrownBy(() -> userService.getUsersByBirthDateRange(to, from)).isInstanceOf(BadRequestException.class);
    }

}

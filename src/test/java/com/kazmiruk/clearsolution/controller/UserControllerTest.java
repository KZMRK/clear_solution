package com.kazmiruk.clearsolution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import com.kazmiruk.clearsolution.model.dto.UserDto;
import com.kazmiruk.clearsolution.service.UserService;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userRequest;

    @BeforeEach
    public void init() {
        userRequest = UserDto.builder()
                .firstName("Dmytro")
                .lastName("Kazmiruk")
                .email("dima.kazmiruk.05@gmail.com")
                .dateOfBirth(LocalDate.of(2004, 10, 5))
                .build();

    }


    @Test
    public void UserController_CreateUser_ReturnCreatedUserDto() throws Exception {
        when(userService.createUser(Mockito.any(UserDto.class))).thenReturn(userRequest);

        ResultActions response = mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequest))
        );
        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpectAll(
                        MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(userRequest.getFirstName())),
                        MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(userRequest.getLastName())),
                        MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(userRequest.getEmail())),
                        MockMvcResultMatchers.jsonPath("$.dateOfBirth", CoreMatchers.is(userRequest.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                )
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void UserController_GetALlUsers_ReturnUserDtos() throws Exception {

        Set<UserDto> userResponses = Set.of(
                UserDto.builder()
                        .id(1L)
                        .firstName("Dmytro")
                        .lastName("Kazmiruk")
                        .email("dima.kazmiruk.05@gmail.com")
                        .dateOfBirth(LocalDate.of(2004, 10, 5))
                        .build(),
                UserDto.builder()
                        .id(2L)
                        .firstName("Dmytro")
                        .lastName("Kazmiruk")
                        .email("dima.kazmiruk@gmail.com")
                        .dateOfBirth(LocalDate.of(2004, 10, 5))
                        .build()
        );

        Mockito.when(userService.getAllUsers()).thenReturn(userResponses);

        ResultActions response = mockMvc.perform(
                get("/api/v1/users")
        );
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(userResponses.size())));
    }

    @Test
    public void UserController_GetUserById_ReturnUserDto() throws Exception {
        UserDto userResponse = UserDto.builder()
                .id(1L)
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .dateOfBirth(userRequest.getDateOfBirth())
                .build();

        Mockito.when(userService.getUserById(1L)).thenReturn(userResponse);

        ResultActions response = mockMvc.perform(
                get("/api/v1/users/1")
        );

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpectAll(
                        MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(userResponse.getFirstName())),
                        MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(userResponse.getLastName())),
                        MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(userResponse.getEmail())),
                        MockMvcResultMatchers.jsonPath("$.dateOfBirth", CoreMatchers.is(userResponse.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                )
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void UserController_UpdateUser_ReturnUserDto() throws Exception {
        when(userService.updateUser(Mockito.anyLong(), Mockito.any(UserDto.class))).thenReturn(userRequest);

        ResultActions response = mockMvc.perform(
                put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
        );
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpectAll(
                        MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(userRequest.getFirstName())),
                        MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(userRequest.getLastName())),
                        MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(userRequest.getEmail())),
                        MockMvcResultMatchers.jsonPath("$.dateOfBirth", CoreMatchers.is(userRequest.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                )
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void UserController_UpdatePatchUser_ReturnUserDto() throws Exception {
        userRequest.setFirstName("Oleh");
        when(userService.updateUser(Mockito.anyLong(), Mockito.any(JsonPatch.class))).thenReturn(userRequest);

        JsonPatch patchRequest = new JsonPatch(
                List.of(
                        new ReplaceOperation(
                                new JsonPointer("/firstName"),
                                TextNode.valueOf("Oleh")
                        )
                )
        );

        ResultActions response = mockMvc.perform(
                patch("/api/v1/users/1")
                        .contentType("application/json-patch+json")
                        .content(objectMapper.writeValueAsString(patchRequest))
        );
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpectAll(
                        MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(userRequest.getFirstName())),
                        MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(userRequest.getLastName())),
                        MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(userRequest.getEmail())),
                        MockMvcResultMatchers.jsonPath("$.dateOfBirth", CoreMatchers.is(userRequest.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                )
                .andDo(MockMvcResultHandlers.print());

    }


    @Test
    public void UserController_DeleteUser_ReturnNoContent() throws Exception {

        doNothing().when(userService).deleteUser(1L);

        ResultActions response = mockMvc.perform(
                delete("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
        );
        response.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void UserController_GetUserByBirthRange_ReturnUserDtos() throws Exception {
        List<UserDto> expectedResponses = List.of(userRequest, userRequest, userRequest);

        LocalDate from = LocalDate.parse("2004-10-05");
        LocalDate to = LocalDate.parse("2006-06-28");
        when(userService.getUsersByBirthDateRange(from, to)).thenReturn(expectedResponses);

        ResultActions response = mockMvc.perform(
                get("/api/v1/users/byBirthDateRange")
                        .param("from", from.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        .param("to", to.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
        );

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(expectedResponses.size())))
                .andExpectAll(
                        MockMvcResultMatchers.jsonPath("$[0].firstName", CoreMatchers.is(expectedResponses.get(0).getFirstName())),
                        MockMvcResultMatchers.jsonPath("$[0].lastName", CoreMatchers.is(expectedResponses.get(0).getLastName())),
                        MockMvcResultMatchers.jsonPath("$[0].email", CoreMatchers.is(expectedResponses.get(0).getEmail())),
                        MockMvcResultMatchers.jsonPath("$[0].dateOfBirth", CoreMatchers.is(expectedResponses.get(0).getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                )
                .andDo(MockMvcResultHandlers.print());
    }



}

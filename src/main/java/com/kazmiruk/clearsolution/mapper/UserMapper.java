package com.kazmiruk.clearsolution.mapper;

import com.kazmiruk.clearsolution.model.dto.UserDto;
import com.kazmiruk.clearsolution.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget User userTarget, UserDto userDto);
}

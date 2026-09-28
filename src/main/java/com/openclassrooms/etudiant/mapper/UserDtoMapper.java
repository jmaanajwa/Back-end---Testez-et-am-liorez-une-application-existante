package com.openclassrooms.etudiant.mapper;

import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Converts registration data into user entities with MapStruct.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserDtoMapper {
    /**
     * Copies registration details, including the password still in plaintext.
     * This mapping does not populate the ID, timestamps, or authorities.
     *
     * @param registerDTO the registration data to convert
     * @return a new user entity, or {@code null} if the DTO is null
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toEntity(RegisterDTO registerDTO);
}

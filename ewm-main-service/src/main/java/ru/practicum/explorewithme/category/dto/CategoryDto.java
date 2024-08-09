package ru.practicum.explorewithme.category.dto;

import lombok.*;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    @Size(min = 2, max = 50)
    @NotBlank
    private String name;
}


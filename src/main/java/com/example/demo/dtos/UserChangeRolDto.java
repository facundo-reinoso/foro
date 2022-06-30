package com.example.demo.dtos;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UserChangeRolDto {
    @NotEmpty
    @NotNull
    public Long idUser;

    @NotEmpty
    @NotNull
    public Long idRol;
}

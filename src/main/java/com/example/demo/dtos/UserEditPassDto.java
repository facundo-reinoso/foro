package com.example.demo.dtos;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UserEditPassDto {
    @NotEmpty(message = "Password no puede estar vacio")
    @NotNull
    public String password;
}

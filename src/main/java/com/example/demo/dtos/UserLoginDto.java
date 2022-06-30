package com.example.demo.dtos;

import javax.validation.constraints.NotEmpty;

public class UserLoginDto {
    @NotEmpty(message = "Usuario no puede estar vacio")
    public String username;

    @NotEmpty(message = "Password no puede estar vacio")
    public String password;
}

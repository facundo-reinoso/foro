package com.example.demo.dtos;

import ch.qos.logback.core.boolex.EvaluationException;

import javax.validation.constraints.*;
import java.util.Date;

public class UserCreateDto {
    @NotEmpty(message = "Nombre no puede estar vacio")
    @NotNull
    public String name;

    @NotEmpty(message = "Apellido no puede estar vacio")
    @NotNull
    public String lastName;

    @Past
    @NotNull
    public Date birthDate;

    @NotEmpty(message = "Email no puede estar vacio")
    @NotNull
    @Email(message = "Ingrese formato valido de e-mail")
    public String email;

    @NotEmpty(message = "Usuario no puede estar vacio")
    @NotNull
    public String username;

    @NotEmpty(message = "Password no puede estar vacio")
    @NotNull
    public String password;

    @NotNull
    public Long idRole;
}

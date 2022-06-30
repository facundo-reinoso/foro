package com.example.demo.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

public class UserEditDto {
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
}

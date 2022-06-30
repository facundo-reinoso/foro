package com.example.demo.controllers;

import com.example.demo.dtos.UserLoginDto;
import com.example.demo.exceptions.*;
import com.example.demo.models.UsersEntity;
import com.example.demo.services.LoginService;
import com.example.demo.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private LoginService login;
    private JWTUtil jwtutil;

    @Autowired
    public AuthController(LoginService login, JWTUtil jwt){
        this.jwtutil = jwt;
        this.login = login;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto loginDto) {
        log.info("Peticion del usuario: "+ loginDto.username + " para el login");
        Long id;
        String role = "";

        try {
            Optional<UsersEntity> usersEntity = login.getUserByCredentials(loginDto);
            id = usersEntity.get().getIdUsuario();
            role = usersEntity.get().getIdRole().getName();

            return new ResponseEntity<>(login.login(loginDto, id, role), HttpStatus.OK);

        }catch (TokenException u) {
            log.error("Token Exception - Create");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (RepositoryException e) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

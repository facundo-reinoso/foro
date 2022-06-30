package com.example.demo.controllers;

import com.example.demo.context.RequestContext;
import com.example.demo.dtos.UserLoginDto;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.models.RolesEntity;
import com.example.demo.models.UsersEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.LoginService;
import com.example.demo.utils.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "spring.config.location=application.properties" })
class AuthControllerTest {

    private static UserRepository repository;
    private static LoginService service;
    private static AuthController controller;
    private JWTUtil util;

    private static UserLoginDto loginDto;

    @BeforeEach
    void setUp() {

        String key = "palabrasecreta";
        String issuer = "Main";
        long ttlMillis = 10800000;

        util = new JWTUtil(key, issuer, ttlMillis);
        repository = mock(UserRepository.class);
        service = new LoginService(repository, util);
        controller = new AuthController(service, util);

        loginDto = new UserLoginDto();
        loginDto.username = "encontrado";
        loginDto.password = "123456";
    }

    @Test
    void loginUsuarioEmpty() {
        loginDto.username = "";
        loginDto.password = "123456";

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(loginDto);

        ResponseEntity<String> response;

        if(violations.isEmpty()){
            response = (ResponseEntity<String>) controller.login(loginDto);
        }else {
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }

    @Test
    void loginPasswordEmpty(){
        loginDto.username = "encontrado";
        loginDto.password = "";

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(loginDto);

        ResponseEntity<String> response;

        if(violations.isEmpty()){
            response = (ResponseEntity<String>) controller.login(loginDto);
        }else {
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }

    @Test
    void login(){
        UsersEntity entity = new UsersEntity();
        entity.setUsername("encontrado");
        entity.setPassword("123456");
        entity.setIdRole(new RolesEntity(1L));

        when(repository.getUser("encontrado")).thenReturn(Optional.of(entity));

        ResponseEntity<String> response = (ResponseEntity<String>) controller.login(loginDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void loginUsuarioNotFound(){
        loginDto.username = "no_encontrado";

        when(repository.getUser("no_encontrado")).thenThrow(new UserNotFoundException());
        ResponseEntity<String> response = (ResponseEntity<String>) controller.login(loginDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void loginWrongPassword(){
        UsersEntity entity = new UsersEntity();
        entity.setUsername("encontrado");
        entity.setPassword("123456");
        loginDto.password = "456789";

        when(repository.getUser("encontrado")).thenReturn(Optional.of(entity));
        ResponseEntity<String> response = (ResponseEntity<String>) controller.login(loginDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginException(){
        loginDto.username = "exception";

        when(repository.getUser("exception")).thenThrow(new RuntimeException());
        ResponseEntity<String> response = (ResponseEntity<String>) controller.login(loginDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
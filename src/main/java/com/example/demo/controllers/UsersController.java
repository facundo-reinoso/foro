package com.example.demo.controllers;

import com.example.demo.context.RequestContext;
import com.example.demo.dtos.*;
import com.example.demo.exceptions.*;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UsersService;
import com.example.demo.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("users")

public class UsersController {
    public static final Logger log = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private RequestContext context;

    @Autowired
    private UsersService usersService;

    @Autowired
    private JWTUtil jwtutil;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    public UsersController(UsersService service, JWTUtil jwtUtil, RequestContext context) {
        this.usersService = service;
        this.jwtutil = jwtUtil;
        this.context = context;
    }

    @PostMapping("register")
    @Validated
    public ResponseEntity<String> register(@Valid @RequestBody UserCreateDto dto) {

        try {
            log.info("Peticion de registro nuevo usuario");
            usersService.register(dto);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (DuplicatedUserException | NotMinimalRequisitePasswordException e) {
            log.error("Error al crear el nuevo usuario");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (RepositoryException d) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getUser(@RequestParam(required = true) String user) {
        try {
            log.info(String.format("Peticion de usuario '%s' para request creada en '%s' con rol '%s'", context.getUserId(), context.getCreated(), context.getRoles().iterator().next().toString()));
            return new ResponseEntity<>(usersService.getUser(user), HttpStatus.OK);

        } catch (UserNotFoundException u) {
            log.error("Usuario no encontrado");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.NOT_FOUND);

        } catch (RepositoryException e) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("")
    @Validated
    public ResponseEntity<?> editProfile(@Valid @RequestBody UserEditDto profile) {
        try {
            log.info(String.format("Peticion de usuario '%s' para request creada en '%s'", context.getUserId(), context.getCreated()));
            usersService.editProfile(profile, Long.valueOf(context.getUserId()));
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (UserNotFoundException u) {
            log.error("Usuario no encontrado");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RepositoryException e) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("rol")
    public ResponseEntity<?> changeRol(@RequestBody UserChangeRolDto userChangeRolDto) {
        try {
            if (!context.hasRole("Admin")) {
                log.info("Peticion de User denegada para cambiar rol de usuario");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            log.info("Peticion de Admin concedida para cambiar rol de usuario");
            usersService.changeRol(userChangeRolDto);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (UserNotFoundException u) {
            log.error("Usuario no encontrado");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.NOT_FOUND);

        } catch (RepositoryException e) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("password")
    @Validated
    public ResponseEntity<String> editPass(@Valid @RequestBody UserEditPassDto pass) {
        try {
            log.info(String.format("Peticion de usuario '%s' para request creada en '%s'", context.getUserId(), context.getCreated()));
            usersService.editPass(Long.valueOf(context.getUserId()), pass.password);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (UserNotFoundException u) {
            log.error("Usuario no encontrado");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.NOT_FOUND);

        } catch (NotMinimalRequisitePasswordException u) {
            log.error("La contraseña con cumple con los requisitos");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (RepositoryException e) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("")
    public ResponseEntity<String> delete() {
        try {
            log.info(String.format("Peticion de usuario '%s' para request creada en '%s'", context.getUserId(), context.getCreated()));
            usersService.delete(Long.valueOf(context.getUserId()));
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (UserNotFoundException u) {
            log.error("Usuario no encontrado");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.NOT_FOUND);

        } catch (RepositoryException e) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {

        if (!context.hasRole("Admin")) {
            log.info("Peticion de User denegada para eliminar un usario en particular");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        log.info("Peticion de Admin concedida para eliminar un usario en particular");

        try {
            usersService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserNotFoundException u) {
            log.error("Usuario no encontrado");
            return new ResponseEntity<>(u.getMessage(), HttpStatus.NOT_FOUND);

        } catch (RepositoryException e) {
            log.error("Error desde el repositorio");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}

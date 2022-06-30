package com.example.demo.controllers;

import com.example.demo.context.RequestContext;
import com.example.demo.dtos.UserChangeRolDto;
import com.example.demo.dtos.UserCreateDto;
import com.example.demo.dtos.UserEditDto;
import com.example.demo.dtos.UserEditPassDto;
import com.example.demo.models.RolesEntity;
import com.example.demo.models.UsersEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UsersService;
import com.example.demo.utils.JWTUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UsersControllerTest {
    private static UserRepository repository;
    private static UsersService service;
    private static UsersController controller;
    private static JWTUtil jwtUtil;
    private static RequestContext context;

    private static final String name = "nombre";
    private static final String lastName = "apellido";
    private static final Date birthDate = new Date(1990 - 05 - 22);
    private static final String email = "mail@gmail.com";
    private static final String username = "username";
    private static final String pass = "123456";
    private static final int status = 1;

    private static final UsersEntity user = new UsersEntity();

    private static final List<UsersEntity> userList = new ArrayList<>();

//    final Logger logger = (Logger) LoggerFactory.getLogger(controller.getClass());

    private void initialization(String idUser, String userRol){
        String key = "palabrasecreta";
        String issuer = "Main";
        long ttlMillis = 10800000;
        Collection roles = new ArrayList();
        roles.add(new SimpleGrantedAuthority(userRol));

        LocalDateTime dateCreated = LocalDateTime.now();

        jwtUtil = new JWTUtil(key, issuer, ttlMillis);
        context = new RequestContext(dateCreated, idUser, roles);

        repository = mock(UserRepository.class);
        service = new UsersService(repository, context);
        controller = new UsersController(service, jwtUtil, context);

        Mockito.when(repository.getUserByUser("enc")).thenReturn(userList);
        Mockito.when(repository.getUserByUser("exception")).thenThrow(new RuntimeException());
        Mockito.when(repository.getUserByUser("no-enc")).thenReturn(Collections.emptyList());

        Mockito.when(repository.getUser("username")).thenReturn(Optional.empty());
        Mockito.when(repository.getUser("duplicado")).thenReturn(Optional.of(user));
        Mockito.when(repository.getUser("exception")).thenThrow(new RuntimeException());

        Mockito.when(repository.getUserById(1L)).thenReturn(Optional.of(user));
        Mockito.when(repository.getUserById(2L)).thenReturn(Optional.empty());
        Mockito.when(repository.getUserById(10L)).thenThrow(new RuntimeException());
    }

    @BeforeAll
    static void setUpAll(){
        user.setIdUsuario(1L);
        user.setName(name);
        user.setLastName(lastName);
        user.setBirthDate(birthDate);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(pass);
        user.setIdRole(new RolesEntity(1L));
        user.setStatus(1);
        userList.add(user);
    }

    @Test
    void register_OK() {
        initialization("1", "Admin");
        UserCreateDto usuario = new UserCreateDto();
        usuario.name = "nombre";
        usuario.lastName = "apellido";
        usuario.email = "mail@gmail.com";
        usuario.birthDate = new Date(1990 - 01 - 01);
        usuario.username = "username";
        usuario.password = "12345678";

        ResponseEntity<String> response = controller.register(usuario);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void register_BAD_REQUEST() {
        initialization("1", "Admin");

        UserCreateDto usuario = new UserCreateDto();
        usuario.name = "";
        usuario.lastName = "apellido";
        usuario.email = "mail@gmail.com";
        usuario.birthDate = new Date(1990 - 01 - 01);
        usuario.username = "username";
        usuario.password = "1234567";
        usuario.idRole = 1L;

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(usuario);

        ResponseEntity<String> response;

        if(violations.isEmpty()){
            response = controller.register(usuario);
        }else {
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void register_NotMinimalRequisitePasswordException() {
        initialization("1", "Admin");

        String msj_error = "El password no cumple los minimos requisitos. Debe tener mínimo 8 dígitos";

        UserCreateDto usuario = new UserCreateDto();
        usuario.name = "nombre";
        usuario.lastName = "apellido";
        usuario.email = "mail@gmail.com";
        usuario.birthDate = new Date(1990 - 01 - 01);
        usuario.username = "username";
        usuario.password = "1234567";

        ResponseEntity<String> response = controller.register(usuario);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void register_DuplicatedUserException() {
        initialization("1", "Admin");

        String msj_error = "El usuario ya se encuentra registrado";

        UserCreateDto usuario2 = new UserCreateDto();
        usuario2.username = "duplicado";

        ResponseEntity<String> response = controller.register(usuario2);

        assertAll(
                () -> assertEquals(msj_error, response.getBody()),
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
        );
    }

    @Test
    void register_Exception() {
        initialization("1", "Admin");

        UserCreateDto user = new UserCreateDto();
        user.username = "exception";

        ResponseEntity<String> response = controller.register(user);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    //NO ANDA
    @Test
    void getUser_OK() {
        initialization("1", "Admin");

        ResponseEntity<?> response = controller.getUser("enc");

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(1, ((ArrayList) response.getBody()).size()),
                () -> assertInstanceOf(ArrayList.class, response.getBody())
        );
    }

    @Test
    void getUser_UserNotFoundException() {
        initialization("1", "Admin");

        ResponseEntity<?> response = controller.getUser("no-enc");

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
        );
    }

    @Test
    void getUser_Exception(){
        initialization("1", "Admin");

        ResponseEntity<?> response = controller.getUser("exception");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void editProfile_OK() {
        initialization("1", "Admin");

        UserEditDto usuario = new UserEditDto();
        ResponseEntity<?> response = controller.editProfile(usuario);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    void editProfile_NOT_FOUND() {
        initialization("2", "Admin");
        UserEditDto usuario = new UserEditDto();
        ResponseEntity<?> response = controller.editProfile(usuario);

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
        );
    }

    @Test
    void editProfile_BadRequest() {
        initialization("1", "Admin");
        UserEditDto userIncomplete = new UserEditDto();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserEditDto>> violations = validator.validate(userIncomplete);

        ResponseEntity<?> response;

        if(violations.isEmpty()){
            response = controller.editProfile(userIncomplete);
        }else {
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
        );
    }

    @Test
    void editProfile_Exception() {
        initialization("10", "Admin");

        UserEditDto usuario = new UserEditDto();
        ResponseEntity<?> response = controller.editProfile(usuario);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void editPass_OK() {
        initialization("1", "Admin");

        UserEditPassDto usuario = new UserEditPassDto();
        usuario.password = "32165498";

        ResponseEntity<String> response = controller.editPass(usuario);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    void editPass_UserNotFoundException() {
        initialization("2", "Admin");

        UserEditPassDto usuario = new UserEditPassDto();
        usuario.password = "32165489";

        ResponseEntity<String> response = controller.editPass(usuario);

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
        );
    }

    @Test
    void editPass_NotMinimalRequisitePasswordException() {
        initialization("1", "Admin");

        UserEditPassDto usuario = new UserEditPassDto();
        usuario.password = "3216549";

        ResponseEntity<String> response = controller.editPass(usuario);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
        );
    }

    @Test
    void editPass_Exception() {
        initialization("10", "Admin");

        UserEditPassDto usuario = new UserEditPassDto();
        usuario.password = "32165498";

        ResponseEntity<String> response = controller.editPass(usuario);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void delete_OK() {
        initialization("1", "Admin");

        ResponseEntity<String> response = controller.delete();

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    void delete_UserNotFoundException() {
        initialization("2", "Admin");

        ResponseEntity<String> response = controller.delete();

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode())
        );
    }

    @Test
    void delete_Exception() {
        initialization("10", "Admin");

        ResponseEntity<String> response = controller.delete();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void changeRol_OK(){
        initialization("1", "Admin");
        UserChangeRolDto dto = new UserChangeRolDto();
        dto.idRol=1L;
        dto.idUser=1L;

        ResponseEntity<?> response = controller.changeRol(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void changeRol_FORBIDDEN(){
        initialization("1", "User");
        UserChangeRolDto dto = new UserChangeRolDto();
        dto.idRol=1L;
        dto.idUser=1L;

        ResponseEntity<?> response = controller.changeRol(dto);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void changeRol_NOTFOUND(){
        initialization("1", "Admin");
        UserChangeRolDto dto = new UserChangeRolDto();
        dto.idRol=1L;
        dto.idUser=2L;

        ResponseEntity<?> response = controller.changeRol(dto);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void changeRol_Exception(){
        initialization("1", "Admin");
        UserChangeRolDto dto = new UserChangeRolDto();
        dto.idRol=1L;
        dto.idUser=10L;

        ResponseEntity<?> response = controller.changeRol(dto);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteUser_OK(){
        initialization("1", "Admin");
        ResponseEntity<String> response = controller.deleteUser(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteUser_NOTFOUND(){
        initialization("1", "Admin");
        ResponseEntity<String> response = controller.deleteUser(2L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteUser_Exception(){
        initialization("1", "Admin");
        ResponseEntity<String> response = controller.deleteUser(10L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteUser_FORBIDDEN(){
        initialization("1", "User");
        ResponseEntity<String> response = controller.deleteUser(1L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

}
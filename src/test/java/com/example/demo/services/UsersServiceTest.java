package com.example.demo.services;

import com.example.demo.context.RequestContext;
import com.example.demo.controllers.UsersController;
import com.example.demo.dtos.*;
import com.example.demo.exceptions.DuplicatedUserException;
import com.example.demo.exceptions.NotMinimalRequisitePasswordException;

import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.models.RolesEntity;
import com.example.demo.models.UsersEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JWTUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsersServiceTest {

    private static UserRepository repository;
    private static UsersService service;
    private static RequestContext context;
    private static UsersController controller;
    private static JWTUtil jwtUtil;

    private static final String name = "nombre";
    private static final String lastName = "apellido";
    private static final Date birthDate = new Date(1990 - 05 - 22);
    private static final String email = "mail@gmail.com";
    private static final String username = "username";
    private static final String pass = "123456";
    private static final int status = 1;

    private static final Logger log = LoggerFactory.getLogger(UsersServiceTest.class);

    private static final UsersEntity user = new UsersEntity();
    private static final List<UsersEntity> userList = new ArrayList<>();


    private void initialization(String idUser, String userRol){
        String key = "palabrasecreta";
        String issuer = "Main";
        long ttlMillis = 10800000;

        Collection roles = new ArrayList();
        roles.add(new SimpleGrantedAuthority("Admin"));
        LocalDateTime dateCreated = LocalDateTime.now();

        context = new RequestContext(dateCreated, "1", roles);
        jwtUtil = new JWTUtil(key, issuer, ttlMillis);

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


    //NO ANDA
    @Test
    void getUser_OK() {
        initialization("1", "Admin");
        List<UserResponseDto> response = service.getUser("enc");

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());

        Mockito.verify(repository).getUserByUser("enc");
    }

    @Test
    void getUser_Exception() {
        initialization("1", "Admin");
        assertThrows(RuntimeException.class, () -> service.getUser("exception"));
        Mockito.verify(repository).getUserByUser("exception");
    }

    @Test
    void getUser_UserNotFoundException() {
        initialization("1", "Admin");
        assertThrows(UserNotFoundException.class, () -> service.getUser("no-enc"));
    }


    @Test
    void register_OK() {
        initialization("1", "Admin");
        UserCreateDto newUser = new UserCreateDto();
        newUser.name = "Juan";
        newUser.lastName = "Diaz";
        newUser.birthDate = new Date(1990 - 05 - 22);
        newUser.email = "juand@gmail.com";
        newUser.username = "username";
        newUser.password = "123456789";

        assertAll(() -> service.register(newUser));
    }


    @Test
    void register_DuplicatedException() {
        initialization("1", "Admin");
        UserCreateDto usuario = new UserCreateDto();
        usuario.username = "duplicado";

        assertThrows(DuplicatedUserException.class, () -> service.register(usuario));
    }


    @Test
    void register_NotMinimalRequisitePasswordException() {
        initialization("1", "Admin");
        UserCreateDto user = new UserCreateDto();
        user.name = "Juan";
        user.lastName = "Diaz";
        user.birthDate = new Date(1990 - 05 - 22);
        user.email = "juand@gmail.com";
        user.username = "username";
        user.password = "132456";

        assertThrows(NotMinimalRequisitePasswordException.class,
                () ->service.register(user));
    }

    @Test
    void register_Exception() {
        initialization("1", "Admin");
        UserCreateDto user = new UserCreateDto();
        user.username = "exception";

        assertThrows(RuntimeException.class, () -> service.register(user));

    }

    @Test
    void editProfile_OK() {
        initialization("1", "Admin");
        UserEditDto userEdit = new UserEditDto();

        assertAll(() -> {
            service.editProfile(userEdit, 1L);
        });
    }

    @Test
    void editProfile_UserNotFoundExc() {
        initialization("1", "Admin");
        UserEditDto userEdit = new UserEditDto();
        assertThrows(UserNotFoundException.class,
                () -> service.editProfile(userEdit, 2L));
    }

    @Test
    void editProfile_Exception() {
        initialization("1", "Admin");
        UserEditDto userEdit = new UserEditDto();
        assertThrows(RuntimeException.class,
                () -> service.editProfile(userEdit, 10L));
    }

    @Test
    void editPass_OK() {
        initialization("1", "Admin");
        UserEditPassDto passEdit = new UserEditPassDto();
        passEdit.password = "00000000";

        assertAll(() -> {
            service.editPass(1L, passEdit.password);
        });
    }

    @Test
    void editPass_UserNotFoundException() {
        initialization("1", "Admin");
        UserEditPassDto passEdit = new UserEditPassDto();
        passEdit.password = "00000000";

        assertThrows(UserNotFoundException.class, () -> {
            service.editPass(2L, passEdit.password);
        });
    }

    @Test
    void editPass_NotMinimalRequisitePasswordException() {
        initialization("1", "Admin");
        UserEditPassDto passEdit = new UserEditPassDto();
        passEdit.password = "0000000";

        assertThrows(NotMinimalRequisitePasswordException.class, () -> {
            service.editPass(1L, passEdit.password);
        });
    }

    @Test
    void editPass_Exception() {
        initialization("1", "Admin");
        UserEditPassDto passEdit = new UserEditPassDto();
        passEdit.password = "00000000";

        assertThrows(RuntimeException.class, ()->service.editPass(10L, passEdit.password));
    }

    @Test
    void delete_OK() {
        initialization("1", "Admin");
        assertAll(() ->service.delete(1L));
    }

    @Test
    void delete_UserNotFoundException() {
        initialization("1", "Admin");
        assertThrows(UserNotFoundException.class, () -> service.delete(2L));
    }

    @Test
    void delete_Exception() {
        initialization("1", "Admin");
        assertThrows(RuntimeException.class, ()-> service.delete(10L));
    }

    @Test
    void deleteUser_OK() {
        initialization("1", "Admin");
        assertAll(() ->service.deleteUser(1L));
    }

    @Test
    void deleteUser_UserNotFoundException() {
        initialization("1", "Admin");
        assertThrows(UserNotFoundException.class, () -> service.deleteUser(2L));
    }

    @Test
    void deleteUser_Exception() {
        initialization("1", "Admin");
        assertThrows(RuntimeException.class, ()-> service.deleteUser(10L));
    }

    @Test
    void changeRol_OK(){
        initialization("1", "Admin");
        UserChangeRolDto dto = new UserChangeRolDto();
        dto.idUser=1L;
        dto.idRol=1L;
        assertAll(() ->service.changeRol(dto));
    }

    @Test
    void changeRol_NOTFOUND(){
        initialization("1", "Admin");
        UserChangeRolDto dto = new UserChangeRolDto();
        dto.idUser=2L;
        dto.idRol=1L;
        assertThrows(UserNotFoundException.class, () -> service.changeRol(dto));
    }

    @Test
    void changeRol_EXCEPTION(){
        initialization("1", "Admin");
        UserChangeRolDto dto = new UserChangeRolDto();
        dto.idUser=10L;
        dto.idRol=1L;
        assertThrows(RuntimeException.class, () -> service.changeRol(dto));
    }

}
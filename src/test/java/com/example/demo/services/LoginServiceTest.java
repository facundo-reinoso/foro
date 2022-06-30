package com.example.demo.services;

import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.WrongPasswordException;
import com.example.demo.models.UsersEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JWTUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class LoginServiceTest {
    private static UserRepository repository;
    private static LoginService service;
    private static UsersEntity user;
    private static JWTUtil jwtUtil;
    private static final String pass = "123456";
    private static final String name = "alguien";

    @BeforeAll
    static void setUpAll() {
        String key = "palabrasecreta";
        String issuer = "Main";
        long ttlMillis = 10800000;
        jwtUtil = new JWTUtil(key, issuer, ttlMillis);

        UsersEntity user = new UsersEntity();
        user.setIdUsuario(1L);
        user.setName(name);
        user.setPassword(pass);
        LoginServiceTest.user = spy(user);

        repository = mock(UserRepository.class);
        Mockito.when(repository.getUser("encontrado")).thenReturn(java.util.Optional.of(LoginServiceTest.user));
        Mockito.when(repository.getUser("no_encontrado")).thenThrow(new UserNotFoundException());
        Mockito.when(repository.getUser("exception")).thenThrow(new RuntimeException());

        service = new LoginService(repository, jwtUtil);
    }

//    @Test
//    void getUserByCredentials_NotFound() {
//        assertThrows(UserNotFoundException.class, () -> service.getUserByCredentials("no_encontrado", pass));
//    }
//    @Test
//    void getUserByCredentials_WrongPassword() {
//        assertThrows(WrongPasswordException.class, () -> service.getUserByCredentials("encontrado", "789456"));
//    }
//    @Test
//    void getUserByCredentials_RepositoryException() {
//        assertThrows(RuntimeException.class, () -> service.getUserByCredentials("exception", "789456"));
//    }
//    @Test
//    void getUserByCredentials_Found() {
//        Optional<UsersEntity> usersEntity = service.getUserByCredentials("encontrado", pass);
//        assertEquals(1L, usersEntity.get().getIdUsuario());
//        Mockito.verify(user).getIdUsuario();
//        Mockito.verify(user).getPassword();
//        Mockito.verify(repository).getUser("encontrado");
//    }
}
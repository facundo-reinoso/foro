package com.example.demo.services;

import com.example.demo.dtos.LoginUserInfoDto;
import com.example.demo.dtos.UserLoginDto;
import com.example.demo.exceptions.RepositoryException;
import com.example.demo.exceptions.TokenException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.WrongPasswordException;
import com.example.demo.models.UsersEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class LoginService {
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);
    private UserRepository userRepo;
    private JWTUtil jwtutil;


    @Autowired
    public LoginService(UserRepository userRepo, JWTUtil jwt) {
        this.jwtutil = jwt;
        this.userRepo = userRepo;
    }


    public Optional<UsersEntity> getUserByCredentials(UserLoginDto loginDto) {
        log.debug("Login Service: Get user id by credentials");
        try {
            Optional<UsersEntity> usersEntity = userRepo.getUser(loginDto.username);

            if (usersEntity.isEmpty()) {
                throw new UserNotFoundException();
            }

            if (!loginDto.password.equals(usersEntity.get().getPassword())) {
                throw new WrongPasswordException();
            }


            return usersEntity;

        } catch (UserNotFoundException e) {
            log.error("Login Service: User not found (getIdByCredentials)");
            throw new UserNotFoundException();
        } catch (WrongPasswordException e) {
            log.error("Login Service: Wrong password (getIdByCredentials)");
            throw new WrongPasswordException();
        } catch (RuntimeException e) {
            log.error("Login Service: Error from repository (getIdByCredentials)");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public Optional<LoginUserInfoDto> login(UserLoginDto loginDto, Long id, String role) {
        try{
            String tokenJwt = jwtutil.create(String.valueOf(id), loginDto.username, role);

            if(tokenJwt.isEmpty()){
                throw new TokenException();
            }

            LoginUserInfoDto loginUserInfoDto = new LoginUserInfoDto();
            loginUserInfoDto.setId(id);
            loginUserInfoDto.setUsername(loginDto.username);
            loginUserInfoDto.setToken(tokenJwt);
            loginUserInfoDto.setType("Bearer");

            return Optional.of(loginUserInfoDto);

        } catch (TokenException e) {
            log.error("Login Service: Token error (create)");
            throw new TokenException();

        } catch (RuntimeException e) {
            log.error("Login Service: Error from repository (getIdByCredentials)");
            throw new RepositoryException(e.getMessage(), e);
        }
    }
}

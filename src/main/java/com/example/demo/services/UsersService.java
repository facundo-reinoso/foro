package com.example.demo.services;

import com.example.demo.context.RequestContext;
import com.example.demo.dtos.*;
import com.example.demo.exceptions.*;
import com.example.demo.models.RolesEntity;
import com.example.demo.models.UsersEntity;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsersService {

    private static final Logger log = LoggerFactory.getLogger(UsersService.class);
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RequestContext context;

    @Autowired
    public UsersService(UserRepository userRepo, RequestContext context) {
        this.userRepo = userRepo;
        this.context = context;
    }

    public void register(UserCreateDto dto) {
        log.debug("Users Service: register a new user");
        try{
            Optional<UsersEntity> existingUser = userRepo.getUser(dto.username);

            if (existingUser.isPresent()) {
                throw new DuplicatedUserException();
            }

            UsersEntity newUser = new UsersEntity();

            newUser.setName(dto.name.trim());
            newUser.setLastName(dto.lastName.trim());
            newUser.setEmail(dto.email.trim());
            newUser.setBirthDate(dto.birthDate);
            newUser.setUsername(dto.username.trim().replace(" ", ""));
            newUser.setStatus(1);
            newUser.setIdRole(new RolesEntity(2L));

            if (checkPassword(dto.password)) {
                throw new NotMinimalRequisitePasswordException();
            }

            newUser.setPassword(dto.password);
            userRepo.save(newUser);

        }catch(DuplicatedUserException e){
            log.error("Users Service: User not found");
            throw new DuplicatedUserException();

        } catch(NotMinimalRequisitePasswordException e){
            log.error("Users Service: Not minimal requisite password");
            throw new NotMinimalRequisitePasswordException();

        }catch(RuntimeException e){
            log.error("Users Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public void changeRol(UserChangeRolDto userChangeRolDto){
        log.debug("Users Service: Change rol");
        try{
            Optional<UsersEntity> usersEntity = userRepo.getUserById(userChangeRolDto.idUser);

            if (usersEntity.isEmpty()) {
                throw new UserNotFoundException();
            }

            UsersEntity user = usersEntity.get();
            user.setIdRole(new RolesEntity(userChangeRolDto.idRol));
            userRepo.save(user);

        } catch(UserNotFoundException e){
            log.error("Users Service: User not found");
            throw new UserNotFoundException();

        } catch(RuntimeException e){
            log.error("Users Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public List<UserResponseDto> getUser(String user) {
        log.debug("Users Service: Find a user by username");
        try {
            List<UsersEntity> users = userRepo.getUserByUser(user);

            if (users.isEmpty()) {
                throw new UserNotFoundException();
            }

            List<UserResponseDto> listUsers = new ArrayList();

            for (UsersEntity usersEntity : users) {
                UserResponseDto userResponseDto = new UserResponseDto();
                userResponseDto.id = usersEntity.getIdUsuario();
                userResponseDto.name = usersEntity.getName();
                userResponseDto.lastName = usersEntity.getLastName();
                userResponseDto.email = usersEntity.getEmail();
                userResponseDto.birthDate = usersEntity.getBirthDate();
                userResponseDto.username = usersEntity.getUsername();
                userResponseDto.status = usersEntity.getStatus();
                userResponseDto.roleName = "";

                if (context.hasRole("Admin")) {
                    userResponseDto.roleName = usersEntity.getIdRole().getName();
                }

                listUsers.add(userResponseDto);
            }

            return listUsers;

        } catch(UserNotFoundException e){
            log.error("Users Service: User not found");
            throw new UserNotFoundException();

        } catch(RuntimeException e){
            log.error("Users Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }


    public void editProfile(UserEditDto dto, Long id) {
        log.debug("Users Service: Edit profile of user");
        try{
            Optional<UsersEntity> userById = userRepo.getUserById(id);

            if (userById.isEmpty()) {
                throw new UserNotFoundException();
            }

            UsersEntity profile = userById.get();
            profile.setName(dto.name);
            profile.setLastName(dto.lastName);
            profile.setBirthDate(dto.birthDate);
            profile.setEmail(dto.email);

            userRepo.save(profile);

        }catch(UserNotFoundException e){
            log.error("Users Service: User not found");
            throw new UserNotFoundException();

        }catch(RuntimeException e){
            log.error("Users Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }

    }

    public void editPass(Long id, String newPass) {
        log.debug("Users Service: Edit password of user with id: "+ id);
        try{
            Optional<UsersEntity> userById = userRepo.getUserById(id);

            if (userById.isEmpty()) {
                throw new UserNotFoundException();
            }

            if (checkPassword(newPass)) {
                throw new NotMinimalRequisitePasswordException();
            }

            UsersEntity user = userById.get();
            user.setPassword(newPass);
            userRepo.save(user);

        }catch(UserNotFoundException e){
            log.error("Users Service: User not found");
            throw new UserNotFoundException();

        }catch(NotMinimalRequisitePasswordException e){
            log.error("Users Service: Not minimal requisite password");
            throw new NotMinimalRequisitePasswordException();

        } catch(RuntimeException e){
            log.error("Users Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        log.debug("Users Service: Delete own user with id: "+ id);
        try{
            Optional<UsersEntity> userById = userRepo.getUserById(id);

            if (userById.isEmpty()) {
                throw new UserNotFoundException();
            }

            UsersEntity user = userById.get();
            user.setStatus(0);
            userRepo.save(user);

        }catch(UserNotFoundException e){
            log.error("Users Service: User not found");
            throw new UserNotFoundException();
        }catch(RuntimeException e){
            log.error("Users Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public void deleteUser(Long id) {
        log.debug("Users Service: Delete user with id: "+ id);
        try{
            Optional<UsersEntity> userById = userRepo.getUserById(id);

            if (userById.isEmpty()) {
                throw new UserNotFoundException();
            }

            UsersEntity user = userById.get();
            user.setStatus(0);
            userRepo.save(user);

        }catch(UserNotFoundException e){
            log.error("Users Service: User not found");
            throw new UserNotFoundException();
        }catch(RuntimeException e){
            log.error("Users Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    //METODOS PERSONALIZADOS
    public boolean checkPassword(String pass) {
        return pass.length() < 8;
    }
}

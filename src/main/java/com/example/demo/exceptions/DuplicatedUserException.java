package com.example.demo.exceptions;

public class DuplicatedUserException extends RuntimeException{
    public DuplicatedUserException(){
        super("El usuario ya se encuentra registrado");
    }
}

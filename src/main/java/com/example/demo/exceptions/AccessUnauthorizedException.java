package com.example.demo.exceptions;

public class AccessUnauthorizedException extends RuntimeException{
    public AccessUnauthorizedException(){
        super("Acceso no autorizado");
    }
}

package com.example.demo.exceptions;

public class TokenException extends RuntimeException{
    public TokenException(){
        super("Error al crear Token");
    }
}

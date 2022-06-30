package com.example.demo.exceptions;

public class NotMinimalRequisitePasswordException extends RuntimeException{
    public NotMinimalRequisitePasswordException(){
        super("El password no cumple los minimos requisitos. Debe tener mínimo 8 dígitos");
    }
}

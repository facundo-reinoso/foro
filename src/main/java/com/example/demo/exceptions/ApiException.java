package com.example.demo.exceptions;

public class ApiException extends RuntimeException {

    protected final int code;

    public ApiException(int code, String mensaje){
        super(mensaje);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

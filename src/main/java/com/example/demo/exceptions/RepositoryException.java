package com.example.demo.exceptions;

public class RepositoryException extends RuntimeException{
    public RepositoryException(String message, Throwable cause){
        super(message, cause);
    }
}

package com.example.demo.exceptions;

public class CommentNotFoundException extends ApiException{
    public CommentNotFoundException(){
        super(404, "The comment/comments doesn´t exist");
    }
}

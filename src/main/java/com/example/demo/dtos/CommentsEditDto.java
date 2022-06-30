package com.example.demo.dtos;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class CommentsEditDto {

    @NotEmpty(message = "The comment cannot be empty")
    @Size(min=1, max=150, message = "message must between 1 and 150 characters")
    public String message;

}

package com.example.demo.dtos;

import java.time.LocalDateTime;

public class CommentsResponseDto {

    public Long commentId;
    public Long parentId;
    public Long userId;
    public String message;
    public LocalDateTime creationDate;
    public Long status;
}

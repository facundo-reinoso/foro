package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LoginUserInfoDto {
    String token;
    String username;
    Long id;
    String type;
}

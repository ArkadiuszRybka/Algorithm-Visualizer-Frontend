package com.example.algorithmvisualizerfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterDto {
    private String name;
    private String email;
    private String password;
}

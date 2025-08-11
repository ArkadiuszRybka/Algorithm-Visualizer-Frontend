package com.example.algorithmvisualizerfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmDto {
    private Long id;
    private String name;
    private String category;
    private String description;

}

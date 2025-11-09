package com.dragun.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provinces")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Province {
    
    @Id
    @Column(name = "code", length = 10)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    private String slug;
    
    private String type;
    
    @Column(name = "name_with_type")
    @JsonProperty("name_with_type")
    private String nameWithType;
}



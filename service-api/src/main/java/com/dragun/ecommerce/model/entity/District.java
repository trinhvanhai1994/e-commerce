package com.dragun.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "districts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class District {
    
    @Id
    @Column(name = "code", length = 10)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    private String type;
    
    private String slug;
    
    @Column(name = "name_with_type")
    @JsonProperty("name_with_type")
    private String nameWithType;
    
    private String path;
    
    @Column(name = "path_with_type")
    @JsonProperty("path_with_type")
    private String pathWithType;
    
    @Column(name = "parent_code", length = 10, nullable = false)
    @JsonProperty("parent_code")
    private String parentCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_code", insertable = false, updatable = false)
    @JsonIgnore
    private Province province;
}



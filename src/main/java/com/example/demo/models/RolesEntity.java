package com.example.demo.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class RolesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "idRole")
    private List<UsersEntity> user_id;


    //Contructores
    public RolesEntity() {

    }

    public RolesEntity(String name) {
        this.name = name;
    }

    public RolesEntity(Long id) {
        this.id = id;
    }

    public RolesEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

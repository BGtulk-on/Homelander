package com.uktc.schoolInventory.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "equipment")
@Data
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String serialNumber;
    private String photoUrl;

    private boolean isAssigned = false;
    private String assignedTo;


}
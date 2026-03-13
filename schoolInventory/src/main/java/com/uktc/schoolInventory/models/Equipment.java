package com.uktc.schoolInventory.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "equipment")
@Data // Това автоматично прави Getter-и и Setter-и (ако имаш Lombok)
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String serialNumber;

    // Полетата за зачисляване, които поиска
    private boolean isAssigned = false;
    private String assignedTo; // Тук ще пазим името на човека или ID
}
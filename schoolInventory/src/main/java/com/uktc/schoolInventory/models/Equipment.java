package com.uktc.schoolInventory.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "equipment")
@Data
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Връзка към таблица equipment_types
    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "equipment_status")
    private EquipmentStatus status = EquipmentStatus.Available;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_condition", columnDefinition = "equipment_condition")
    private EquipmentCondition currentCondition;

    // Връзка към таблица locations
    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "is_assigned")
    private boolean isAssigned = false;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
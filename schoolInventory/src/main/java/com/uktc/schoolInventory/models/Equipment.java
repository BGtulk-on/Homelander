package com.uktc.schoolInventory.models;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uktc.schoolInventory.config.EquipmentConditionConverter;
import com.uktc.schoolInventory.config.EquipmentStatusConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private EquipmentType type;

    @Column(name = "serial_number", unique = true, nullable = false)
    private String serialNumber;

    @Convert(converter = EquipmentStatusConverter.class)
    @Column(name = "status")
    private EquipmentStatus status = EquipmentStatus.Available;

    @Convert(converter = EquipmentConditionConverter.class)
    @Column(name = "current_condition")
    private EquipmentCondition currentCondition;

    // Връзка към таблица locations
    @Column(name = "location_id")
    private Integer locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Location location;

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
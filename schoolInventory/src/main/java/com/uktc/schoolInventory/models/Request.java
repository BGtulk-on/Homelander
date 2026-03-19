package com.uktc.schoolInventory.models;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "requests")
@Data
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatusType requestStatus;

    @Column(name = "requested_start_date")
    private OffsetDateTime requestedStartDate;

    @Column(name = "requested_end_date")
    private OffsetDateTime requestedEndDate;

    @Column(name = "approved_by_admin_id")
    private Long approvedByAdminId;

    @Column(name = "actual_return_date")
    private OffsetDateTime actualReturnDate;

    @Column(name = "return_condition")
    private EquipmentCondition returnCondition;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}

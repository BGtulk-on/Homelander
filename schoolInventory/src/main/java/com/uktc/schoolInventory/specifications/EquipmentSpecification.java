package com.uktc.schoolInventory.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.uktc.schoolInventory.config.EquipmentConditionConverter;
import com.uktc.schoolInventory.config.EquipmentStatusConverter;
import com.uktc.schoolInventory.models.Equipment;
import com.uktc.schoolInventory.models.EquipmentCondition;
import com.uktc.schoolInventory.models.EquipmentStatus;
import com.uktc.schoolInventory.models.EquipmentType;
import com.uktc.schoolInventory.models.Location;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class EquipmentSpecification {

    public static Specification<Equipment> hasNameLike(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Equipment> hasQuery(String q) {
        return (root, query, cb) -> {
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("serialNumber")), pattern)
            );
        };
    }

    public static Specification<Equipment> hasSerialNumberLike(String serialNumber) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("serialNumber")), "%" + serialNumber.toLowerCase() + "%");
    }

    public static Specification<Equipment> hasCondition(EquipmentCondition condition) {
        String dbValue = new EquipmentConditionConverter().convertToDatabaseColumn(condition);
        return (root, query, cb) ->
                cb.equal(root.get("currentCondition").as(String.class), dbValue);
    }

    public static Specification<Equipment> hasStatus(EquipmentStatus status) {
        String dbValue = new EquipmentStatusConverter().convertToDatabaseColumn(status);
        return (root, query, cb) ->
                cb.equal(root.get("status").as(String.class), dbValue);
    }

    public static Specification<Equipment> hasTypeName(String typeName) {
        return (root, query, cb) -> {
            Join<Equipment, EquipmentType> typeJoin = root.join("type", JoinType.LEFT);
            return cb.equal(cb.lower(typeJoin.get("typeName")), typeName.toLowerCase());
        };
    }

    public static Specification<Equipment> hasLocationName(String locationName) {
        return (root, query, cb) -> {
            Join<Equipment, Location> locationJoin = root.join("location", JoinType.LEFT);
            return cb.equal(cb.lower(locationJoin.get("roomName")), locationName.toLowerCase());
        };
    }
}
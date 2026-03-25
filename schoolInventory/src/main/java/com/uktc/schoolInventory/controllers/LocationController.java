package com.uktc.schoolInventory.controllers;

import com.uktc.schoolInventory.models.Location;
import com.uktc.schoolInventory.repositories.LocationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('ADMIN')")
    public Location createLocation(@RequestBody Location location) {
        return locationRepository.save(location);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('ADMIN')")
    public Location updateLocation(@PathVariable Integer id, @RequestBody Location locationDetails) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        location.setRoomName(locationDetails.getRoomName());
        return locationRepository.save(location);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('ADMIN')")
    public void deleteLocation(@PathVariable Integer id) {
        locationRepository.deleteById(id);
    }
}

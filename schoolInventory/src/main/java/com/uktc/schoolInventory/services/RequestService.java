package com.uktc.schoolInventory.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.uktc.schoolInventory.exception.ResourceNotFoundException;
import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.repositories.EquipmentRepository;
import com.uktc.schoolInventory.repositories.RequestRepository;
import com.uktc.schoolInventory.repositories.UserRepository;

@Service
public class RequestService {
    private static final Logger log = LoggerFactory.getLogger(RequestService.class);
    private final RequestRepository repository;
    private final EmailService emailService;
    private final UserRepository userRepository; // Нова зависимост
    private final EquipmentRepository equipmentRepository; // Нова зависимост

    // Обнови конструктора, за да включва новите репозиторита
    public RequestService(RequestRepository repository,
                          EmailService emailService,
                          UserRepository userRepository,
                          EquipmentRepository equipmentRepository) {
        this.repository = repository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public Request createRequest(Request request) {
        // 1. Изваждаме истинския потребител от базата по ID-то от JSON-а
        var user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUser().getId()));

        // 2. Fetch the equipment from the database
        var equipment = equipmentRepository.findById(request.getEquipment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + request.getEquipment().getId()));
        log.debug("Saving request for equipment: {}", equipment.getName());

        // 3. Свързваме ги с текущата заявка
        request.setUser(user);
        request.setEquipment(equipment);
        request.setRequestStatus(RequestStatusType.PENDING);

        // 4. Записваме заявката в базата
        Request saved = repository.save(request);

        // 5. Пращаме имейл (в try-catch, за да не гърми, ако Mailtrap се забави)
        try {
            emailService.sendRentalConfirmation(
                    user.getEmail(),
                    equipment.getName()
            );
            log.info("Email sent successfully");
        } catch (Exception e) {
            log.warn("Failed to send email: {}", e.getMessage());
        }

        return saved;
    }
}
package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.repositories.RequestRepository;
import com.uktc.schoolInventory.repositories.UserRepository; // Добави този импорт
import com.uktc.schoolInventory.repositories.EquipmentRepository; // Добави този импорт
import org.springframework.stereotype.Service;

@Service
public class RequestService {
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
                .orElseThrow(() -> new RuntimeException("Потребителят не е намерен!"));

        // 2. Изваждаме истинската техника от базата
        var equipment = equipmentRepository.findById(request.getEquipment().getId())
                .orElseThrow(() -> new RuntimeException("Техниката не е намерена!"));
        System.out.println("DEBUG: Опитвам се да запиша заявка за техника: " + equipment.getName());

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
            System.out.println("✅ Имейлът е изпратен към Mailtrap успешно!");
        } catch (Exception e) {
            System.err.println("❌ Грешка при пращане на имейл: " + e.getMessage());
        }

        return saved;
    }
}
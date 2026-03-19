package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.repositories.RequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OverdueTaskService {

    private final RequestRepository repository;
    private final EmailService emailService;

    public OverdueTaskService(RequestRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;

    }

    // cron = "0 0 9 * * *" означава: Всеки ден точно в 09:00 сутринта
    @Scheduled(cron = "0 0 9 * * *")
    public void sendRemindersToOverdueUsers() {
        OffsetDateTime today = OffsetDateTime.now();

        // Вземаме всички, които са одобрени (т.е. техниката е у тях)
        List<Request> activeRequests = repository.findAllByRequestStatus(RequestStatusType.APPROVED);

        for (Request req : activeRequests) {
            // Проверка: Ако днешната дата е СЛЕД крайната дата за връщане
            if (today.isAfter(req.getRequestedEndDate())) {

                try {
                    // Изпращаме имейла
                    emailService.sendOverdueReminder(
                            req.getUser().getEmail(),
                            req.getEquipment().getName()
                    );

                    // Лог в конзолата, за да знаеш, че системата е свършила работа
                    System.out.println("✅ Изпратено напомняне до: " + req.getUser().getEmail());
                } catch (Exception e) {
                    // Ако нещо се прецака с имейла, да не спира целия цикъл
                    System.err.println("❌ Грешка при пращане до " + req.getUser().getEmail() + ": " + e.getMessage());
                }
            }
        }
    }
}
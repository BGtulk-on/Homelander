package com.uktc.schoolInventory.services;

import com.uktc.schoolInventory.models.Request;
import com.uktc.schoolInventory.models.RequestStatusType;
import com.uktc.schoolInventory.repositories.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OverdueTaskService {
    private static final Logger log = LoggerFactory.getLogger(OverdueTaskService.class);

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
                    log.info("Overdue reminder sent to: {}", req.getUser().getEmail());
                } catch (Exception e) {
                    log.warn("Failed to send overdue reminder to {}: {}", req.getUser().getEmail(), e.getMessage());
                }
            }
        }
    }
}
package com.uktc.schoolInventory.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Вземаме имейла автоматично от application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRentalConfirmation(String toEmail, String equipmentName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // ЗАДЪЛЖИТЕЛНО за Hostinger
        message.setTo(toEmail);
        message.setSubject("Ново наемане - School Inventory");
        message.setText("Здравейте, успешно наехте: " + equipmentName);

        mailSender.send(message);
        System.out.println("✅ Потвърждението е изпратено до: " + toEmail);
    }

    public void sendOverdueReminder(String toEmail, String equipmentName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // ЗАДЪЛЖИТЕЛНО за Hostinger
        message.setTo(toEmail);
        message.setSubject("⚠️ ПРОСРОЧЕНА ТЕХНИКА");
        message.setText("Здравейте, срокът за връщане на " + equipmentName + " е изтекъл! Моля, върнете я възможно най-скоро.");

        mailSender.send(message);
        System.out.println("🚨 Напомнянето за просрочие е изпратено до: " + toEmail);
    }
}
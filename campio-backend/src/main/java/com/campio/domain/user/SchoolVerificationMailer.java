package com.campio.domain.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "campio.school-verification.mail-enabled", havingValue = "true")
public class SchoolVerificationMailer {

  private final JavaMailSender mailSender;
  private final String from;

  public SchoolVerificationMailer(
      JavaMailSender mailSender,
      @Value("${campio.school-verification.mail-from}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  public void send(String email, String code) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(email);
    message.setSubject("Campio school email verification");
    message.setText("Your Campio verification code is " + code + ". It expires in 10 minutes.");
    mailSender.send(message);
  }
}

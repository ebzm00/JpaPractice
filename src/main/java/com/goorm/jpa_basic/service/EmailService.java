package com.goorm.jpa_basic.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    //이메일 인증번호 발송
    public String sendVerificationEmail(String email) throws MessagingException {
        String verificationCode = generateVerificationCode();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("이메일 인증번호");
        helper.setText("인증번호: " + verificationCode);

        mailSender.send(message);

        return verificationCode;

    }

    //인증 번호 생성(예: 6자리 숫자)
    private String generateVerificationCode() {
        int code =  (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }
}

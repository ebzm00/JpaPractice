package com.goorm.jpa_basic.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    //✅ 이메일 전송 공통 메서드 (모든 이메일 전송에 사용)
    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);; // HTML 형식

        mailSender.send(message);
    }

    //이메일 인증번호 발송
    public String sendVerificationEmail(String email) throws MessagingException {
        String verificationCode = generateVerificationCode();
        String subject = "이메일 인증번호";
        String content = "<html><body>"
                + "<h2>회원가입을 위한 이메일 인증번호</h2>"
                + "<p>아래 인증번호를 입력하여 회원가입을 완료하세요:</p>"
                + "<h3>" + verificationCode + "</h3>"
                + "<p>감사합니다!</p>"
                + "</body></html>";

        sendEmail(email,subject,content); // 공통 메서드 활용
        return verificationCode;

    }

    //✅ 임시 비밀번호 발송
    public void sendTemporaryPasswordEmail(String email, String tempPassword) throws  MessagingException {
        String subject = "임시 비밀번호 발급 안내";
        String content = "<html><body>"
                +"<h2>임시 비밀번호 안내</h2>"
                +"<p>귀하의 임시 비밀번호는 다음과 같습니다:</p>"
                +"<h3>" + tempPassword + "</h3>"
                +"<p>로그인 후 반드시 새 비밀번호로 변경하세요.</p>"
                +"</body></html>";

        sendEmail(email,subject,content); //기존 이메일 전송 메서드 활용
    }

    //인증 번호 생성(예: 6자리 숫자)
    private String generateVerificationCode() {
        int code =  (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    //✅ 임시 비밀번호 생성 메서드
    public String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0,8); //8자리 난수 생성
    }
}

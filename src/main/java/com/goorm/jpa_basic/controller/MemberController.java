package com.goorm.jpa_basic.controller;

import com.goorm.jpa_basic.dto.LoginRequest;
import com.goorm.jpa_basic.model.Member;
import com.goorm.jpa_basic.service.EmailService;
import com.goorm.jpa_basic.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    private String verificationCode; // 이메일 인증번호 저장 변수

    Logger logger = LoggerFactory.getLogger(MemberController.class);

    //모든 멤버 조회
    @GetMapping("/members")
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    // 특정 이메일로 멤버 조회
    @GetMapping("/members/{email}")
    public ResponseEntity<Member> getMemberByEmail(@PathVariable String email) {
        Optional<Member> member = memberService.getUserByEmail(email);
        return member.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Member>> getMembersByEmail(@RequestParam String email) {
        List<Member> members = memberService.getUsersByEmailLike(email);
        return ResponseEntity.ok(members);
    }

    //회원가입 시 이메일 인증번호 발송
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Member member, HttpSession session) throws MessagingException {
        // 이메일 인증번호 발송
        verificationCode = emailService.sendVerificationEmail(member.getEmail());
        session.setAttribute("member",member);

        return ResponseEntity.status(HttpStatus.CREATED).body("이메일 인증번호를 발송했습니다.");
    }

    //이메일 인증번호 확인
    @PostMapping("/activate")
    public ResponseEntity<String> verifyEmail(@RequestParam String inputCode, HttpSession session) {
        //세션에서 저장한 member 객체 가져오기
        Member member = (Member) session.getAttribute("member");
        String sessionVerificationCode = (String) session.getAttribute("verificationCode");
        LocalDateTime verificationTime = (LocalDateTime) session.getAttribute("verificationTime");

        if (member == null) {
            logger.error("세션에 저장된 회원 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원 정보가 없습니다.");
        }

        //인증번호와 시간이 없다면 인증번호 없는걸로  메시지 반환
        if (sessionVerificationCode == null || verificationTime == null) {
            logger.error("세션에 인증번호 또는 인증 시간 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 없습니다.");
        }

        //인증번호 만료 시간 확인 (예: 5분 이내)
        if (verificationTime.plusMinutes(5).isBefore(LocalDateTime.now())) {
            session.removeAttribute("verificationCode");
            session.removeAttribute("verificationTime");
            logger.warn("인증번호가 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 만료되었습니다.");
        }

        //입력된 인증번호 검증
        if (!inputCode.equals(sessionVerificationCode)) {
            logger.warn("잘못된 인증번호 입력: {}", inputCode);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 인증번호입니다.");
        }

        try {
            //이메일 중복확인
            Optional<Member> existingMember = memberService.getUserByEmail(member.getEmail());
            if (existingMember.isPresent()) {
                logger.info("이미 가입된 이메일: {}", member.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 가입된 이메일입니다.");
            }

            //회원 저장
            memberService.saveMember(member);
            logger.info("회원가입  성공: {}", member.getEmail());
            //세션에서 인증 정보 제거
            session.removeAttribute("member");
            session.removeAttribute("verificationCode");
            session.removeAttribute("verificationTime");

            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (Exception e){
            logger.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("서버 오류로 인해 회원가입에 실패했습니다.");
        }
    }

    //로그인 기능
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        boolean isSuccess = memberService.login(loginRequest.getEmail(), loginRequest.getPassword(), session);

        if (isSuccess) {
            return ResponseEntity.ok("로그인 성공!");
        } else {
            return ResponseEntity.status(401).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    //로그아웃 (세션 삭제)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        memberService.logout(session);
        return ResponseEntity.ok("로그아웃 성공!");
    }
}



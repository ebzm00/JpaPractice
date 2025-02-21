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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final Logger logger = LoggerFactory.getLogger(MemberController.class);

    // 🔹 인증번호 저장 변수 제거 (세션 사용하도록 개선)

    // ✅ 모든 멤버 조회
    @GetMapping("/members")
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    // ✅ 특정 이메일로 멤버 조회
    @GetMapping("/members/{email}")
    public ResponseEntity<Member> getMemberByEmail(@PathVariable String email) {
        Optional<Member> member = memberService.getUserByEmail(email);
        return member.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //✅ 이메일 검색(Like)
    @GetMapping("/search")
    public ResponseEntity<List<Member>> getMembersByEmail(@RequestParam String email) {
        return ResponseEntity.ok(memberService.getUsersByEmailLike(email));
    }

    //@GetMapping으로 회원가입 페이지로 이동
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // 회원가입 폼을 작성한 register.html로 이동
    }

    //✅ 회원가입 시 이메일 인증번호 발송(기존 사용자 검사 추가)
    // 이메일 인증번호 발송 후 응답 수정
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Member member, HttpSession session) throws MessagingException {
        // 이미 가입된 이메일인지 확인
        if (memberService.getUserByEmail(member.getEmail()).isPresent()) {
            logger.warn("이미 가입된 이메일: {}", member.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "이미 가입된 이메일입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // 이메일 인증번호 발송
        String verificationCode = emailService.sendVerificationEmail(member.getEmail());

        // 세션에 인증번호와 인증 시간 저장
        session.setAttribute("verificationCode", verificationCode);
        session.setAttribute("verificationTime", LocalDateTime.now());
        session.setAttribute("member", member);

        logger.info("인증번호 발송 완료: {}", member.getEmail());

        // JSON 형태로 응답 반환
        Map<String, String> response = new HashMap<>();
        response.put("message", "이메일 인증번호를 발송했습니다.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ 이메일 인증번호 확인 및 회원가입 (회원 상태 "ACTIVE" 변경)
    @PostMapping("/activate")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> requestBody, HttpSession session) {
        // 🔹 세션에서 회원 정보 및 인증번호 가져오기
        String inputCode = requestBody.get("inputCode");
        Member member = (Member) session.getAttribute("member");
        String sessionVerificationCode = (String) session.getAttribute("verificationCode");
        LocalDateTime verificationTime = (LocalDateTime) session.getAttribute("verificationTime");

        Map<String, String> response = new HashMap<>();

        if (member == null || sessionVerificationCode == null || verificationTime == null) {
            logger.error("세션 데이터 없음 (회원 정보 또는 인증번호 누락");
            response.put("message", "인증 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 🔹 인증번호 만료 시간 확인 (5분 제한)
        if (verificationTime.plusMinutes(5).isBefore(LocalDateTime.now())) {
            session.invalidate();
            logger.warn("인증번호가 만료됨");
            response.put("message", "인증번호가 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 🔹 입력된 인증번호 검증
        if (!inputCode.equals(sessionVerificationCode)) {
            logger.warn("잘못된 인증번호 입력: {}", inputCode);
            response.put("message", "잘못된 인증번호입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 🔹 회원 정보 저장 & `status`를 "ACTIVE"로 설정
        try {
            member.setStatus("ACTIVE"); // 인증 완료 상태로 변경
            memberService.saveMember(member);
            logger.info("회원가입 완료: {}", member.getEmail());

            // 세션 정보 제거
            session.invalidate();
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("redirectUrl","/login.html"); //로그인 페이지 URL 생성
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생", e);
            response.put("message", "서버 오류로 인해 회원가입에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 로그인 페이지 랜더링 (GET 요청)
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; //login.html을 반환 (템플릿 엔진을 사용하는 경우)
    }

    // ✅ 로그인 (회원 상태 "ACTIVE"인지 체크)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Optional<Member> memberOpt = memberService.getUserByEmail(loginRequest.getEmail());

        // 🔹 회원 존재 여부 확인
        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        Member member = memberOpt.get();

        // 🔹 인증된 회원인지 확인
        if (!"ACTIVE".equals(member.getStatus())) {
            logger.warn("미인증 회원 로그인 시도: {}", member.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 인증이 완료되지 않았습니다.");
        }

        // 🔹 로그인 처리
        boolean isSuccess = memberService.login(loginRequest.getEmail(), loginRequest.getPassword(), session);
        if (isSuccess) {
            return ResponseEntity.ok("로그인 성공!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    // ✅ 로그아웃 (세션 삭제)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        memberService.logout(session);
        return ResponseEntity.ok("로그아웃 성공!");
    }
}



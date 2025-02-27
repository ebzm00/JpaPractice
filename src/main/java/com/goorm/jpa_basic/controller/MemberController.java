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
import java.util.*;


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
        // 🔴 필수 정보 누락 체크
        if (member.getMName() == null || member.getPhoneNumber() == null) {
            // 로그로 출력해서 확인
            logger.error("회원가입 실패: 이름 또는 전화번호 누락, mName: {}, phoneNumber: {}", member.getMName(),member.getPhoneNumber());
            Map<String, String> response = new HashMap<>();
            response.put("message", "이름과 전화번호는 필수 입력값입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

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

    // 비밀번호 찾기 페이지 랜더링 (GET 요청)
    @GetMapping("/find")
    public String findPage() {
        return "find"; //find.html을 반환 (템플릿 엔진을 사용하는 경우)
    }

    //✅ 비밀번호 찾기 (임시 비밀번호 발급 & 이메일 전송)
    @PostMapping("/find")
    public ResponseEntity<Map<String,String>> forgotPassword(@RequestBody Map<String, String> request) throws MessagingException {
        String email = request.get("email");
        Optional<Member> memberOpt = memberService.getUserByEmail(email);
        Map<String, String> response = new HashMap<>();

        if (memberOpt.isEmpty()) {
            response.put("message", "해당 이메일로 등록된 계정이 없습니다.");
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        //🔷 임시 비밀번호 생성
        String tempPassword = emailService.generateTemporaryPassword();

        //🔷 비밀번호 변경 & 저장
        memberService.updatePassword(email, tempPassword);

        //🔷 임시 비밀번호 이메일 발송
        try {
            emailService.sendTemporaryPasswordEmail(email,tempPassword);
            response.put("message", "임시 비밀번호가 이메일로 발송되었습니다.");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            response.put("message", "이메일 전송에 실패했습니다. 다시 시도해주세요.");
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //✅ 이메일 찾기 (이름과 전화번호로 이메일 조회)
    @PostMapping("/find-email")
    public ResponseEntity<Map<String,String>> findEmailBymNameAndPhoneNumber(@RequestBody Map<String,String> request) {
        String mName = request.get("mName");
        String phoneNumber = request.get("phoneNumber");

        Map<String, String> response = new HashMap<>();

        if (mName == null || mName.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            response.put("message","이름 과 전화번호를 정확히 입력해주세요.");
            response.put("status","fail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 회원 정보 조회
        Optional<Member> memberOpt = memberService.getUserBymNameAndPhoneNumber(mName,phoneNumber);

        if (memberOpt.isEmpty()) {
            response.put("message", "해당 이름과 전화번호로 등록된 이메일이 없습니다.");
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        //이메일 정보 반환 (보안상 이메일 일부 마스킹)
        Member member = memberOpt.get();
        String email = member.getEmail();
        String maskedEmail = maskEmail(email); //이메일 마스킹 처리

        response.put("email",maskedEmail);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    //이메일 마스킹 처리 메서드
    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) return email; // "ab@example.com" 같은 경우 마스킹 하지 않음
        String maskedPart = "*".repeat(atIndex - 2);
        return email.substring(0, 2) + maskedPart + email.substring(atIndex);
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



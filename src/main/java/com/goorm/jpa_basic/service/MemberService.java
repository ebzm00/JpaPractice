package com.goorm.jpa_basic.service;

import com.goorm.jpa_basic.model.Member;
import com.goorm.jpa_basic.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 모든 회원 조회
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    //이메일로 회원 조회
    public Optional<Member> getUserByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    //이메일 LIKE 검색
    public List<Member> getUsersByEmailLike(String email) {
        return memberRepository.findByEmailContaining(email);
    }

    // 회원 저장 (비밀번호 암호화 및 중복 이메일 검사 추가)
    public Member saveMember(Member member) {
        if(memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        member.setPassword(passwordEncoder.encode(member.getPassword())); //비밀번호 암호화
        return memberRepository.save(member);
    }

    //로그인(세션 저장 + 인증 상태 체크)
    public boolean login(String email, String password, HttpSession session) {
        Optional<Member> memberOpt = memberRepository.findByEmail(email);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            //비밀번호 검증
            if (!passwordEncoder.matches(password, member.getPassword())) {
                return false; //비밀번호 불일치
            }

            //인증 상태 확인(활성화된 회원만 로그인 가능)
            if(!"ACTIVE".equals(member.getStatus())) {
                throw new IllegalStateException("계정이 활성화되지 않았습니다.");
            }

            session.setAttribute("loggedInUser", member); // 세션에 유저 정보 저장
            return true;
        }
        return false;
    }

    //로그아웃 (세션 삭제)
    public void logout(HttpSession session) {
        session.invalidate(); //세션 무효화
    }

}

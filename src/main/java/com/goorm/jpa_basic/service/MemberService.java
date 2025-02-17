package com.goorm.jpa_basic.service;

import com.goorm.jpa_basic.model.Member;
import com.goorm.jpa_basic.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    //특정 이메일로 조회
    public Optional<Member> getUserByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    //이메일 Like절 사용
    public List<Member> getUsersByEmailLike(String email) {
        return memberRepository.findByEmailLike(email);
    }


    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    //로그인(세션 저장)
    public boolean login(String email, String password, HttpSession session) {
        Optional<Member> member = memberRepository.findByEmail(email);

        if (member.isPresent() && member.get().getPassword().equals(password)) {
            session.setAttribute("loggedInUser",member.get()); //세션에 유저 정보 저장
            return true;
        }
        return false;
    }

    //로그아웃 (세션 삭제)
    public void logout(HttpSession session) {
        session.invalidate(); //세션 무효화
    }
}

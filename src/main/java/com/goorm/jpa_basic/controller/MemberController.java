package com.goorm.jpa_basic.controller;

import com.goorm.jpa_basic.model.Member;
import com.goorm.jpa_basic.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //모든 멤버 조회
    @GetMapping
    public ResponseEntity<List<Member>>  getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    // 특정 이메일로 멤버 조회
    @GetMapping("/{email}")
    public ResponseEntity<Member> getMemberByEmail(@PathVariable String email) {
        Optional<Member> member = memberService.getUserByEmail(email);
        return member.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody Member member) {
        return ResponseEntity.ok(memberService.saveMember(member));
    }

}



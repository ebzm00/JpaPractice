package com.goorm.jpa_basic.repository;

import com.goorm.jpa_basic.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findByEmail(String email);

    //이메일 Like 검색 (부분 일치 검색)
    List<Member> findByEmailContaining(String email);

}

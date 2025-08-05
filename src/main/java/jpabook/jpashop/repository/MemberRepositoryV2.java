package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepositoryV2 extends JpaRepository<Member, Long> {
	List<Member> findByName(String name);
}

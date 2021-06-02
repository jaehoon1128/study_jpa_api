package jh.jpaapi.repository;

import jh.jpaapi.doamin.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
JpaRepository<Entity, Entity PK>
기본적인 CRUD 기능이 모두 제공
*/
public interface MemberRepository extends JpaRepository<Member, Long> {

    //일반화 하기 어려운 기능도 메서드 이름으로 정확히 JPQL 쿼리를 실행(구현체 필요 X)
    //select m from Member m where m.name = ?
    List<Member> findByName(String name);
}

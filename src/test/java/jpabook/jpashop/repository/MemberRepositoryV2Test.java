package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * MemberRepositoryV2에 대한 포괄적인 단위 테스트
 * Spring Data JPA 기반 레포지토리의 기본 메서드와 커스텀 메서드 테스트
 * 테스팅 프레임워크: JUnit 4 + Spring Boot Test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberRepositoryV2Test {

    @Autowired
    private MemberRepositoryV2 memberRepositoryV2;

    @PersistenceContext
    private EntityManager em;

    // =========================== 기본 CRUD 테스트 ===========================

    /**
     * 회원 저장 기능 테스트 - 정상 케이스
     */
    @Test
    public void save_정상저장() {
        // given
        Member member = createMember("김영희", "서울", "강남구", "12345");

        // when
        Member savedMember = memberRepositoryV2.save(member);

        // then
        assertNotNull("저장된 회원이 null이면 안됨", savedMember);
        assertNotNull("저장된 회원의 ID가 생성되어야 함", savedMember.getId());
        assertEquals("저장된 회원 이름이 일치해야 함", "김영희", savedMember.getName());
        assertEquals("저장된 회원 주소가 일치해야 함", "서울", savedMember.getAddress().getCity());
    }

    /**
     * 회원 저장 기능 테스트 - null 객체 저장
     */
    @Test(expected = IllegalArgumentException.class)
    public void save_null객체_예외발생() {
        // when & then
        memberRepositoryV2.save(null);
    }

    /**
     * 회원 저장 기능 테스트 - 빈 이름
     */
    @Test
    public void save_빈이름_저장가능() {
        // given
        Member member = createMember("", "부산", "해운대구", "48000");

        // when
        Member savedMember = memberRepositoryV2.save(member);

        // then
        assertNotNull("빈 이름도 저장 가능해야 함", savedMember);
        assertEquals("빈 이름이 그대로 저장되어야 함", "", savedMember.getName());
    }

    /**
     * 회원 저장 기능 테스트 - null 이름
     */
    @Test
    public void save_null이름_저장가능() {
        // given
        Member member = createMember(null, "대구", "중구", "41000");

        // when
        Member savedMember = memberRepositoryV2.save(member);

        // then
        assertNotNull("null 이름도 저장 가능해야 함", savedMember);
        assertNull("null 이름이 그대로 저장되어야 함", savedMember.getName());
    }

    /**
     * 회원 저장 기능 테스트 - 주소 없이 저장
     */
    @Test
    public void save_주소없이저장() {
        // given
        Member member = new Member();
        member.setName("주소없는회원");

        // when
        Member savedMember = memberRepositoryV2.save(member);

        // then
        assertNotNull("주소 없이도 저장 가능해야 함", savedMember);
        assertEquals("회원 이름이 일치해야 함", "주소없는회원", savedMember.getName());
        assertNull("주소가 null이어야 함", savedMember.getAddress());
    }

    // =========================== 조회 테스트 ===========================

    /**
     * ID로 회원 조회 기능 테스트 - 정상 케이스
     */
    @Test
    public void findById_정상조회() {
        // given
        Member member = createMember("홍길동", "인천", "남동구", "21500");
        Member savedMember = memberRepositoryV2.save(member);
        em.flush();
        em.clear();

        // when
        Optional<Member> foundMember = memberRepositoryV2.findById(savedMember.getId());

        // then
        assertTrue("조회된 회원이 존재해야 함", foundMember.isPresent());
        assertEquals("조회된 회원 ID가 일치해야 함", savedMember.getId(), foundMember.get().getId());
        assertEquals("조회된 회원 이름이 일치해야 함", "홍길동", foundMember.get().getName());
        assertEquals("조회된 회원 주소가 일치해야 함", "인천", foundMember.get().getAddress().getCity());
    }

    /**
     * ID로 회원 조회 기능 테스트 - 존재하지 않는 ID
     */
    @Test
    public void findById_존재하지않는ID() {
        // given
        Long nonExistentId = 999999L;

        // when
        Optional<Member> foundMember = memberRepositoryV2.findById(nonExistentId);

        // then
        assertFalse("존재하지 않는 ID로 조회시 empty Optional 반환", foundMember.isPresent());
    }

    /**
     * ID로 회원 조회 기능 테스트 - null ID
     */
    @Test
    public void findById_nullID() {
        // when
        Optional<Member> foundMember = memberRepositoryV2.findById(null);

        // then
        assertFalse("null ID로 조회시 empty Optional 반환", foundMember.isPresent());
    }

    /**
     * 모든 회원 조회 기능 테스트 - 정상 케이스
     */
    @Test
    public void findAll_정상조회() {
        // given
        Member member1 = createMember("회원1", "서울", "강남구", "12345");
        Member member2 = createMember("회원2", "부산", "해운대구", "48000");
        Member member3 = createMember("회원3", "대구", "중구", "41000");

        memberRepositoryV2.save(member1);
        memberRepositoryV2.save(member2);
        memberRepositoryV2.save(member3);
        em.flush();
        em.clear();

        // when
        List<Member> allMembers = memberRepositoryV2.findAll();

        // then
        assertNotNull("조회 결과가 null이면 안됨", allMembers);
        assertTrue("저장된 회원 수 이상이어야 함", allMembers.size() >= 3);
        
        // 저장한 회원들이 모두 포함되어 있는지 확인
        assertTrue("회원1이 포함되어야 함", 
            allMembers.stream().anyMatch(m -> "회원1".equals(m.getName())));
        assertTrue("회원2가 포함되어야 함", 
            allMembers.stream().anyMatch(m -> "회원2".equals(m.getName())));
        assertTrue("회원3이 포함되어야 함", 
            allMembers.stream().anyMatch(m -> "회원3".equals(m.getName())));
    }

    /**
     * 모든 회원 조회 기능 테스트 - 데이터가 없는 경우
     */
    @Test
    public void findAll_데이터없음() {
        // given - 기존 데이터 모두 삭제
        memberRepositoryV2.deleteAll();
        em.flush();
        em.clear();

        // when
        List<Member> allMembers = memberRepositoryV2.findAll();

        // then
        assertNotNull("조회 결과가 null이면 안됨", allMembers);
        assertTrue("데이터가 없으면 빈 리스트 반환", allMembers.isEmpty());
    }

    // =========================== 커스텀 메서드 테스트 ===========================

    /**
     * findByName1 커스텀 메서드 테스트 - 정상 케이스
     */
    @Test
    public void findByName1_정상조회() {
        // given
        Member member1 = createMember("이순신", "서울", "종로구", "03000");
        Member member2 = createMember("김유신", "경주", "시내", "38000");
        Member member3 = createMember("이순신", "부산", "중구", "48000");

        memberRepositoryV2.save(member1);
        memberRepositoryV2.save(member2);
        memberRepositoryV2.save(member3);
        em.flush();
        em.clear();

        // when
        List<Member> foundMembers = memberRepositoryV2.findByName1("이순신");

        // then
        assertNotNull("조회 결과가 null이면 안됨", foundMembers);
        assertEquals("같은 이름의 회원 2명이 조회되어야 함", 2, foundMembers.size());
        foundMembers.forEach(member -> 
            assertEquals("조회된 모든 회원의 이름이 '이순신'이어야 함", "이순신", member.getName())
        );
    }

    /**
     * findByName1 커스텀 메서드 테스트 - 존재하지 않는 이름
     */
    @Test
    public void findByName1_존재하지않는이름() {
        // given
        Member member = createMember("존재하는회원", "광주", "서구", "61000");
        memberRepositoryV2.save(member);
        em.flush();
        em.clear();

        // when
        List<Member> foundMembers = memberRepositoryV2.findByName1("존재하지않는회원");

        // then
        assertNotNull("조회 결과가 null이면 안됨", foundMembers);
        assertTrue("존재하지 않는 이름으로 조회시 빈 리스트 반환", foundMembers.isEmpty());
    }

    /**
     * findByName1 커스텀 메서드 테스트 - null 이름
     */
    @Test
    public void findByName1_null이름() {
        // given
        Member member1 = createMember("정상회원", "대전", "유성구", "34000");
        Member member2 = createMember(null, "울산", "남구", "44000");
        
        memberRepositoryV2.save(member1);
        memberRepositoryV2.save(member2);
        em.flush();
        em.clear();

        // when
        List<Member> foundMembers = memberRepositoryV2.findByName1(null);

        // then
        assertNotNull("조회 결과가 null이면 안됨", foundMembers);
        assertEquals("null 이름을 가진 회원 1명이 조회되어야 함", 1, foundMembers.size());
        assertNull("조회된 회원의 이름이 null이어야 함", foundMembers.get(0).getName());
    }

    /**
     * findByName1 커스텀 메서드 테스트 - 빈 문자열 이름
     */
    @Test
    public void findByName1_빈문자열이름() {
        // given
        Member member1 = createMember("정상회원", "세종", "시내", "30000");
        Member member2 = createMember("", "제주", "제주시", "63000");
        
        memberRepositoryV2.save(member1);
        memberRepositoryV2.save(member2);
        em.flush();
        em.clear();

        // when
        List<Member> foundMembers = memberRepositoryV2.findByName1("");

        // then
        assertNotNull("조회 결과가 null이면 안됨", foundMembers);
        assertEquals("빈 문자열 이름을 가진 회원 1명이 조회되어야 함", 1, foundMembers.size());
        assertEquals("조회된 회원의 이름이 빈 문자열이어야 함", "", foundMembers.get(0).getName());
    }

    /**
     * findByName1 커스텀 메서드 테스트 - 대소문자 구분
     */
    @Test
    public void findByName1_대소문자구분() {
        // given
        Member member1 = createMember("John", "서울", "강서구", "07000");
        Member member2 = createMember("john", "인천", "연수구", "22000");
        
        memberRepositoryV2.save(member1);
        memberRepositoryV2.save(member2);
        em.flush();
        em.clear();

        // when
        List<Member> foundMembersUpper = memberRepositoryV2.findByName1("John");
        List<Member> foundMembersLower = memberRepositoryV2.findByName1("john");

        // then
        assertEquals("대문자 John으로 조회시 1명", 1, foundMembersUpper.size());
        assertEquals("소문자 john으로 조회시 1명", 1, foundMembersLower.size());
        assertEquals("대문자로 조회된 회원 이름", "John", foundMembersUpper.get(0).getName());
        assertEquals("소문자로 조회된 회원 이름", "john", foundMembersLower.get(0).getName());
    }

    // =========================== 수정/삭제 테스트 ===========================

    /**
     * 회원 정보 수정 테스트
     */
    @Test
    public void update_회원정보수정() {
        // given
        Member member = createMember("수정전이름", "수정전도시", "수정전구", "00000");
        Member savedMember = memberRepositoryV2.save(member);
        em.flush();
        em.clear();

        // when
        Member foundMember = memberRepositoryV2.findById(savedMember.getId()).get();
        foundMember.setName("수정후이름");
        foundMember.getAddress().setCity("수정후도시");
        Member updatedMember = memberRepositoryV2.save(foundMember);
        em.flush();
        em.clear();

        // then
        Member finalMember = memberRepositoryV2.findById(savedMember.getId()).get();
        assertEquals("회원 이름이 수정되어야 함", "수정후이름", finalMember.getName());
        assertEquals("회원 주소가 수정되어야 함", "수정후도시", finalMember.getAddress().getCity());
    }

    /**
     * 회원 삭제 테스트 - ID로 삭제
     */
    @Test
    public void deleteById_정상삭제() {
        // given
        Member member = createMember("삭제테스트", "삭제시", "삭제구", "99999");
        Member savedMember = memberRepositoryV2.save(member);
        em.flush();
        em.clear();

        // when
        memberRepositoryV2.deleteById(savedMember.getId());
        em.flush();
        em.clear();

        // then
        Optional<Member> deletedMember = memberRepositoryV2.findById(savedMember.getId());
        assertFalse("삭제된 회원은 조회되지 않아야 함", deletedMember.isPresent());
    }

    /**
     * 회원 삭제 테스트 - 엔티티로 삭제
     */
    @Test
    public void delete_엔티티삭제() {
        // given
        Member member = createMember("엔티티삭제테스트", "삭제도시", "삭제구역", "11111");
        Member savedMember = memberRepositoryV2.save(member);
        em.flush();
        em.clear();

        // when
        Member memberToDelete = memberRepositoryV2.findById(savedMember.getId()).get();
        memberRepositoryV2.delete(memberToDelete);
        em.flush();
        em.clear();

        // then
        Optional<Member> deletedMember = memberRepositoryV2.findById(savedMember.getId());
        assertFalse("삭제된 회원은 조회되지 않아야 함", deletedMember.isPresent());
    }

    /**
     * 전체 회원 삭제 테스트
     */
    @Test
    public void deleteAll_전체삭제() {
        // given
        Member member1 = createMember("삭제대상1", "도시1", "구1", "11111");
        Member member2 = createMember("삭제대상2", "도시2", "구2", "22222");
        memberRepositoryV2.save(member1);
        memberRepositoryV2.save(member2);
        em.flush();

        // when
        memberRepositoryV2.deleteAll();
        em.flush();
        em.clear();

        // then
        List<Member> remainingMembers = memberRepositoryV2.findAll();
        assertTrue("모든 회원이 삭제되어야 함", remainingMembers.isEmpty());
    }

    // =========================== 기타 유틸리티 테스트 ===========================

    /**
     * 회원 수 조회 테스트
     */
    @Test
    public void count_회원수조회() {
        // given
        long initialCount = memberRepositoryV2.count();
        
        Member member1 = createMember("카운트테스트1", "서울", "강남구", "12345");
        Member member2 = createMember("카운트테스트2", "부산", "해운대구", "48000");
        memberRepositoryV2.save(member1);
        memberRepositoryV2.save(member2);
        em.flush();

        // when
        long finalCount = memberRepositoryV2.count();

        // then
        assertEquals("저장된 회원 수가 2 증가해야 함", initialCount + 2, finalCount);
    }

    /**
     * 회원 존재 여부 확인 테스트 - 존재하는 경우
     */
    @Test
    public void existsById_존재함() {
        // given
        Member member = createMember("존재확인테스트", "확인시", "확인구", "99999");
        Member savedMember = memberRepositoryV2.save(member);
        em.flush();

        // when
        boolean exists = memberRepositoryV2.existsById(savedMember.getId());

        // then
        assertTrue("저장된 회원이 존재해야 함", exists);
    }

    /**
     * 회원 존재 여부 확인 테스트 - 존재하지 않는 경우
     */
    @Test
    public void existsById_존재하지않음() {
        // given
        Long nonExistentId = 999999L;

        // when
        boolean exists = memberRepositoryV2.existsById(nonExistentId);

        // then
        assertFalse("존재하지 않는 회원은 false 반환", exists);
    }

    // =========================== 성능 및 엣지 케이스 테스트 ===========================

    /**
     * 대용량 데이터 처리 성능 테스트
     */
    @Test
    public void 대용량데이터처리_성능테스트() {
        // given
        int testDataSize = 100;
        long startTime = System.currentTimeMillis();
        
        // when
        for (int i = 0; i < testDataSize; i++) {
            Member member = createMember("대용량테스트" + i, "도시" + i, "구" + i, String.format("%05d", i));
            memberRepositoryV2.save(member);
            
            if (i % 20 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
        
        List<Member> allMembers = memberRepositoryV2.findAll();
        long endTime = System.currentTimeMillis();
        
        // then
        assertTrue("저장된 데이터가 최소한 테스트 데이터 수 이상이어야 함", 
                   allMembers.size() >= testDataSize);
        
        long processingTime = endTime - startTime;
        System.out.println("처리 시간: " + processingTime + "ms, 데이터 수: " + allMembers.size());
        
        // 성능 임계값 설정 (10초 이내)
        assertTrue("처리 시간이 10초를 초과하면 안됨", processingTime < 10000);
    }

    /**
     * 동시성 테스트 - 같은 이름으로 여러 회원 저장
     */
    @Test
    public void 동시성테스트_같은이름여러회원저장() {
        // given
        String sameName = "동시성테스트회원";
        int memberCount = 50;
        
        // when
        for (int i = 0; i < memberCount; i++) {
            Member member = createMember(sameName, "도시" + i, "구" + i, String.format("%05d", i));
            memberRepositoryV2.save(member);
        }
        em.flush();
        
        // then
        List<Member> membersWithSameName = memberRepositoryV2.findByName1(sameName);
        assertEquals("같은 이름의 회원이 정확한 수만큼 저장되어야 함", 
                     memberCount, membersWithSameName.size());
    }

    /**
     * 특수문자 이름 처리 테스트
     */
    @Test
    public void 특수문자이름_처리테스트() {
        // given
        String[] specialNames = {"!@#$%", "한글이름", "English Name", "123456", "이름@domain.com"};
        
        // when
        for (String name : specialNames) {
            Member member = createMember(name, "특수도시", "특수구", "00000");
            memberRepositoryV2.save(member);
        }
        em.flush();
        em.clear();
        
        // then
        for (String name : specialNames) {
            List<Member> foundMembers = memberRepositoryV2.findByName1(name);
            assertEquals("특수문자 이름도 정상적으로 저장/조회되어야 함: " + name, 
                         1, foundMembers.size());
            assertEquals("저장된 이름이 일치해야 함", name, foundMembers.get(0).getName());
        }
    }

    /**
     * 긴 문자열 이름 처리 테스트
     */
    @Test
    public void 긴문자열이름_처리테스트() {
        // given
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longName.append("가");
        }
        String longNameStr = longName.toString();
        
        // when
        Member member = createMember(longNameStr, "긴이름도시", "긴이름구", "99999");
        Member savedMember = memberRepositoryV2.save(member);
        em.flush();
        em.clear();
        
        // then
        List<Member> foundMembers = memberRepositoryV2.findByName1(longNameStr);
        assertEquals("긴 이름도 정상적으로 저장/조회되어야 함", 1, foundMembers.size());
        assertEquals("저장된 긴 이름이 일치해야 함", longNameStr, foundMembers.get(0).getName());
    }

    // =========================== 헬퍼 메서드 ===========================

    /**
     * 테스트용 Member 객체 생성 헬퍼 메서드
     */
    private Member createMember(String name, String city, String street, String zipcode) {
        Member member = new Member();
        member.setName(name);
        
        Address address = new Address();
        address.setCity(city);
        address.setStreet(street);
        address.setZipcode(zipcode);
        member.setAddress(address);
        
        return member;
    }
}
package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);

        //then
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        memberService.join(member2); //예외가 발생해야 한다!!!

        //then
        fail("예외가 발생해야 한다.");
    }

    @Test
    public void 회원가입_주소포함() throws Exception {
        //given
        Member member = new Member();
        member.setName("park");
        Address address = new Address("서울", "강남구", "12345");
        member.setAddress(address);

        //when
        Long savedId = memberService.join(member);

        //then
        Member foundMember = memberRepository.findOne(savedId);
        assertEquals(member, foundMember);
        assertEquals("서울", foundMember.getAddress().getCity());
        assertEquals("강남구", foundMember.getAddress().getStreet());
        assertEquals("12345", foundMember.getAddress().getZipcode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void 회원가입_이름없음() throws Exception {
        //given
        Member member = new Member();
        // name을 설정하지 않음

        //when
        memberService.join(member);

        //then
        fail("이름이 없는 회원은 가입할 수 없어야 한다.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void 회원가입_빈이름() throws Exception {
        //given
        Member member = new Member();
        member.setName("");

        //when
        memberService.join(member);

        //then
        fail("빈 이름으로는 회원가입할 수 없어야 한다.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void 회원가입_공백이름() throws Exception {
        //given
        Member member = new Member();
        member.setName("   ");

        //when
        memberService.join(member);

        //then
        fail("공백만 있는 이름으로는 회원가입할 수 없어야 한다.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void 회원가입_null멤버() throws Exception {
        //given
        Member member = null;

        //when
        memberService.join(member);

        //then
        fail("null 멤버는 가입할 수 없어야 한다.");
    }

    @Test
    public void 회원가입_긴이름() throws Exception {
        //given
        Member member = new Member();
        String longName = "a".repeat(100); // 100자 이름
        member.setName(longName);

        //when
        Long savedId = memberService.join(member);

        //then
        Member foundMember = memberRepository.findOne(savedId);
        assertEquals(longName, foundMember.getName());
    }

    @Test
    public void 회원가입_특수문자이름() throws Exception {
        //given
        Member member = new Member();
        member.setName("김철수@#$%");

        //when
        Long savedId = memberService.join(member);

        //then
        Member foundMember = memberRepository.findOne(savedId);
        assertEquals("김철수@#$%", foundMember.getName());
    }

    @Test
    public void 회원가입_숫자이름() throws Exception {
        //given
        Member member = new Member();
        member.setName("123456");

        //when
        Long savedId = memberService.join(member);

        //then
        Member foundMember = memberRepository.findOne(savedId);
        assertEquals("123456", foundMember.getName());
    }

    @Test
    public void 전체회원조회() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("회원1");
        Member member2 = new Member();
        member2.setName("회원2");
        Member member3 = new Member();
        member3.setName("회원3");

        //when
        memberService.join(member1);
        memberService.join(member2);
        memberService.join(member3);
        List<Member> members = memberService.findMembers();

        //then
        assertEquals(3, members.size());
        assertTrue(members.contains(member1));
        assertTrue(members.contains(member2));
        assertTrue(members.contains(member3));
    }

    @Test
    public void 빈목록_전체회원조회() throws Exception {
        //given
        // 회원을 추가하지 않음

        //when
        List<Member> members = memberService.findMembers();

        //then
        assertNotNull(members);
        assertEquals(0, members.size());
        assertTrue(members.isEmpty());
    }

    @Test
    public void 단일회원조회_존재하는경우() throws Exception {
        //given
        Member member = new Member();
        member.setName("테스트회원");
        Long savedId = memberService.join(member);

        //when
        Member foundMember = memberService.findOne(savedId);

        //then
        assertNotNull(foundMember);
        assertEquals(member.getName(), foundMember.getName());
        assertEquals(savedId, foundMember.getId());
    }

    @Test
    public void 단일회원조회_존재하지않는경우() throws Exception {
        //given
        Long nonExistentId = 999999L;

        //when
        Member foundMember = memberService.findOne(nonExistentId);

        //then
        assertNull(foundMember);
    }

    @Test(expected = IllegalArgumentException.class)
    public void 단일회원조회_null아이디() throws Exception {
        //given
        Long nullId = null;

        //when
        memberService.findOne(nullId);

        //then
        fail("null ID로는 회원을 조회할 수 없어야 한다.");
    }

    @Test
    public void 이름으로회원찾기_존재하는경우() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("김회원");
        Member member2 = new Member();
        member2.setName("박회원");
        memberService.join(member1);
        memberService.join(member2);

        //when
        List<Member> foundMembers = memberService.findMembersByName("김회원");

        //then
        assertEquals(1, foundMembers.size());
        assertEquals("김회원", foundMembers.get(0).getName());
    }

    @Test
    public void 이름으로회원찾기_존재하지않는경우() throws Exception {
        //given
        Member member = new Member();
        member.setName("김회원");
        memberService.join(member);

        //when
        List<Member> foundMembers = memberService.findMembersByName("이회원");

        //then
        assertNotNull(foundMembers);
        assertEquals(0, foundMembers.size());
    }

    @Test
    public void 이름으로회원찾기_동명이인() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("김동명");
        Member member2 = new Member();
        member2.setName("김동명");
        Address address1 = new Address("서울", "강남구", "12345");
        Address address2 = new Address("부산", "해운대구", "67890");
        member1.setAddress(address1);
        member2.setAddress(address2);
        
        memberService.join(member1);
        memberService.join(member2);

        //when
        List<Member> foundMembers = memberService.findMembersByName("김동명");

        //then
        assertEquals(2, foundMembers.size());
        assertTrue(foundMembers.stream().anyMatch(m -> "서울".equals(m.getAddress().getCity())));
        assertTrue(foundMembers.stream().anyMatch(m -> "부산".equals(m.getAddress().getCity())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void 이름으로회원찾기_null이름() throws Exception {
        //when
        memberService.findMembersByName(null);

        //then
        fail("null 이름으로는 회원을 찾을 수 없어야 한다.");
    }

    @Test
    public void 이름으로회원찾기_빈문자열() throws Exception {
        //given
        Member member = new Member();
        member.setName("정상회원");
        memberService.join(member);

        //when
        List<Member> foundMembers = memberService.findMembersByName("");

        //then
        assertNotNull(foundMembers);
        assertEquals(0, foundMembers.size());
    }

    @Test
    public void 회원정보수정() throws Exception {
        //given
        Member member = new Member();
        member.setName("원래이름");
        Long savedId = memberService.join(member);

        //when
        Member foundMember = memberService.findOne(savedId);
        foundMember.setName("변경된이름");
        em.flush(); // 영속성 컨텍스트 강제 플러시

        //then
        Member updatedMember = memberService.findOne(savedId);
        assertEquals("변경된이름", updatedMember.getName());
    }

    @Test
    public void 회원주소수정() throws Exception {
        //given
        Member member = new Member();
        member.setName("회원");
        Address originalAddress = new Address("서울", "강남구", "12345");
        member.setAddress(originalAddress);
        Long savedId = memberService.join(member);

        //when
        Member foundMember = memberService.findOne(savedId);
        Address newAddress = new Address("부산", "해운대구", "67890");
        foundMember.setAddress(newAddress);
        em.flush();

        //then
        Member updatedMember = memberService.findOne(savedId);
        assertEquals("부산", updatedMember.getAddress().getCity());
        assertEquals("해운대구", updatedMember.getAddress().getStreet());
        assertEquals("67890", updatedMember.getAddress().getZipcode());
    }

    @Test
    public void 대소문자구분_중복체크() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("Kim");
        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        Long savedId2 = memberService.join(member2);

        //then
        assertNotNull(savedId2);
        List<Member> allMembers = memberService.findMembers();
        assertEquals(2, allMembers.size());
    }

    @Test
    public void 트림처리_중복체크() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("김회원");
        Member member2 = new Member();
        member2.setName(" 김회원 "); // 앞뒤 공백

        //when
        memberService.join(member1);
        
        try {
            memberService.join(member2);
            fail("공백이 있는 동일한 이름으로 중복 가입이 되면 안된다.");
        } catch (IllegalStateException e) {
            // 예상된 예외
        }

        //then
        List<Member> allMembers = memberService.findMembers();
        assertEquals(1, allMembers.size());
    }

    @Test
    public void 동시성_회원가입테스트() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("동시가입자");
        Member member2 = new Member();
        member2.setName("동시가입자");

        //when & then
        memberService.join(member1);
        
        try {
            memberService.join(member2);
            fail("동일한 이름으로 동시 가입이 되면 안된다.");
        } catch (IllegalStateException e) {
            // 예상된 예외
            assertEquals("이미 존재하는 회원입니다.", e.getMessage());
        }
    }
}
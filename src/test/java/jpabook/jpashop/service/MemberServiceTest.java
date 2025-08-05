package jpabook.jpashop.service;

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

    @Test
    public void 회원가입_정상케이스_완전한_정보() throws Exception {
        //given
        Member member = new Member();
        member.setName("완전한회원");

        //when
        Long savedId = memberService.join(member);

        //then
        assertNotNull("저장된 ID는 null이 아니어야 한다", savedId);
        Member foundMember = memberRepository.findOne(savedId);
        assertNotNull("저장된 회원을 찾을 수 있어야 한다", foundMember);
        assertEquals("회원 이름이 일치해야 한다", "완전한회원", foundMember.getName());
        assertEquals("회원 ID가 일치해야 한다", savedId, foundMember.getId());

    @Test
    public void 회원가입_빈_이름_검증() throws Exception {
        //given
        Member member = new Member();
        member.setName("");

        //when
        Long savedId = memberService.join(member);

        //then
        assertNotNull("빈 이름이라도 저장되어야 한다", savedId);
        Member foundMember = memberRepository.findOne(savedId);
        assertEquals("빈 이름이 저장되어야 한다", "", foundMember.getName());

    @Test
    public void 회원가입_null_이름_검증() throws Exception {
        //given
        Member member = new Member();
        member.setName(null);

        //when
        Long savedId = memberService.join(member);

        //then
        assertNotNull("null 이름이라도 ID는 생성되어야 한다", savedId);
        Member foundMember = memberRepository.findOne(savedId);
        assertNull("null 이름이 저장되어야 한다", foundMember.getName());

    @Test
    public void 회원가입_특수문자_이름_검증() throws Exception {
        //given
        Member member = new Member();
        member.setName("김철수!@#$%^&*()_+-=[]{}|;:,.<>?");

        //when
        Long savedId = memberService.join(member);

        //then
        assertNotNull("특수문자 포함 이름도 저장되어야 한다", savedId);
        Member foundMember = memberRepository.findOne(savedId);
        assertEquals("특수문자 포함 이름이 정확히 저장되어야 한다", "김철수!@#$%^&*()_+-=[]{}|;:,.<>?", foundMember.getName());
    }
    }
    }
    }
    }
}
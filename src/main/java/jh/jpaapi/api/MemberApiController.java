package jh.jpaapi.api;

import jh.jpaapi.doamin.Member;
import jh.jpaapi.servcie.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API
     *   위한 모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론!!!
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
    @PostMapping("/api/v1/members")
    //RequestBody는 JSON으로 온 Body를 Member 그대로 매핑
    //Member.name에 @NotEmpty을 적용하면 name값은 꼭와야됨(@Valid가 체크함)
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 등록 V2: 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     */
    @PostMapping("/api/v2/members")
    //별도의 DTO 사용
    //Entity 사용하면 API 스펙을 보진 않는 이상 어떤 값이 들어오는지 모름
    //별도의 DTO을 사용하면 API 스펙이 정리가됨
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 수정 API
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request){

        //커맨드와 쿼리르 분리할시 유지보수가 좋음 하나의 방법임
        memberService.update(id, request.getName());
        //id로 조회하기 부하가 안일어날때 이렇게 사용(다시 조회)
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(id, findMember.getName());
    }

    /**
     * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출한다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 모든 값이 노출된다.
     * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
     * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데,
         한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.
     *   (별도의 Result 클래스 생성으로 해결)
     * 결론
     * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     *
     * 조회 V1 : 안 좋은 버전, 모든 엔티티가 노출, @JsonIgnore
     * -> 이건 정말 최악, api가 이거 하나인가! 화면에 종속적이지 마라!
     */
    @GetMapping("/api/v1/members")
    public List<Member> memberV1(){
        return memberService.findMembers();

        /*
        결과
        [{"id": 1,"name": "회원1",
           "address": {"city": "서울", "street": "영등포", "zipcode": "111"},
            "orders": []}, ... ]*/
        //orders가 보여주기싫다면 @JsonIgnore 추가 하면 제외하고 JSON 생성
        //Array로 넘어옴 : [ ... ] -> 굳어버려 확장성이 떨어짐
    }

    /**
     * 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
     */
    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();
        //엔티티 -> DTO 변환
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());  //Array로 반환되지 않음

        return new Result(collect.size(), collect); //count을 추가 가능

        /*
        결과
        { "count": 3, "data": [{"name": "회원1"}, {"name": "나다"}, ...]}*/
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty   //name값이 꼭 필요함
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }
}

/**
api와 템플릿은 패키지를 따로 하여 예외처리하는 것을 권고
템플릿 패키지는 오류를 html로 하는반면
api는 오류 처리를 json으로 하기 때문
 */

/**
 * !!!! 결론 !!!!
 * API을 사용할때 엔티티로 받거다 노출시키지 말자!!!
 * 무조건 새로 생성해서 받고 노출!!!
 */
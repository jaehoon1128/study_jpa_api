package jpabook.jpashop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(MemberApiController.class)
public class MemberApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;
    private final Long TEST_MEMBER_ID = 1L;
    private final String TEST_MEMBER_NAME = "TestUser";

    @Before
    public void setUp() {
        testMember = new Member();
        testMember.setId(TEST_MEMBER_ID);
        testMember.setName(TEST_MEMBER_NAME);
    }

    // 회원 등록 V1 API 테스트
    @Test
    public void saveMemberV1_WithValidMember_ShouldReturnCreatedMember() throws Exception {
        // given
        given(memberService.join(any(Member.class))).willReturn(TEST_MEMBER_ID);

        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMember)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_MEMBER_ID));

        verify(memberService).join(any(Member.class));
    }

    @Test
    public void saveMemberV1_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(memberService, never()).join(any(Member.class));
    }

    @Test
    public void saveMemberV1_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(memberService, never()).join(any(Member.class));
    }

    @Test
    public void saveMemberV1_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // given
        given(memberService.join(any(Member.class)))
                .willThrow(new RuntimeException("Database connection failed"));

        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMember)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(memberService).join(any(Member.class));
    }

    // 회원 등록 V2 API 테스트
    @Test
    public void saveMemberV2_WithValidRequest_ShouldReturnCreatedMember() throws Exception {
        // given
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(TEST_MEMBER_NAME);
        
        given(memberService.join(any(Member.class))).willReturn(TEST_MEMBER_ID);

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_MEMBER_ID));

        verify(memberService).join(any(Member.class));
    }

    @Test
    public void saveMemberV2_WithNullName_ShouldReturnBadRequest() throws Exception {
        // given
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(null);

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(memberService, never()).join(any(Member.class));
    }

    @Test
    public void saveMemberV2_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        // given
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName("");

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(memberService, never()).join(any(Member.class));
    }

    @Test
    public void saveMemberV2_WithDuplicateMember_ShouldReturnError() throws Exception {
        // given
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(TEST_MEMBER_NAME);
        
        given(memberService.join(any(Member.class)))
                .willThrow(new IllegalStateException("이미 존재하는 회원입니다."));

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(memberService).join(any(Member.class));
    }

    // 회원 수정 V2 API 테스트
    @Test
    public void updateMemberV2_WithValidRequest_ShouldReturnUpdatedMember() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Updated Name");
        
        Member updatedMember = new Member();
        updatedMember.setId(TEST_MEMBER_ID);
        updatedMember.setName("Updated Name");
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, "Updated Name");
        given(memberService.findOne(TEST_MEMBER_ID)).willReturn(updatedMember);

        // when & then
        mockMvc.perform(put("/api/v2/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_MEMBER_ID))
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(memberService).update(TEST_MEMBER_ID, "Updated Name");
        verify(memberService).findOne(TEST_MEMBER_ID);
    }

    @Test
    public void updateMemberV2_WithNonExistentId_ShouldReturnError() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Updated Name");
        
        doThrow(new IllegalArgumentException("Member not found with id: 999"))
                .when(memberService).update(anyLong(), anyString());

        // when & then
        mockMvc.perform(put("/api/v2/members/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(memberService).update(999L, "Updated Name");
    }

    @Test
    public void updateMemberV2_WithInvalidPathVariable_ShouldReturnBadRequest() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Updated Name");

        // when & then
        mockMvc.perform(put("/api/v2/members/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(memberService, never()).update(anyLong(), anyString());
    }

    @Test
    public void updateMemberV2_WithNullName_ShouldReturnBadRequest() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName(null);

        // when & then
        mockMvc.perform(put("/api/v2/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(memberService, never()).update(anyLong(), anyString());
    }

    // 회원 수정 V2.1 API 테스트 - 특별히 aaabbccdd 메서드 호출에 집중
    @Test
    public void updateMemberV2_1_WithValidRequest_ShouldReturnUpdatedMember() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Updated Name V2.1");
        
        Member updatedMember = new Member();
        updatedMember.setId(TEST_MEMBER_ID);
        updatedMember.setName("Updated Name V2.1");
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, "Updated Name V2.1");
        given(memberService.aaabbccdd(TEST_MEMBER_ID)).willReturn(updatedMember);

        // when & then
        mockMvc.perform(put("/api/v2.1/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_MEMBER_ID))
                .andExpect(jsonPath("$.name").value("Updated Name V2.1"));

        verify(memberService).update(TEST_MEMBER_ID, "Updated Name V2.1");
        verify(memberService).aaabbccdd(TEST_MEMBER_ID);
    }

    @Test
    public void updateMemberV2_1_ShouldCallAaabbccddMethod_NotFindOne() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Test Name");
        
        Member member = new Member();
        member.setId(TEST_MEMBER_ID);
        member.setName("Test Name");
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, "Test Name");
        given(memberService.aaabbccdd(TEST_MEMBER_ID)).willReturn(member);

        // when
        mockMvc.perform(put("/api/v2.1/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // then - V2.1은 aaabbccdd를 호출하고 findOne은 호출하지 않음
        verify(memberService).aaabbccdd(TEST_MEMBER_ID);
        verify(memberService, never()).findOne(anyLong());
    }

    @Test
    public void updateMemberV2_1_WhenAaabbccddThrowsException_ShouldReturnError() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Test Name");
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, "Test Name");
        given(memberService.aaabbccdd(TEST_MEMBER_ID))
                .willThrow(new RuntimeException("aaabbccdd method failed"));

        // when & then
        mockMvc.perform(put("/api/v2.1/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(memberService).update(TEST_MEMBER_ID, "Test Name");
        verify(memberService).aaabbccdd(TEST_MEMBER_ID);
    }

    @Test
    public void updateMemberV2_1_WhenAaabbccddReturnsNull_ShouldHandleGracefully() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Test Name");
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, "Test Name");
        given(memberService.aaabbccdd(TEST_MEMBER_ID)).willReturn(null);

        // when & then
        mockMvc.perform(put("/api/v2.1/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(memberService).update(TEST_MEMBER_ID, "Test Name");
        verify(memberService).aaabbccdd(TEST_MEMBER_ID);
    }

    // 회원 조회 V1 API 테스트
    @Test
    public void membersV1_ShouldReturnAllMembers() throws Exception {
        // given
        Member member1 = new Member();
        member1.setId(1L);
        member1.setName("Member1");
        
        Member member2 = new Member();
        member2.setId(2L);
        member2.setName("Member2");
        
        List<Member> members = Arrays.asList(member1, member2);
        given(memberService.findMembers()).willReturn(members);

        // when & then
        mockMvc.perform(get("/api/v1/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Member1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Member2"));

        verify(memberService).findMembers();
    }

    @Test
    public void membersV1_WhenNoMembers_ShouldReturnEmptyList() throws Exception {
        // given
        given(memberService.findMembers()).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(memberService).findMembers();
    }

    @Test
    public void membersV1_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // given
        given(memberService.findMembers())
                .willThrow(new RuntimeException("Database connection failed"));

        // when & then
        mockMvc.perform(get("/api/v1/members"))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(memberService).findMembers();
    }

    // 회원 조회 V2 API 테스트
    @Test
    public void membersV2_ShouldReturnMemberDtosWrappedInResult() throws Exception {
        // given
        Member member1 = new Member();
        member1.setId(1L);
        member1.setName("Member1");
        
        Member member2 = new Member();
        member2.setId(2L);
        member2.setName("Member2");
        
        List<Member> members = Arrays.asList(member1, member2);
        given(memberService.findMembers()).willReturn(members);

        // when & then
        mockMvc.perform(get("/api/v2/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name").value("Member1"))
                .andExpect(jsonPath("$.data[1].name").value("Member2"))
                .andExpect(jsonPath("$.data[0].id").doesNotExist())
                .andExpect(jsonPath("$.data[1].id").doesNotExist());

        verify(memberService).findMembers();
    }

    @Test
    public void membersV2_WhenNoMembers_ShouldReturnEmptyDataInResult() throws Exception {
        // given
        given(memberService.findMembers()).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v2/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.data").isArray());

        verify(memberService).findMembers();
    }

    @Test
    public void membersV2_ShouldOnlyExposeNameField() throws Exception {
        // given
        Member member = new Member();
        member.setId(1L);
        member.setName("TestMember");
        
        given(memberService.findMembers()).willReturn(Collections.singletonList(member));

        // when & then
        mockMvc.perform(get("/api/v2/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("TestMember"))
                .andExpect(jsonPath("$.data[0].id").doesNotExist())
                .andExpect(jsonPath("$.data[0]", not(hasKey("id"))));

        verify(memberService).findMembers();
    }

    @Test
    public void membersV2_WithLargeNumberOfMembers_ShouldHandleEfficiently() throws Exception {
        // given
        List<Member> largeNumberOfMembers = Collections.nCopies(100, testMember);
        given(memberService.findMembers()).willReturn(largeNumberOfMembers);

        // when & then
        mockMvc.perform(get("/api/v2/members"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(100)))
                .andExpect(jsonPath("$.data[0].name").value(TEST_MEMBER_NAME));

        verify(memberService).findMembers();
    }

    // DTO 클래스 테스트
    @Test
    public void createMemberRequest_ShouldWorkCorrectly() {
        // given
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        String testName = "Test Member";

        // when
        request.setName(testName);

        // then
        assertEquals(testName, request.getName());
    }

    @Test
    public void createMemberResponse_ShouldWorkCorrectly() {
        // given & when
        MemberApiController.CreateMemberResponse response = 
                new MemberApiController.CreateMemberResponse(TEST_MEMBER_ID);

        // then
        assertEquals(TEST_MEMBER_ID, response.getId());
    }

    @Test
    public void updateMemberRequest_ShouldWorkCorrectly() {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        String updatedName = "Updated Name";

        // when
        request.setName(updatedName);

        // then
        assertEquals(updatedName, request.getName());
    }

    @Test
    public void updateMemberResponse_ShouldWorkCorrectly() {
        // given & when
        MemberApiController.UpdateMemberResponse response = 
                new MemberApiController.UpdateMemberResponse(TEST_MEMBER_ID, TEST_MEMBER_NAME);

        // then
        assertEquals(TEST_MEMBER_ID, response.getId());
        assertEquals(TEST_MEMBER_NAME, response.getName());
    }

    @Test
    public void memberDto_ShouldWorkCorrectly() {
        // given & when
        MemberApiController.MemberDto dto = new MemberApiController.MemberDto(TEST_MEMBER_NAME);

        // then
        assertEquals(TEST_MEMBER_NAME, dto.getName());
    }

    @Test
    public void result_ShouldWorkCorrectly() {
        // given
        List<String> testData = Arrays.asList("data1", "data2");

        // when
        MemberApiController.Result<List<String>> result = 
                new MemberApiController.Result<>(testData);

        // then
        assertEquals(testData, result.getData());
        assertEquals(2, result.getData().size());
    }

    // HTTP 메서드별 엣지 케이스 테스트
    @Test
    public void wrongHttpMethod_ShouldReturn405() throws Exception {
        mockMvc.perform(patch("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void requestWithoutContentType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v2/members")
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void requestWithVeryLongName_ShouldHandleGracefully() throws Exception {
        // given
        String veryLongName = "a".repeat(1000);
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(veryLongName);
        
        given(memberService.join(any(Member.class))).willReturn(TEST_MEMBER_ID);

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void requestWithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // given
        String nameWithSpecialChars = "김철수@#$%^&*()";
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(nameWithSpecialChars);
        
        given(memberService.join(any(Member.class))).willReturn(TEST_MEMBER_ID);

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).join(any(Member.class));
    }

    @Test
    public void requestWithUnicodeName_ShouldHandleCorrectly() throws Exception {
        // given
        String unicodeName = "김철수 🎉 Test 测试";
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName(unicodeName);
        
        Member updatedMember = new Member();
        updatedMember.setId(TEST_MEMBER_ID);
        updatedMember.setName(unicodeName);
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, unicodeName);
        given(memberService.findOne(TEST_MEMBER_ID)).willReturn(updatedMember);

        // when & then
        mockMvc.perform(put("/api/v2/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(unicodeName));
    }

    // 엣지 케이스: 네트워크 및 시스템 레벨 오류
    @Test
    public void largeJsonPayload_ShouldHandleCorrectly() throws Exception {
        // given - 매우 큰 JSON 페이로드
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("A");
        }
        
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(largeContent.toString());
        
        given(memberService.join(any(Member.class))).willReturn(TEST_MEMBER_ID);

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void concurrentUpdateRequests_ShouldHandleCorrectly() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Concurrent Update");
        
        Member updatedMember = new Member();
        updatedMember.setId(TEST_MEMBER_ID);
        updatedMember.setName("Concurrent Update");
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, "Concurrent Update");
        given(memberService.findOne(TEST_MEMBER_ID)).willReturn(updatedMember);

        // when & then - 동일한 ID에 대한 동시 업데이트 시뮬레이션
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(put("/api/v2/members/{id}", TEST_MEMBER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        verify(memberService, times(3)).update(TEST_MEMBER_ID, "Concurrent Update");
        verify(memberService, times(3)).findOne(TEST_MEMBER_ID);
    }

    // 보안 관련 테스트
    @Test
    public void maliciousInputInName_ShouldBeSanitized() throws Exception {
        // given - XSS 공격 시도
        String maliciousName = "<script>alert('xss')</script>";
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(maliciousName);
        
        given(memberService.join(any(Member.class))).willReturn(TEST_MEMBER_ID);

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).join(any(Member.class));
    }

    @Test
    public void sqlInjectionAttemptInName_ShouldBeHandled() throws Exception {
        // given - SQL Injection 시도
        String sqlInjectionName = "'; DROP TABLE members; --";
        MemberApiController.CreateMemberRequest request = new MemberApiController.CreateMemberRequest();
        request.setName(sqlInjectionName);
        
        given(memberService.join(any(Member.class))).willReturn(TEST_MEMBER_ID);

        // when & then
        mockMvc.perform(post("/api/v2/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(memberService).join(any(Member.class));
    }

    // 성능 관련 테스트
    @Test
    public void responseTimeForSimpleGet_ShouldBeFast() throws Exception {
        // given
        given(memberService.findMembers()).willReturn(Collections.singletonList(testMember));

        // when & then
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/v2/members"))
                .andDo(print())
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // 응답 시간이 1초 이하여야 함 (단위 테스트이므로 충분히 빨라야 함)
        assertTrue("Response time should be less than 1000ms, but was: " + responseTime + "ms", 
                   responseTime < 1000);
        
        verify(memberService).findMembers();
    }

    // API 버전별 동작 차이 검증
    @Test
    public void compareV2AndV2_1UpdateBehavior() throws Exception {
        // given
        MemberApiController.UpdateMemberRequest request = new MemberApiController.UpdateMemberRequest();
        request.setName("Version Comparison Test");
        
        Member member = new Member();
        member.setId(TEST_MEMBER_ID);
        member.setName("Version Comparison Test");
        
        doNothing().when(memberService).update(TEST_MEMBER_ID, "Version Comparison Test");
        given(memberService.findOne(TEST_MEMBER_ID)).willReturn(member);
        given(memberService.aaabbccdd(TEST_MEMBER_ID)).willReturn(member);

        // when & then - V2 호출
        mockMvc.perform(put("/api/v2/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // when & then - V2.1 호출  
        mockMvc.perform(put("/api/v2.1/members/{id}", TEST_MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // then - 메서드 호출 검증
        verify(memberService, times(2)).update(TEST_MEMBER_ID, "Version Comparison Test");
        verify(memberService, times(1)).findOne(TEST_MEMBER_ID); // V2에서만 호출
        verify(memberService, times(1)).aaabbccdd(TEST_MEMBER_ID); // V2.1에서만 호출
    }
}
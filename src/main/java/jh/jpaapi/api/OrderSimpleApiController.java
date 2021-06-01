package jh.jpaapi.api;

import jh.jpaapi.doamin.Address;
import jh.jpaapi.doamin.Order;
import jh.jpaapi.doamin.OrderStatus;
import jh.jpaapi.repository.OrderRepository;
import jh.jpaapi.repository.OrderSearch;
import jh.jpaapi.repository.order.simplequery.OrderSimpleQueryDto;
import jh.jpaapi.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ManyToOne, OneToOne 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true)같이 사용
        for(Order order : all){
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress();   //Lazy 강제 초기화
        }
        return  all;
        /*
        문제 1
        Order -> Member -> Member.orders(Order) -> Member... 무한루프
        //엔티티 양방향 관계 중 한곳을 @JsonIgnore을 막아놔야 함

        문제 2
        지연로딩으로 Proxy 객체로 생성됨으로 에러가발생함  ~~~ByteBuddyInterceptor
        지연로딩인경우 Proxy 일때는 뿌리지 말라는 hibernate5 을서치하여 Hibernate5Module @Bean 등록하면됨

        결과
        [{"id": 4, "member": null, "orderItems": null, "delivery": null,
        "orderDate": "2021-06-01T02:40:26.071514", "status": "ORDER","totalPrice": 50000},
        {"id": 11,"member": null, "orderItems": null, "delivery": null,
        "orderDate": "2021-06-01T02:40:26.107419", "status": "ORDER", "totalPrice": 220000}]

        // hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true) 추가후
        [{"id": 4,
        "member": {"id": 1, "name": "userA",
            "address": {"city": "서울", "street": "1","zipcode": "111"}
            },
        "orderItems": [
        {"id": 6,
        "item": {"id": 2,"name": "JPA1 Book", ㅈ"price": 100000,
                 "stockQuantity": 99, "categories": [], "author": null, "isbn": null
                }, .....

         DTO을 사용하자!!
         - Hibernate5Module을 API 반환때문에 등록하지 말자
         - 지연로딩을 즉시로딩으로 변경하지도 말자
         */
    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 단점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2(){
        //ORDER 2개
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        /*
        ORDER -> SQL 1번 -> 결과 주문 수 2개
        결과 수 만큼 member, delibery 쿼리가 조회 -> SQL  4번
        N + 1 -> 1 + 회원 N + 배송 N
        */
        return result;
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * - 전체 컬럼(select 절 모두)을 가져와 필요한 값들만 매핑
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * V4. JPA에서 DTO로 바로 조회
     * - 쿼리 1번 호출
     * - select 절에서 원하는 데이터만 선택해서 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4(){
        //컨트롤러가 reposiotry에 의존관계가 생기면 안되기때문
        //SimpleOrderDto을 repository에 클래스(SimpleOrderQUeryDto) 새로 생성
        //의존관계는 가급적이면 한방향으로만 흐르는것을 권고
        return orderSimpleQueryRepository.findOrderDtos();
    }

    /**
     * V3
     * - 전체 select절을 가져옴
     * - 리포지토리 재활용성이 좋음
     * - Order는 엔티여서 향 후 udpdate 가능
     *
     * V4
     * - 원하는 select 절만 가져옴
     * - 화면에서 뽑을때는 성능은 좋음(네트워크 용량 최적화)(생각보다 미비..컬럼이 적으면..)
     * - 리포지토리 재활용성이 떨어짐
     * - dto로 조회했기 때문에 update 바로 불가
     *
     * 둘중 어떤게 더 좋냐 보단 테이블, 트래픽에 관해서 선택 권고
     *
     * 쿼리 방식 선택 권장 순서
     * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택
     * 2. 필요하면 페치 조인으로 성능을 최적화 -> 대부분 성능 이슈가 해결
     * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용
     * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용
     */

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        //DTO가 Entity를 받는건 중요하지않음
        //중요하지않는 곳(dto)에 중요한것(entity)을 받기때문
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
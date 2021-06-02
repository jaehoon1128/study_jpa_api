package jh.jpaapi.api;

import jh.jpaapi.doamin.Address;
import jh.jpaapi.doamin.Order;
import jh.jpaapi.doamin.OrderItem;
import jh.jpaapi.doamin.OrderStatus;
import jh.jpaapi.repository.OrderRepository;
import jh.jpaapi.repository.OrderSearch;
import jh.jpaapi.repository.order.query.OrderFlatDto;
import jh.jpaapi.repository.order.query.OrderItemQueryDto;
import jh.jpaapi.repository.order.query.OrderQueryDto;
import jh.jpaapi.repository.order.query.OrderQueryRepository;
import jh.jpaapi.servcie.query.OrderQueryService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     * - 트랜잭션안에 지연 로딩 필요
     * - 엔티티가 변하면 API 스펙이 변경됨
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 트랜잭션 안에서 지연 로딩 필요
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - 1:다 페치조인시 페이징 처리 못함
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        //1:다 을 페치조인 하면 페이징 처리를 못함
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * V3-1. 엔티티를 조회해서 DTO로 변환
     * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경가능)
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        //xToOne(OneToOne, ManyToOne) 관계는 모두 폐치조인(컬렉션(XToMany은 제외)
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        /**
         컬렉션은 지연로딩으로 조회
         1. hibernate.default_batch_fetch_size: 값(application.yml 참고)
         - 글로벌 처리
         2. @BatchSize(size = 값)
         - 개별 최적화
         - xToOne은 클래스위에 설정해야함(Item 참고)
         - xToMany은 컬렉션 위에 설정(Order.orderItems 참고)
         -> 값만큼 인쿼리내에 in으로 처리
         ex) 값: 10 총데이터 100개라면 지연쿼리는 10번 실행
         */
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     장점
     - 쿼리 호출 수가 1 + N 1 + 1 로 최적화
     - 조인보다 DB 데이터 전송량이 최적화
     - Order와 OrderItem을 조인하면 Order가 OrderItem 만큼 중복해서 조회
     - 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없음
     - 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소
     - 컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능

     결론**
     ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다. 따라서 ToOne 관계는 페치조인으로
     쿼리 수를 줄이고 해결하고, 나머지는 hibernate.default_batch_fetch_size 로 최적화 하자.

     default_batch_fetch_size는 100~1000 사이를 권장(단 DB따라 다름..)
     */

    /**
     * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
     * - 페이징 가능
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        ;
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
     * - fecth 조인보단 select 양이 줄어드냐 코드가 길어짐..
     * - 페이징 가능
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        ;
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
     * - 페이징 불가능
     * - 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터 추가되므로 상황에 따라 V5보다 더 느려질 수 있음
     * - 애플리케이션에서 추가 작업이 큼
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }

    /**
     * 1. 엔티티 조회 방식으로 우선 접근
     * 1. 페치조인으로 쿠리 수를 최적화
     * 2. 컬렉션 최적화
     * 1. 페이징 필요 V3.1
     * 2. 페이징 필요X -> 페치 조인 사용 V3
     * 2. 엔티티 조회방식으로 해결이 안되면 DTO 조회 방식 사용
     * 1. V4 : 코드가 단순, 특정 한건만 조회하면 성능도 잘나옴
     * 2. V5 : 코드가 복잡 대다수 V4보단 V5방식을 권장
     * 3. V6 : V4~6중에 항상 좋은건 아님(대체로 좋음)
     * v4, V5는 다른방식
     * 페이징이 불가
     * 데이터가 많으면 중복 전송이 증가하여 V5와 성능차이가 미비
     * 3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate
     * -> 성능 문제라면 캐싱 처리를 우선적으로 생각해봐야함
     * <p>
     * 엔티티는 캐싱하면 안됨(영속성 컨텍스트에 올려놓고 하는건데 문제가됨..)
     * 캐싱할때면 DTO을 담아서 해야함
     */

    //OSIV OFF일때
    private final OrderQueryService orderQueryService;

    @GetMapping("/api/v1/osiv/orders")
    public List<Order> ordersV1_OSIV_OFF() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // 지연로딩이 안되어 에러발생
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v3/osiv/orders")
    public List<jh.jpaapi.servcie.query.OrderDto> orderV3_OSIV_OFF() {
        return orderQueryService.orderV3_OSIV_OFF();
    }


    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //Dto안에 Entity도 존재해선 안됨..
        //private List<OrderItem> orderItems;
        //OrderItem(Entity) -> dto을 변경해야함
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName;    //상품명
        private int orderPrice;     //주문 가격
        private int count;          //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
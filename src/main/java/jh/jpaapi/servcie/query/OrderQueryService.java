package jh.jpaapi.servcie.query;

import jh.jpaapi.api.OrderApiController;
import jh.jpaapi.doamin.Order;
import jh.jpaapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public List<OrderDto> orderV3_OSIV_OFF() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

}

/**
OSIV
- Open Session in View : 하이버네이트
- Open EntityManager In View : JPA
관례상 OSIV

OSIV ON
- spring.jap.open-in-view: true(기본값)
    - application.yml 참고
- 트랜잭션 시작처럼 최초 데이터베이스 커넥션 시작 시점
- API 응답이 끝날 때 까지 영속성 컨텍스트와 데이터베이스 커넥션을 유지
- View Template나 API 컨트롤에서 지연로딩이 가능
- 지연 로딩은 영속성 컨텍스트가 살아있어야 하고, 영속성 컨텍스트는 기본적으로 데이터베이스 커넥션을 유지 해야 함
- 커넥션이 모자를 수 있음 -> 장애로 이어짐 ***
    - 실시간 트래픽이 중요한 애플리케이션에서 발생 할 수 있음
        - 오랜시간동안 데이터베이스 커넥션 리소스를 사용하기 때문
        - 외부 API 호출시 외부 API 대기 시간만큼 리소를 반환하지 못하고 유지

OSIV OFF
- spring.jap.open-in-view: false
- 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고 데이터베이스 커넥션도 반환
    - 커넥션 리소스 낭비하지 않음
- 지연로딩을 트랜잭션 안에서 처리해아함
- view template에서 지연로딩이 동작하지 않음
    - OrderApiContoller.ordersV1_OSIV_OFF 참고
- 커멘드와 쿼리 분리
  - OrderApiContoller.ordersV3_OSIV_OFF
  - 패키지를 OrderQueryService 처럼은 아니지만 좀더 좋게 변경 권고
    - 화면이나 API에 맞춘 서비스(주로 읽기전용 트랜잭션)일 때 사용
  - 보통 서비스 계층에서 트랜잭션을 유지

권장 : 고객 서비스의 실시간 API는 OSIV는 끄고, ADMIN 처럼 커넥션을 많이 사용하지 않는곳은 OSIV을 켠다.
 */
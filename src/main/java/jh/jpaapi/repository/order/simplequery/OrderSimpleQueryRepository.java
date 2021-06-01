package jh.jpaapi.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jh.jpaapi.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order as o " +
                        " join o.member m "+
                        " join o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }
}
/**
일반 리포지스토리는 엔티티로만
다음과 같은 조회를 패키지로 나누면 화면 전용 이라는 것을 인지 가능
 */
package jpabook.jpashop.repository.order;

import jpabook.jpashop.domain.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepositoryV2 extends JpaRepository<Order,Long> {

	// Spring Data JPA에서는 쿼리 메서드 또는 @Query를 사용해야 합니다.
	@Query("select distinct o from Order o" +
	        " join fetch o.member m" +
	        " join fetch o.delivery d" +
	        " join fetch o.orderItems oi" +
	        " join fetch oi.item i")
	List<Order> findAllWithItem();


	// Spring Data JPA에서는 페이징 처리를 위해 메서드 시그니처에 Pageable을 사용합니다.
	@Query("select o from Order o join fetch o.member m join fetch o.delivery d")
	List<Order> findAllWithMemberDelivery(Pageable pageable);

}

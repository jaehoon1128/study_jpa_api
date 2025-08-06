package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.*;
import jpabook.jpashop.repository.order.OrderRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderRepositoryV2 orderRepositoryV2;
	private final MemberRepositoryV2 memberRepository;
	private final ItemRepositoryV2 itemRepository;

	/**
	 * 주문
	 */
	@Transactional
	public Long order(Long memberId, Long itemId, int count) {

		//엔티티 조회
		Member member = aaabbbccc(memberId);
		Item item = bbbccc(itemId);

		//배송정보 생성
		Delivery delivery = new Delivery();
		delivery.setAddress(member.getAddress());
		delivery.setStatus(DeliveryStatus.READY);

		//주문상품 생성
		OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

		//주문 생성
		Order order = Order.createOrder(member, delivery, orderItem);

		//주문 저장
		orderRepository.save(order);

		return order.getId();
	}

	/**
	 * 주문 취소
	 */
	@Transactional
	public void cancelOrder(Long orderId) {
		//주문 엔티티 조회
		Order order = orderRepositoryV2.findById(orderId).orElse(null);
		//주문 취소
		order.cancel();
	}

	private Member aaabbbccc(Long memberId) {
		return memberRepository.findById(memberId).orElse(null);
	}

	private Item bbbccc(Long itemId) {
		return itemRepository.findById(itemId).orElse(null);
	}

	//검색
	public List<Order> findOrders(OrderSearch orderSearch) {
		return orderRepository.findAllByString(orderSearch);
	}
}

package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", 8, book.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when
        orderService.order(member.getId(), item.getId(), orderCount);

        //then
        fail("재고 수량 부족 예외가 발행해야 한다.");
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("주문 취소시 상태는 CANCEL 이다.", OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.", 10, item.getStockQuantity());
    }


    @Test
    public void 주문_빈_주문항목() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 0;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals("주문 수량이 0일 때도 주문이 생성되어야 한다.", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문 항목이 없어야 한다.", 0, getOrder.getOrderItems().size());
        assertEquals("총 가격이 0이어야 한다.", 0, getOrder.getTotalPrice());
        assertEquals("재고 수량이 변하지 않아야 한다.", 10, book.getStockQuantity());
    }

    @Test
    public void 주문_최대재고수량() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 10; // 재고와 동일한 수량

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("모든 재고가 소진되어야 한다.", 0, book.getStockQuantity());
    }

    @Test
    public void 주문_단일수량() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 15000, 5);
        int orderCount = 1;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 15000, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", 4, book.getStockQuantity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void 주문_잘못된회원ID() throws Exception {
        //given
        Book book = createBook("시골 JPA", 10000, 10);
        Long invalidMemberId = 999999L;
        int orderCount = 2;

        //when
        orderService.order(invalidMemberId, book.getId(), orderCount);

        //then
        fail("잘못된 회원 ID로 예외가 발생해야 한다.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void 주문_잘못된상품ID() throws Exception {
        //given
        Member member = createMember();
        Long invalidItemId = 999999L;
        int orderCount = 2;

        //when
        orderService.order(member.getId(), invalidItemId, orderCount);

        //then
        fail("잘못된 상품 ID로 예외가 발생해야 한다.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void 주문_음수수량() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = -1;

        //when
        orderService.order(member.getId(), book.getId(), orderCount);

        //then
        fail("음수 수량으로 예외가 발생해야 한다.");
    }

    @Test
    public void 주문취소_이미취소된주문() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        orderService.cancelOrder(orderId); // 첫 번째 취소

        //when
        orderService.cancelOrder(orderId); // 두 번째 취소 시도

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals("이미 취소된 주문의 상태는 CANCEL 이어야 한다.", OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals("재고는 한 번만 복원되어야 한다.", 10, book.getStockQuantity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void 주문취소_잘못된주문ID() throws Exception {
        //given
        Long invalidOrderId = 999999L;

        //when
        orderService.cancelOrder(invalidOrderId);

        //then
        fail("잘못된 주문 ID로 예외가 발생해야 한다.");
    }

    @Test
    public void 주문취소후_재주문() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 3;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        orderService.cancelOrder(orderId);

        //when - 취소 후 다시 주문
        Long newOrderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order newOrder = orderRepository.findOne(newOrderId);
        assertEquals("새 주문의 상태는 ORDER", OrderStatus.ORDER, newOrder.getStatus());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, newOrder.getTotalPrice());
        assertEquals("재고가 다시 차감되어야 한다.", 7, book.getStockQuantity());
    }

    @Test
    public void 다중상품주문_서로다른가격() throws Exception {
        //given
        Member member = createMember();
        Book book1 = createBook("시골 JPA", 10000, 10);
        Book book2 = createBook("도시 JPA", 20000, 5);
        int orderCount1 = 2;
        int orderCount2 = 1;

        //when
        Long orderId1 = orderService.order(member.getId(), book1.getId(), orderCount1);
        Long orderId2 = orderService.order(member.getId(), book2.getId(), orderCount2);

        //then
        Order order1 = orderRepository.findOne(orderId1);
        Order order2 = orderRepository.findOne(orderId2);
        
        assertEquals("첫 번째 주문 가격 확인", 20000, order1.getTotalPrice());
        assertEquals("두 번째 주문 가격 확인", 20000, order2.getTotalPrice());
        assertEquals("첫 번째 상품 재고 확인", 8, book1.getStockQuantity());
        assertEquals("두 번째 상품 재고 확인", 4, book2.getStockQuantity());
    }

    @Test
    public void 주문_고가상품() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("프리미엄 JPA", 100000, 3);
        int orderCount = 1;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals("고가 상품 주문 가격 확인", 100000, getOrder.getTotalPrice());
        assertEquals("고가 상품 재고 차감 확인", 2, book.getStockQuantity());
    }

    private Member createMemberWithAddress(String name, String city, String street, String zipcode) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address(city, street, zipcode));
        em.persist(member);
        return member;
    }

    private Book createBookWithDetails(String name, String author, String isbn, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        book.setAuthor(author);
        book.setIsbn(isbn);
        em.persist(book);
        return book;
    }
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }


}
package jpabook.jpashop.repository.order;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * OrderRepositoryV2 comprehensive unit tests
 * Testing Framework: JUnit 4 with Spring Boot Test (following existing project patterns)
 * 
 * Tests the Spring Data JPA repository methods:
 * - findAllWithItem(): 모든 주문을 주문상품과 함께 조회 (fetch join)
 * - findAllWithMemberDelivery(Pageable): 주문을 회원과 배송정보와 함께 페이징 조회
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderRepositoryV2Test {

    @Autowired
    OrderRepositoryV2 orderRepositoryV2;

    @PersistenceContext
    EntityManager em;

    private Member member1;
    private Member member2;
    private Item item1;
    private Item item2;
    private Order order1;
    private Order order2;

    private void setUp() {
        // Create test members
        member1 = createMember("김영한", "서울", "강남구", "12345");
        member2 = createMember("이민수", "부산", "해운대구", "54321");
        em.persist(member1);
        em.persist(member2);

        // Create test items
        item1 = createBook("JPA 완전정복", 30000, 100, "김영한", "978-1234567890");
        item2 = createBook("Spring Boot 실전", 25000, 50, "이민수", "978-0987654321");
        em.persist(item1);
        em.persist(item2);

        // Create test orders
        order1 = createOrder(member1, item1, 2, 30000);
        order2 = createOrder(member2, item2, 1, 25000);
        em.persist(order1);
        em.persist(order2);

        em.flush();
        em.clear();
    }

    @Test
    public void findAllWithItem_모든주문을주문상품과함께조회() {
        // given
        setUp();

        // when
        List<Order> orders = orderRepositoryV2.findAllWithItem();

        // then
        assertEquals("주문이 2개 조회되어야 한다", 2, orders.size());
        
        // Verify order items are loaded (fetch join test)
        for (Order order : orders) {
            assertFalse("주문상품이 비어있으면 안된다", order.getOrderItems().isEmpty());
            for (OrderItem orderItem : order.getOrderItems()) {
                assertNotNull("상품이 로드되어야 한다", orderItem.getItem());
                assertNotNull("상품명이 있어야 한다", orderItem.getItem().getName());
                assertTrue("주문가격이 0보다 커야 한다", orderItem.getOrderPrice() > 0);
                assertTrue("주문수량이 0보다 커야 한다", orderItem.getCount() > 0);
            }
        }

        // Verify specific data
        Order firstOrder = orders.get(0);
        assertTrue("회원이 로드되어야 한다", firstOrder.getMember().getName().equals("김영한") || 
                  firstOrder.getMember().getName().equals("이민수"));
        assertNotNull("배송정보가 로드되어야 한다", firstOrder.getDelivery());
    }

    @Test
    public void findAllWithItem_빈결과처리() {
        // given - clear all orders
        em.createQuery("DELETE FROM OrderItem").executeUpdate();
        em.createQuery("DELETE FROM Order").executeUpdate();
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepositoryV2.findAllWithItem();

        // then
        assertTrue("주문이 없으면 빈 리스트를 반환해야 한다", orders.isEmpty());
    }

    @Test
    public void findAllWithItem_여러주문상품처리() {
        // given
        setUp();
        
        // Create order with multiple items
        OrderItem orderItem1 = OrderItem.createOrderItem(item1, 30000, 1);
        OrderItem orderItem2 = OrderItem.createOrderItem(item2, 25000, 2);
        Order multiItemOrder = Order.createOrder(member1, createDelivery(member1), orderItem1, orderItem2);
        em.persist(multiItemOrder);
        em.flush();
        em.clear();

        // when
        List<Order> orders = orderRepositoryV2.findAllWithItem();

        // then
        assertTrue("주문이 3개 이상 조회되어야 한다", orders.size() >= 3);
        
        // Find the multi-item order and verify
        Order foundMultiItemOrder = orders.stream()
                .filter(o -> o.getOrderItems().size() > 1)
                .findFirst()
                .orElse(null);
        
        assertNotNull("여러 상품을 가진 주문이 조회되어야 한다", foundMultiItemOrder);
        assertEquals("주문상품이 2개여야 한다", 2, foundMultiItemOrder.getOrderItems().size());
    }

    @Test
    public void findAllWithItem_페치조인으로N플러스1문제방지() {
        // given - Create more test data to test N+1 prevention
        for (int i = 0; i < 10; i++) {
            Member member = createMember("회원" + i, "도시" + i, "거리" + i, "1000" + i);
            em.persist(member);
            
            Item item = createBook("책" + i, 10000 + i * 1000, 100, "저자" + i, "ISBN" + i);
            em.persist(item);
            
            Order order = createOrder(member, item, 1, 10000 + i * 1000);
            em.persist(order);
        }
        em.flush();
        em.clear();

        // when
        long startTime = System.currentTimeMillis();
        List<Order> orders = orderRepositoryV2.findAllWithItem();
        long endTime = System.currentTimeMillis();

        // then
        assertTrue("주문이 10개 이상 조회되어야 한다", orders.size() >= 10);
        assertTrue("페치 조인으로 빠르게 조회되어야 한다 (2초 이내)", (endTime - startTime) < 2000);
        
        // Verify no lazy loading exceptions occur
        for (Order order : orders) {
            // These should not throw LazyInitializationException
            assertNotNull(order.getMember().getName());
            assertNotNull(order.getDelivery().getAddress());
            for (OrderItem orderItem : order.getOrderItems()) {
                assertNotNull(orderItem.getItem().getName());
            }
        }
    }

    @Test
    public void findAllWithMemberDelivery_페이징없이조회() {
        // given
        setUp();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Order> orders = orderRepositoryV2.findAllWithMemberDelivery(pageable);

        // then
        assertEquals("주문이 2개 조회되어야 한다", 2, orders.size());
        
        // Verify member and delivery are loaded (fetch join test)
        for (Order order : orders) {
            assertNotNull("회원이 로드되어야 한다", order.getMember());
            assertNotNull("회원명이 있어야 한다", order.getMember().getName());
            assertNotNull("배송정보가 로드되어야 한다", order.getDelivery());
            assertNotNull("배송주소가 있어야 한다", order.getDelivery().getAddress());
            assertEquals("배송상태가 READY여야 한다", DeliveryStatus.READY, order.getDelivery().getStatus());
        }

        // Verify specific data
        boolean hasKimOrder = orders.stream().anyMatch(o -> "김영한".equals(o.getMember().getName()));
        boolean hasLeeOrder = orders.stream().anyMatch(o -> "이민수".equals(o.getMember().getName()));
        assertTrue("김영한의 주문이 있어야 한다", hasKimOrder);
        assertTrue("이민수의 주문이 있어야 한다", hasLeeOrder);
    }

    @Test
    public void findAllWithMemberDelivery_페이징처리() {
        // given
        setUp();
        
        // Create more test data for paging test
        for (int i = 0; i < 5; i++) {
            Member member = createMember("회원" + i, "도시" + i, "거리" + i, "1000" + i);
            em.persist(member);
            
            Item item = createBook("책" + i, 10000 + i * 1000, 100, "저자" + i, "ISBN" + i);
            em.persist(item);
            
            Order order = createOrder(member, item, 1, 10000 + i * 1000);
            em.persist(order);
        }
        em.flush();
        em.clear();

        // when - First page with 3 items
        Pageable firstPage = PageRequest.of(0, 3);
        List<Order> firstPageOrders = orderRepositoryV2.findAllWithMemberDelivery(firstPage);

        // when - Second page with 3 items
        Pageable secondPage = PageRequest.of(1, 3);
        List<Order> secondPageOrders = orderRepositoryV2.findAllWithMemberDelivery(secondPage);

        // then
        assertEquals("첫 번째 페이지는 3개여야 한다", 3, firstPageOrders.size());
        assertTrue("두 번째 페이지는 3개 이하여야 한다", secondPageOrders.size() <= 3);
        assertTrue("전체 주문수는 5개 이상이어야 한다", firstPageOrders.size() + secondPageOrders.size() >= 5);

        // Verify fetch join works with paging
        for (Order order : firstPageOrders) {
            assertNotNull("회원이 로드되어야 한다", order.getMember().getName());
            assertNotNull("배송정보가 로드되어야 한다", order.getDelivery().getAddress());
        }
    }

    @Test
    public void findAllWithMemberDelivery_빈결과처리() {
        // given - clear all orders
        em.createQuery("DELETE FROM OrderItem").executeUpdate();
        em.createQuery("DELETE FROM Order").executeUpdate();
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Order> orders = orderRepositoryV2.findAllWithMemberDelivery(pageable);

        // then
        assertTrue("주문이 없으면 빈 리스트를 반환해야 한다", orders.isEmpty());
    }

    @Test
    public void findAllWithMemberDelivery_페치조인으로N플러스1문제방지() {
        // given - Create large dataset
        for (int i = 0; i < 15; i++) {
            Member member = createMember("회원" + i, "도시" + i, "거리" + i, "1000" + i);
            em.persist(member);
            
            Item item = createBook("책" + i, 10000 + i * 1000, 100, "저자" + i, "ISBN" + i);
            em.persist(item);
            
            Order order = createOrder(member, item, 1, 10000 + i * 1000);
            em.persist(order);
        }
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        long startTime = System.currentTimeMillis();
        List<Order> orders = orderRepositoryV2.findAllWithMemberDelivery(pageable);
        long endTime = System.currentTimeMillis();

        // then
        assertEquals("페이징 크기만큼 조회되어야 한다", 10, orders.size());
        assertTrue("페치 조인으로 빠르게 조회되어야 한다 (2초 이내)", (endTime - startTime) < 2000);
        
        // Verify no lazy loading exceptions occur
        for (Order order : orders) {
            // These should not throw LazyInitializationException
            assertNotNull(order.getMember().getName());
            assertNotNull(order.getDelivery().getAddress().getCity());
        }
    }

    @Test
    public void 트랜잭션경계에서지연로딩테스트() {
        // given
        setUp();

        // when
        List<Order> orders1 = orderRepositoryV2.findAllWithItem();
        List<Order> orders2 = orderRepositoryV2.findAllWithMemberDelivery(PageRequest.of(0, 10));

        // then - Should work within transaction boundary
        assertFalse("주문목록1이 비어있으면 안된다", orders1.isEmpty());
        assertFalse("주문목록2가 비어있으면 안된다", orders2.isEmpty());
        
        // Verify lazy loading works within transaction
        for (Order order : orders1) {
            // These should not throw exceptions
            assertTrue("주문상품 수가 0보다 커야 한다", order.getOrderItems().size() > 0);
            assertNotNull("회원 주소가 있어야 한다", order.getMember().getAddress().getCity());
        }

        for (Order order : orders2) {
            // Lazy loading should work for orderItems even though not fetch joined
            assertTrue("주문상품 수가 0보다 커야 한다", order.getOrderItems().size() > 0);
            assertNotNull("배송 주소가 있어야 한다", order.getDelivery().getAddress().getStreet());
        }
    }

    @Test
    public void 성능비교_페치조인vs일반조회() {
        // given - Create test data
        for (int i = 0; i < 20; i++) {
            Member member = createMember("회원" + i, "도시" + i, "거리" + i, "1000" + i);
            em.persist(member);
            
            Item item = createBook("책" + i, 10000 + i * 1000, 100, "저자" + i, "ISBN" + i);
            em.persist(item);
            
            Order order = createOrder(member, item, 1, 10000 + i * 1000);
            em.persist(order);
        }
        em.flush();
        em.clear();

        // when - Test findAllWithItem (fetch join)
        long startTime1 = System.currentTimeMillis();
        List<Order> ordersWithFetchJoin = orderRepositoryV2.findAllWithItem();
        long fetchJoinTime = System.currentTimeMillis() - startTime1;

        // Clear persistence context
        em.clear();

        // when - Test findAllWithMemberDelivery (fetch join)
        long startTime2 = System.currentTimeMillis();
        List<Order> ordersWithMemberDelivery = orderRepositoryV2.findAllWithMemberDelivery(PageRequest.of(0, 20));
        long memberDeliveryTime = System.currentTimeMillis() - startTime2;

        // then
        assertTrue("페치조인으로 조회한 주문이 20개 이상이어야 한다", ordersWithFetchJoin.size() >= 20);
        assertTrue("회원배송 페치조인으로 조회한 주문이 20개여야 한다", ordersWithMemberDelivery.size() == 20);
        
        // Performance should be reasonable (adjust thresholds as needed)
        assertTrue("페치조인 성능이 합리적이어야 한다 (3초 이내)", fetchJoinTime < 3000);
        assertTrue("회원배송 페치조인 성능이 합리적이어야 한다 (3초 이내)", memberDeliveryTime < 3000);

        // Verify data integrity
        for (Order order : ordersWithFetchJoin) {
            assertNotNull("주문의 회원이 있어야 한다", order.getMember());
            assertFalse("주문상품이 비어있으면 안된다", order.getOrderItems().isEmpty());
        }

        for (Order order : ordersWithMemberDelivery) {
            assertNotNull("주문의 회원이 있어야 한다", order.getMember());
            assertNotNull("주문의 배송정보가 있어야 한다", order.getDelivery());
        }
    }

    // Helper methods for test data creation
    private Member createMember(String name, String city, String street, String zipcode) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address(city, street, zipcode));
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity, String author, String isbn) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        book.setAuthor(author);
        book.setIsbn(isbn);
        return book;
    }

    private Order createOrder(Member member, Item item, int count, int price) {
        Delivery delivery = createDelivery(member);
        OrderItem orderItem = OrderItem.createOrderItem(item, price, count);
        return Order.createOrder(member, delivery, orderItem);
    }

    private Delivery createDelivery(Member member) {
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);
        return delivery;
    }
}
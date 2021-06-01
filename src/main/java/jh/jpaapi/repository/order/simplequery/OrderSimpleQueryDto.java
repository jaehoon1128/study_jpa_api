package jh.jpaapi.repository.order.simplequery;

import jh.jpaapi.doamin.Address;
import jh.jpaapi.doamin.Order;
import jh.jpaapi.doamin.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

//컨트롤러가 reposiotry에 의존관계가 생기면 안되기때문 따로 생성
@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    //DTO가 Entity를 받는건 중요하지않음
    //중요하지않는 곳(dto)에 중요한것(entity)을 받기때문
    public OrderSimpleQueryDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName();
        orderDate = order.getOrderDate();
        orderStatus = order.getStatus();
        address = order.getDelivery().getAddress();
    }

    public OrderSimpleQueryDto(Long id, String name, LocalDateTime orderDate, OrderStatus status, Address address) {
        orderId = id;
        this.name = name;
        this.orderDate = orderDate;
        orderStatus = status;
        this.address = address;
    }
}

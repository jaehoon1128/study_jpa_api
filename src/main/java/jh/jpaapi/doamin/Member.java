package jh.jpaapi.doamin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    //@NotEmpty
    private String name;

    @Embedded
    private Address address;

    @JsonIgnore     //양방항중 둘중 한곳은 막아놔야함(무한루프때문에..) 반대쪽
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}

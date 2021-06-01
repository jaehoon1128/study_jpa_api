package jh.jpaapi;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpaapiApplication {
	public static void main(String[] args) {
		SpringApplication.run(JpaapiApplication.class, args);
	}

	/**
	@GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1(){...} 에서 Proxy 객체 에러나는 부분 하기위해 등록
	 */
	@Bean
	Hibernate5Module hibernate5Module(){
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		//다음 코드 추가시 LAZY 로딩하여 다 가져옴
		//Lazy 로딩때문에 다음과 같이 사용하면안됨...
		//hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}
}
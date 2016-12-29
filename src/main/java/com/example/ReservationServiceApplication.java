package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.LongSummaryStatistics;
import java.util.stream.Stream;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}

@RestController
class MessageController {
	private String message;
	@Autowired
	MessageController(@Value("${message}") String message) {
		this.message = message;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/message")
	public String getMessage() {
		return message;
	}

}

@Component
class SampleDataCLR implements CommandLineRunner {
	private final ReservationRepository reservationRepository;

	@Autowired
	public SampleDataCLR(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}


	@Override
	public void run(String... args) throws Exception {
		Stream.of("ABC", "DEF", "LMN").forEach(name -> reservationRepository.save(new Reservation(name)));
		reservationRepository.findAll().stream().forEach(System.out::println);
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

}

@Entity
class Reservation {
	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", reservationName='" + reservationName + '\'' +
				'}';
	}

	public Reservation() { // JPA
	}

	public Reservation(String reservationName) {
		this.id = id;
		this.reservationName = reservationName;
	}

	@Id

	@GeneratedValue
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}

	private String reservationName;
}

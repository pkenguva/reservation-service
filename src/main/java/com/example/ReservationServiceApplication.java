package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.governator.annotations.binding.Input;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.LongSummaryStatistics;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.Duration.between;
import static java.time.Instant.now;

@EnableBinding (ReservationChannels.class)
@SpringBootApplication
@EnableDiscoveryClient
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

}

interface ReservationChannels {
	@Input
	SubscribableChannel input();
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

@MessageEndpoint
@Log4j
class ReservationProcessor {

	private static final String UTF_8 = "UTF-8";

	private final ReservationRepository reservationRepository;

	@Autowired
	ReservationProcessor(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@RabbitListener(queues = "reservations")
	public void receiveMessage(byte[] message) throws Exception {
		log.info("Processing the message " + message);
		reservationRepository.save(new Reservation(getReadableMessage(message)));
	}

	private String getReadableMessage(byte[] message) throws Exception {
		try {
			return new String(message, UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new Exception("Could not read message", e);
		}
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
		return "Reservation {" +
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

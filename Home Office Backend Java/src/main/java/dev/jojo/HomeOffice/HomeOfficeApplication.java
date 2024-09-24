package dev.jojo.HomeOffice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomeOfficeApplication implements CommandLineRunner{

	public static void main(String[] args)  {
		SpringApplication.run(HomeOfficeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Make the gRPC call when the Spring Boot server starts
		GrpcClient.makeRpcCall();
	}
}

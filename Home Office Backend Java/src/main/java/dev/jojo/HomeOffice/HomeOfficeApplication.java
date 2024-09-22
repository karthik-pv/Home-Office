package dev.jojo.HomeOffice;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import dev.jojo.HomeOffice.GetNAVGrpc;
import dev.jojo.HomeOffice.GetNAVOuterClass;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.annotation.PostConstruct;

@SpringBootApplication
public class HomeOfficeApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeOfficeApplication.class, args);
	}

	@PostConstruct
	public void runGrpcClient() {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 3020)
				.usePlaintext()
				.build();

		System.out.println("Attempting to connect to gRPC server...");

		GetNAVGrpc.GetNAVBlockingStub stub = GetNAVGrpc.newBlockingStub(channel);

		try {
			GetNAVOuterClass.Request request = GetNAVOuterClass.Request.newBuilder()
					.setQuery("Test Query")
					.build();

			System.out.println("Sending request to gRPC server...");
			GetNAVOuterClass.Response response = stub.getData(request);
			System.out.println("Response from Python server: " + response.getResult());

		} catch (StatusRuntimeException e) {
			System.err.println("RPC failed: " + e.getStatus());
			e.printStackTrace(); // Print the full stack trace for debugging
		} finally {
			channel.shutdown();
		}
	}

}

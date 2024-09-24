package dev.jojo.HomeOffice;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {

    // This method will be called from Spring Boot's main application class
    public static void makeRpcCall() {
        // Create a channel to the gRPC server
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051) // Ensure that the host and port match your server
                .usePlaintext() // Disable TLS for local testing
                .build();

        // Create a blocking stub (synchronous)
        get_navGrpc.get_navBlockingStub stub = get_navGrpc.newBlockingStub(channel);

        // Prepare the request object
        GetNav.Request request = GetNav.Request.newBuilder()
                .setQuery("foo") // Set the query parameter appropriately
                .build();

        // Call the GetData RPC method and get the response
        try {
            GetNav.Response response = stub.getData(request);
            System.out.println("Response from server: " + response.getResult());
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        } finally {
            // Always shutdown the channel when you're done
            channel.shutdown();
        }
    }
}

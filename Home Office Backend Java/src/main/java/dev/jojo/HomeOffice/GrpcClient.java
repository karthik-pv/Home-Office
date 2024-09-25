package dev.jojo.HomeOffice;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {

    public static Double makeRpcCall(String fundName) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        get_navGrpc.get_navBlockingStub stub = get_navGrpc.newBlockingStub(channel);

        GetNav.Request request = GetNav.Request.newBuilder()
                .setQuery(fundName)
                .build();

        try {
            GetNav.Response response = stub.getData(request);
            System.out.println("Response from server: " + response.getResult());
            return response.getResult();
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
            return null;
        } finally {
            channel.shutdown();
        }
    }
}

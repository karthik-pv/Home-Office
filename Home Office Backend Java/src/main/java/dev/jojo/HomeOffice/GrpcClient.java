package dev.jojo.HomeOffice;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import dev.jojo.HomeOffice.GetNAVGrpc;
import dev.jojo.HomeOffice.GetNAVOuterClass;

public class GrpcClient {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6969)
                .usePlaintext()
                .build();

        GetNAVGrpc.GetNAVBlockingStub stub = GetNAVGrpc.newBlockingStub(channel);

        try {

            GetNAVOuterClass.Request request = GetNAVOuterClass.Request.newBuilder()
                    .setQuery("Test Query")
                    .build();

            GetNAVOuterClass.Response response = stub.getData(request);


            System.out.println("Response from Python server: " + response.getResult());

        } catch (StatusRuntimeException e) {

            System.err.println("RPC failed: " + e.getStatus());
        } finally {

            channel.shutdown();
        }
    }
}
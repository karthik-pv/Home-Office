package dev.jojo.HomeOffice;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Service definition for GetNAV
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.4.0)",
    comments = "Source: get_nav.proto")
public final class get_navGrpc {

  private get_navGrpc() {}

  public static final String SERVICE_NAME = "get_nav";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<GetNav.Request,
      GetNav.Response> METHOD_GET_DATA =
      io.grpc.MethodDescriptor.<GetNav.Request, GetNav.Response>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "get_nav", "GetData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              GetNav.Request.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              GetNav.Response.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static get_navStub newStub(io.grpc.Channel channel) {
    return new get_navStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static get_navBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new get_navBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static get_navFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new get_navFutureStub(channel);
  }

  /**
   * <pre>
   * Service definition for GetNAV
   * </pre>
   */
  public static abstract class get_navImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * RPC method to get data
     * </pre>
     */
    public void getData(GetNav.Request request,
                        io.grpc.stub.StreamObserver<GetNav.Response> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_DATA, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_GET_DATA,
            asyncUnaryCall(
              new MethodHandlers<
                GetNav.Request,
                GetNav.Response>(
                  this, METHODID_GET_DATA)))
          .build();
    }
  }

  /**
   * <pre>
   * Service definition for GetNAV
   * </pre>
   */
  public static final class get_navStub extends io.grpc.stub.AbstractStub<get_navStub> {
    private get_navStub(io.grpc.Channel channel) {
      super(channel);
    }

    private get_navStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected get_navStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new get_navStub(channel, callOptions);
    }

    /**
     * <pre>
     * RPC method to get data
     * </pre>
     */
    public void getData(GetNav.Request request,
                        io.grpc.stub.StreamObserver<GetNav.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_DATA, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Service definition for GetNAV
   * </pre>
   */
  public static final class get_navBlockingStub extends io.grpc.stub.AbstractStub<get_navBlockingStub> {
    private get_navBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private get_navBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected get_navBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new get_navBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * RPC method to get data
     * </pre>
     */
    public GetNav.Response getData(GetNav.Request request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_DATA, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Service definition for GetNAV
   * </pre>
   */
  public static final class get_navFutureStub extends io.grpc.stub.AbstractStub<get_navFutureStub> {
    private get_navFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private get_navFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected get_navFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new get_navFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * RPC method to get data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<GetNav.Response> getData(
        GetNav.Request request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_DATA, getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_DATA = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final get_navImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(get_navImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DATA:
          serviceImpl.getData((GetNav.Request) request,
              (io.grpc.stub.StreamObserver<GetNav.Response>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class get_navDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return GetNav.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (get_navGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new get_navDescriptorSupplier())
              .addMethod(METHOD_GET_DATA)
              .build();
        }
      }
    }
    return result;
  }
}

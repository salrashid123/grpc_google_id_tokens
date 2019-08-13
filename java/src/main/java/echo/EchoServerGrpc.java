package echo;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.22.1)",
    comments = "Source: echo.proto")
public final class EchoServerGrpc {

  private EchoServerGrpc() {}

  public static final String SERVICE_NAME = "echo.EchoServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<echo.Echo.EchoRequest,
      echo.Echo.EchoReply> getSayHelloMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SayHello",
      requestType = echo.Echo.EchoRequest.class,
      responseType = echo.Echo.EchoReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<echo.Echo.EchoRequest,
      echo.Echo.EchoReply> getSayHelloMethod() {
    io.grpc.MethodDescriptor<echo.Echo.EchoRequest, echo.Echo.EchoReply> getSayHelloMethod;
    if ((getSayHelloMethod = EchoServerGrpc.getSayHelloMethod) == null) {
      synchronized (EchoServerGrpc.class) {
        if ((getSayHelloMethod = EchoServerGrpc.getSayHelloMethod) == null) {
          EchoServerGrpc.getSayHelloMethod = getSayHelloMethod = 
              io.grpc.MethodDescriptor.<echo.Echo.EchoRequest, echo.Echo.EchoReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "echo.EchoServer", "SayHello"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  echo.Echo.EchoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  echo.Echo.EchoReply.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getSayHelloMethod;
  }

  private static volatile io.grpc.MethodDescriptor<echo.Echo.EchoRequest,
      echo.Echo.EchoReply> getSayHelloStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SayHelloStream",
      requestType = echo.Echo.EchoRequest.class,
      responseType = echo.Echo.EchoReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<echo.Echo.EchoRequest,
      echo.Echo.EchoReply> getSayHelloStreamMethod() {
    io.grpc.MethodDescriptor<echo.Echo.EchoRequest, echo.Echo.EchoReply> getSayHelloStreamMethod;
    if ((getSayHelloStreamMethod = EchoServerGrpc.getSayHelloStreamMethod) == null) {
      synchronized (EchoServerGrpc.class) {
        if ((getSayHelloStreamMethod = EchoServerGrpc.getSayHelloStreamMethod) == null) {
          EchoServerGrpc.getSayHelloStreamMethod = getSayHelloStreamMethod = 
              io.grpc.MethodDescriptor.<echo.Echo.EchoRequest, echo.Echo.EchoReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "echo.EchoServer", "SayHelloStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  echo.Echo.EchoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  echo.Echo.EchoReply.getDefaultInstance()))
                  .build();
          }
        }
     }
     return getSayHelloStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EchoServerStub newStub(io.grpc.Channel channel) {
    return new EchoServerStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EchoServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new EchoServerBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EchoServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new EchoServerFutureStub(channel);
  }

  /**
   */
  public static abstract class EchoServerImplBase implements io.grpc.BindableService {

    /**
     */
    public void sayHello(echo.Echo.EchoRequest request,
        io.grpc.stub.StreamObserver<echo.Echo.EchoReply> responseObserver) {
      asyncUnimplementedUnaryCall(getSayHelloMethod(), responseObserver);
    }

    /**
     */
    public void sayHelloStream(echo.Echo.EchoRequest request,
        io.grpc.stub.StreamObserver<echo.Echo.EchoReply> responseObserver) {
      asyncUnimplementedUnaryCall(getSayHelloStreamMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSayHelloMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                echo.Echo.EchoRequest,
                echo.Echo.EchoReply>(
                  this, METHODID_SAY_HELLO)))
          .addMethod(
            getSayHelloStreamMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                echo.Echo.EchoRequest,
                echo.Echo.EchoReply>(
                  this, METHODID_SAY_HELLO_STREAM)))
          .build();
    }
  }

  /**
   */
  public static final class EchoServerStub extends io.grpc.stub.AbstractStub<EchoServerStub> {
    private EchoServerStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EchoServerStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EchoServerStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EchoServerStub(channel, callOptions);
    }

    /**
     */
    public void sayHello(echo.Echo.EchoRequest request,
        io.grpc.stub.StreamObserver<echo.Echo.EchoReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sayHelloStream(echo.Echo.EchoRequest request,
        io.grpc.stub.StreamObserver<echo.Echo.EchoReply> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getSayHelloStreamMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class EchoServerBlockingStub extends io.grpc.stub.AbstractStub<EchoServerBlockingStub> {
    private EchoServerBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EchoServerBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EchoServerBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EchoServerBlockingStub(channel, callOptions);
    }

    /**
     */
    public echo.Echo.EchoReply sayHello(echo.Echo.EchoRequest request) {
      return blockingUnaryCall(
          getChannel(), getSayHelloMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<echo.Echo.EchoReply> sayHelloStream(
        echo.Echo.EchoRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getSayHelloStreamMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class EchoServerFutureStub extends io.grpc.stub.AbstractStub<EchoServerFutureStub> {
    private EchoServerFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EchoServerFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EchoServerFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EchoServerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<echo.Echo.EchoReply> sayHello(
        echo.Echo.EchoRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SAY_HELLO = 0;
  private static final int METHODID_SAY_HELLO_STREAM = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EchoServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EchoServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SAY_HELLO:
          serviceImpl.sayHello((echo.Echo.EchoRequest) request,
              (io.grpc.stub.StreamObserver<echo.Echo.EchoReply>) responseObserver);
          break;
        case METHODID_SAY_HELLO_STREAM:
          serviceImpl.sayHelloStream((echo.Echo.EchoRequest) request,
              (io.grpc.stub.StreamObserver<echo.Echo.EchoReply>) responseObserver);
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

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (EchoServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getSayHelloMethod())
              .addMethod(getSayHelloStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}

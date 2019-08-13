
package com.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.io.File;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.GrpcSslContexts;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.IdTokenCredentials;

import echo.Echo.EchoReply;
import echo.Echo.EchoRequest;
import echo.EchoServerGrpc;
import echo.EchoServerGrpc.EchoServerBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.auth.MoreCallCredentials;

public class TestApp {

     public static void main(String[] args) throws InterruptedException {

          try {

               boolean usetls = true;

               String address = "localhost";
               String targetAudience = "https://foo.bar";
               String certificateFile = "../certs/CA_crt.pem";

               int port = 8080;
               String sniServerName = "server.domain.com";
               
               String svcAccountFile = "/path/to/svc_account.json";

               // ServiceAccountCredentials
               ServiceAccountCredentials saCreds = ServiceAccountCredentials
                         .fromStream(new FileInputStream(svcAccountFile));
               saCreds = (ServiceAccountCredentials) saCreds
                         .createScoped(Arrays.asList("https://www.googleapis.com/auth/userinfo.email"));
               IdTokenCredentials tokenCredential = IdTokenCredentials.newBuilder().setIdTokenProvider(saCreds)
                         .setTargetAudience(targetAudience).build();

               ManagedChannel ch;
               if (usetls) {
                    //ch = ManagedChannelBuilder.forAddress(address, port).useTransportSecurity().build();
                    ch = NettyChannelBuilder.forAddress("localhost", 8080)
                    .overrideAuthority(sniServerName)
                    .sslContext(GrpcSslContexts.forClient().trustManager(new File(certificateFile)).build())
                    .build();

               } else {

                    ch = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build();
               }
               EchoRequest req = EchoRequest.newBuilder().setName("sal").build();

               EchoServerBlockingStub blockingStub = EchoServerGrpc.newBlockingStub(ch)
                         .withCallCredentials(MoreCallCredentials.from(tokenCredential));

               EchoReply resp = blockingStub.sayHello(req);

               System.out.println(resp);

          } catch (IOException ioex) {
               System.out.println("IOException " + ioex);
          }

     }

}

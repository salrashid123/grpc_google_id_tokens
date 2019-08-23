package main

import (
	"flag"
	"log"
	"net"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"echo"

	oidc "github.com/coreos/go-oidc"
	sal "github.com/salrashid123/oauth2/google"
	"golang.org/x/net/context"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
)

var (
	grpcport       = flag.String("grpcport", ":8080", "grpcport")
	usetls         = flag.Bool("usetls", false, "startup using TLS")
	serverCert     = flag.String("cert", "server_crt.pem", "Server TLS cert")
	serverKey      = flag.String("key", "server_key.pem", "Server TLS key")
	targetAudience = flag.String("targetAudience", "https://grpc.domain.com", "OIDC audience to check")
	validateToken  = flag.Bool("validateToken", false, "validateToken field")
)

const ()

type server struct {
}

type contextKey string

func authUnaryInterceptor(
	ctx context.Context,
	req interface{},
	info *grpc.UnaryServerInfo,
	handler grpc.UnaryHandler,
) (interface{}, error) {
	md, _ := metadata.FromIncomingContext(ctx)
	if len(md["authorization"]) > 0 {
		reqToken := md["authorization"][0]
		splitToken := strings.Split(reqToken, "Bearer")
		reqToken = strings.TrimSpace(splitToken[1])
		uid, err := verifyToken(ctx, reqToken, *targetAudience)
		if err != nil {
			return nil, grpc.Errorf(codes.Unauthenticated, "authentication required")
		}
		newCtx := context.WithValue(ctx, contextKey("uid"), uid)
		return handler(newCtx, req)
	}
	return nil, grpc.Errorf(codes.Unauthenticated, "Authorization header not provided")

}

func verifyToken(ctx context.Context, token string, targetAudience string) (*oidc.IDToken, error) {
	idt, err := sal.VerifyGoogleIDToken(ctx, token, targetAudience)
	if err != nil {
		return nil, err
	}
	log.Printf("Token Verified with Audience: %v  and Subject %v\n", idt.Audience, idt.Subject)
	return idt, nil
}

func (s *server) SayHelloStream(in *echo.EchoRequest, stream echo.EchoServer_SayHelloStreamServer) error {

	log.Println("Got stream:  -->  ")
	ctx := stream.Context()

	var respmdheader = metadata.MD{
		"streamheaderkey": []string{"val"},
	}
	if err := grpc.SendHeader(ctx, respmdheader); err != nil {
		log.Fatalf("grpc.SendHeader(%v, %v) = %v, want %v", ctx, respmdheader, err, nil)
	}

	stream.Send(&echo.EchoReply{Message: "Msg1 " + in.Name})
	stream.Send(&echo.EchoReply{Message: "Msg2 " + in.Name})

	var respmdfooter = metadata.MD{
		"streamtrailerkey": []string{"val"},
	}
	grpc.SetTrailer(ctx, respmdfooter)

	return nil
}

func (s *server) SayHello(ctx context.Context, in *echo.EchoRequest) (*echo.EchoReply, error) {

	log.Println("Got rpc: --> ", in.Name)

	var respmdheader = metadata.MD{
		"rpcheaderkey": []string{"val"},
	}
	if err := grpc.SendHeader(ctx, respmdheader); err != nil {
		log.Fatalf("grpc.SendHeader(%v, %v) = %v, want %v", ctx, respmdheader, err, nil)
	}
	var respmdfooter = metadata.MD{
		"rpctrailerkey": []string{"val"},
	}
	grpc.SetTrailer(ctx, respmdfooter)

	var h, err = os.Hostname()
	if err != nil {
		log.Fatalf("Unable to get hostname %v", err)
	}
	return &echo.EchoReply{Message: "Hello " + in.Name + "  from hostname " + h}, nil
}

func main() {

	flag.Parse()

	lis, err := net.Listen("tcp", *grpcport)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	sopts := []grpc.ServerOption{grpc.MaxConcurrentStreams(10)}

	if *usetls {
		ce, err := credentials.NewServerTLSFromFile(*serverCert, *serverKey)
		if err != nil {
			log.Fatalf("Failed to generate credentials %v", err)
		}
		log.Printf("Starting gRPC server with TLS")
		sopts = append(sopts, grpc.Creds(ce))
	}

	if *validateToken {
		sopts = append(sopts, grpc.UnaryInterceptor(authUnaryInterceptor))
		sopts = append(sopts)
	}

	s := grpc.NewServer(sopts...)

	echo.RegisterEchoServerServer(s, &server{})

	log.Println("Starting gRPC server on port :8080")

	var gracefulStop = make(chan os.Signal)
	signal.Notify(gracefulStop, syscall.SIGTERM)
	signal.Notify(gracefulStop, syscall.SIGINT)
	go func() {
		sig := <-gracefulStop
		log.Printf("caught sig: %+v", sig)
		log.Println("Wait for 1 second to finish processing")
		time.Sleep(1 * time.Second)
		os.Exit(0)
	}()
	s.Serve(lis)
}

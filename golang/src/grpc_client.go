package main

import (
	"crypto/tls"
	"crypto/x509"
	pb "echo"
	"flag"
	"io"
	"io/ioutil"
	"log"
	"time"

	sal "github.com/salrashid123/oauth2/google"
	"golang.org/x/net/context"
	"golang.org/x/oauth2/google"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
)

const ()

var (
	address        = flag.String("address", "localhost:8080", "host:port of gRPC server")
	usetls         = flag.Bool("usetls", false, "startup using TLS")
	targetAudience = flag.String("audience", "https://foo.bar", " audience for the token")
	cacert         = flag.String("cacert", "", "root CA Certificate for TLS")
	sniServerName  = flag.String("servername", "grpc.domain.com", "SNIServer Name assocaited with the server")
	serviceAccount = flag.String("serviceAccount", "svc_account.json", "Path to the service account JSOn file")
)

func main() {

	flag.Parse()

	ctx := context.Background()

	scopes := "https://www.googleapis.com/auth/userinfo.email"
	data, err := ioutil.ReadFile(*serviceAccount)
	if err != nil {
		log.Fatal(err)
	}
	creds, err := google.CredentialsFromJSON(ctx, data, scopes)
	if err != nil {
		log.Fatal(err)
	}
	idTokenSource, err := sal.IdTokenSource(
		sal.IdTokenConfig{
			Credentials: creds,
			Audiences:   []string{*targetAudience},
		},
	)

	rpcCreds, err := sal.NewIDTokenRPCCredential(ctx, idTokenSource)
	if err != nil {
		log.Fatal(err)
	}

	var conn *grpc.ClientConn
	if !*usetls {
		conn, err = grpc.Dial(*address, grpc.WithInsecure())
		if err != nil {
			log.Fatalf("did not connect: %v", err)
		}
	} else {
		var tlsCfg tls.Config
		if len(*cacert) > 0 {
			rootCAs := x509.NewCertPool()
			pem, err := ioutil.ReadFile(*cacert)
			if err != nil {
				log.Fatalf("failed to load root CA certificates  error=%v", err)
			}
			if !rootCAs.AppendCertsFromPEM(pem) {
				log.Fatalf("no root CA certs parsed from file ")
			}
			tlsCfg.RootCAs = rootCAs
		}
		tlsCfg.ServerName = *sniServerName

		ce := credentials.NewTLS(&tlsCfg)
		conn, err = grpc.Dial(*address,
			grpc.WithTransportCredentials(ce),
			grpc.WithPerRPCCredentials(rpcCreds))
		if err != nil {
			log.Fatalf("did not connect: %v", err)
		}
	}
	defer conn.Close()

	c := pb.NewEchoServerClient(conn)

	var testMetadata = metadata.MD{
		"sal":  []string{"value1"},
		"key2": []string{"value2"},
	}

	ctx = metadata.NewOutgoingContext(context.Background(), testMetadata)

	var header, trailer metadata.MD

	for i := 0; i < 3; i++ {
		r, err := c.SayHello(ctx, &pb.EchoRequest{Name: "unary RPC msg "}, grpc.Header(&header), grpc.Trailer(&trailer))
		if err != nil {
			log.Fatalf("could not greet: %v", err)
		}
		time.Sleep(1 * time.Second)
		log.Printf("RPC Response: %v %v", i, r)
	}

	stream, err := c.SayHelloStream(ctx, &pb.EchoRequest{Name: "Stream RPC msg"}, grpc.Header(&header))
	if err != nil {
		log.Fatalf("SayHelloStream(_) = _, %v", err)
	}
	for {
		m, err := stream.Recv()
		if err == io.EOF {
			t := stream.Trailer()
			log.Println("Stream Trailer: ", t)
			break
		}
		if err != nil {
			log.Fatalf("SayHelloStream(_) = _, %v", err)
		}

		h, err := stream.Header()
		if err != nil {
			log.Fatalf("stream.Header error _, %v", err)
		}
		log.Printf("Stream Header: ", h)
		log.Printf("Message: ", m.Message)

	}

}

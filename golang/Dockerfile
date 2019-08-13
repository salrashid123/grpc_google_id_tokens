FROM golang:1.11 as build

RUN apt-get update -y && apt-get install -y build-essential wget unzip curl

RUN curl -OL https://github.com/google/protobuf/releases/download/v3.2.0/protoc-3.2.0-linux-x86_64.zip && \
    unzip protoc-3.2.0-linux-x86_64.zip -d protoc3 && \
    mv protoc3/bin/* /usr/local/bin/ && \
    mv protoc3/include/* /usr/local/include/

WORKDIR /go/

RUN go get github.com/golang/protobuf/proto \
           github.com/golang/protobuf/protoc-gen-go \
           golang.org/x/net/context \
           google.golang.org/grpc \
           google.golang.org/grpc/credentials \
           golang.org/x/oauth2 \
           cloud.google.com/go/compute/metadata \
           github.com/salrashid123/oauth2/google \
           golang.org/x/net/http2
ADD . /go/

RUN wget https://raw.githubusercontent.com/salrashid123/gcegrpc/master/certs/server_crt.pem -P /go/ && \
    wget https://raw.githubusercontent.com/salrashid123/gcegrpc/master/certs/server_key.pem -P /go/ && \
    wget https://raw.githubusercontent.com/salrashid123/gcegrpc/master/certs/CA_crt.pem -P /go/

RUN protoc --go_out=plugins=grpc:. src/echo/echo.proto
RUN export GOBIN=/go/bin && go install src/grpc_server.go

FROM gcr.io/distroless/base
COPY --from=build /go/server_crt.pem /
COPY --from=build /go/server_key.pem /
COPY --from=build /go/CA_crt.pem /
COPY --from=build /go/bin /

EXPOSE 8080

#ENTRYPOINT ["/grpc_server", "--grpcport=:8080", "--cert=server_crt.pem", "--key=server_key.pem", "--targetAudience=https://grpc-rx44ldyleq-uc.a.cloud-run-qual.sandbox.google.com", "--usetls=true", "--validateToken=true"]
ENTRYPOINT ["/grpc_server", "--grpcport=:8080", "--targetAudience=https://grpc-rx44ldyleq-uc.a.cloud-run-qual.sandbox.google.com", "--usetls=false", "--validateToken=true"]

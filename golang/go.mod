module main

go 1.14

require (
	github.com/salrashid123/grpc_google_id_tokens/echo v0.0.0
	golang.org/x/net v0.7.0
	google.golang.org/api v0.24.0
	google.golang.org/grpc v1.29.1
	google.golang.org/protobuf v1.28.1 // indirect
)

replace github.com/salrashid123/grpc_google_id_tokens/echo => ./src/echo

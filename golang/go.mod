module main

go 1.21

toolchain go1.21.12

require (
	github.com/salrashid123/grpc_google_id_tokens/echo v0.0.0
	golang.org/x/net v0.0.0-20200506145744-7e3656a0809f
	google.golang.org/api v0.24.0
	google.golang.org/grpc v1.29.1
)

require (
	cloud.google.com/go v0.56.0 // indirect
	github.com/golang/groupcache v0.0.0-20200121045136-8c9f03a8e57e // indirect
	github.com/golang/protobuf v1.5.0 // indirect
	go.opencensus.io v0.22.3 // indirect
	golang.org/x/oauth2 v0.0.0-20200107190931-bf48bf16ab8d // indirect
	golang.org/x/sys v0.0.0-20200331124033-c3d80250170d // indirect
	golang.org/x/text v0.3.2 // indirect
	google.golang.org/appengine v1.6.5 // indirect
	google.golang.org/genproto v0.0.0-20200331122359-1ee6d9798940 // indirect
	google.golang.org/protobuf v1.28.1 // indirect
)

replace github.com/salrashid123/grpc_google_id_tokens/echo => ./src/echo

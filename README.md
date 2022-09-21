# gRPC Authentication with Google OpenID Connect tokens


This is a continuation of an article i wrote about [Authenticating using Google OpenID Connect Tokens](https://medium.com/google-cloud/authenticating-using-google-openid-connect-tokens-e7675051213b).  That article specifically focused on how to get ID Tokens using a combination of
_google-auth-*_ libraries and directly acquiring the token and applying them to [gRPC](https://grpc.io/) clients.  The specific advantage of using google auth libraries and Credentials they provide is the legwork of refreshing and applying them to grpc native receivers is a lot easier than manually fiddling with request interceptors.

## gRPC Authentication

A bit of background..[gRPC Authentication](https://grpc.io/docs/guides/auth/) covers basically two built-in mechanisms:  

* _SSL/TLS_:  This covers basic TLS and mTLS with the gRPC server
* _Token-based authentication with Google_: This is oauth2 flows where an `access_token` is acquired and emitted to a Google Service.

For the token based authentication, an oauth2 `access_token` gets embedded inside a gRPC header itself similar to HTTP/Rest api calls.  You can see an example of how the HTTP2 frame carries the `Authorization: Bearer token` capability [here](https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-HTTP2.md#example).

Notice the Token based authentication emits an `access_token` intended to get sent to a Google API or service...what we want to do in this article is to emit an `id_token` to a service _we_ run on somewhere we can host gRPC.  At the moment, that somewhere is really on a GCE, GKE and Cloud Run or an arbitrary service that you run somewhere that happens to process google `id_token`.  For a primer on `access_token` vs `id_token`, review the article cited in the first paragraph.

The following is a specific end-to-end example for Cloud Run

- [gRPC Authentication with Cloud Run](https://github.com/salrashid123/cloud_run_grpc_auth)

Anyway, we need an `id_token` for gRPC and while we can [Extending gRPC to support other authentication mechanisms](https://grpc.io/docs/guides/auth/)), there's an easier way to acquire and use an `id_token` within your clients.

## gRPC Client/Server samples using google-auth-* libraries for id_token Authentication

The code samples provided here demonstrates how to get and use a Google OIDC token using _Google's own provided authentication library set_.  Acquiring an ID token is well documented but not how to get one with google's own auth libraries and then using them with a gRPC Credential object.

So, whats contained here:

- **A)**  gRPC Server in golang that can optionally revalidate any inbound `id_token`

- **B)**  grpc Clients in golang, java, python, nodejs that will acquire Google ID tokens using Google Auth libraries and use that token transparently in making unary calls to **A**

- **C)**  `Dockerfile` for gRPC server in **A**

- **D)**  [Envoy](https://www.envoyproxy.io/) configuration that will accept validate google-issued id_tokens embedded within gRPC requests.


Use **A** as a sample app that demonstrates gRPC app which also does automatic (re)validation of an authorization header.  If you are running your gRPC server behind an application that checks the request already (eg, istio), then there is no need to revaliate but thats subject to your paranoia.  Essentially that is:


1.  Client                         -->   Server
  
    Any of [`go,python,java,node`] -->  [`go`]

2.  Client                         -->  Proxy      -->   Server
  
    Any of [`go,python,java,node`] -->  [`Envoy`]  -->   [`go`] 


The client will first use google credentials to acquire an id_token and then embed that into the gRPC header.  THis step is done automatically for you by just specifying the credential type to use.  The samples demonstrate `ServiceAccountCredentials` but you are free to use any credential type except user-based tokens (which cannot proivde id_tokens with named audiences).

The important aspects to note in each call is the client:  each language sample here shows how to get an `id_token` using google apis and then add it into the grpc transport.  For example, in golang:

FIrst get an `id_token`

```golang
import "google.golang.org/api/idtoken"
...
...
	idTokenSource, err := idtoken.NewTokenSource(ctx, targetAudience, idtoken.WithCredentialsFile(serviceAccount))
	if err != nil {
		log.Fatalf("unable to create TokenSource: %v", err)
	}
	tok, err := idTokenSource.Token()
	if err != nil {
		log.Fatal(err)
	}
```

now that you have the `rpcCreds`, embed that into the grpc channel credentials as an option `grpc.WithPerRPCCredentials()`:


```golang
    ce := credentials.NewTLS(&tlsCfg)
	conn, err = grpc.Dial(*address,
		grpc.WithTransportCredentials(ce),
		grpc.WithPerRPCCredentials(grpcTokenSource{
			TokenSource: oauth.TokenSource{
				idTokenSource,
			},
		}),
	)
```

For reference, see: [https://godoc.org/google.golang.org/grpc#WithPerRPCCredentials](https://godoc.org/google.golang.org/grpc#WithPerRPCCredentials)

A couple of notes about the client bootstrapping credentials.  Not all `google-auth-*` libraries support out of the box id_tokens.  That work is in progress so the sample provided here for golang is experimental and sourced from *my* git repo (not google).  You are free to reuse that as necessary in the meantime.


* Python:  Supported as part of [google-auth-python](https://google-auth.readthedocs.io/en/latest/)
* Java: Supported as part of [google-auth-library-java[(https://github.com/googleapis/google-auth-library-java).
* Golang: Not supported.  I've provided sample imlementations for id_tokens and impersonation under [https://github.com/salrashid123/oauth2](https://github.com/salrashid123/oauth2)
* NodeJS: Supported as part of [google-auth-nodejs](https://github.com/googleapis/google-auth-library-nodejs)


## Implementations

The samples below can be run with or without TLS though certain bindings will only allow credentials over TLS.   The snippets are stand alone and uses self-signed certificates with SNI bindings for serverName=`grpc.doman.com`.  Each of the language snippets shows how to specify a self-signed CA as well as how to specify the SNI header.


First step is to download a [ServiceAccount JSON](https://cloud.google.com/iam/docs/creating-managing-service-account-keys) file from any google cloud project.  Once you have that, run the gRPC server and any client you want.


If you want to modify the proto, install `protoc` and the plugins for the languages you're interested in.  You can inspect the steps detailed in `golang/Dockerfile` for the setp steps for `protoc`.

### Setup - create ServiceAccount

First create a service account key

```bash
cd certs/
export PROJECT_ID=`gcloud config get-value core/project`
export PROJECT_NUMBER=`gcloud projects describe $PROJECT_ID --format='value(projectNumber)'`

gcloud iam service-accounts create grpc-client
gcloud iam service-accounts keys  create certs/grpc_client.json --iam-account=grpc-client@$PROJECT_ID.iam.gserviceaccount.com

```

### Golang

You can run he go sample here in two modes: secure and insecure.  If you want to transmit the `Authorization` Header, its encouraged to run the secure mode.


```bash
cd golang
cp -R ../certs .
docker build -t client -f Dockerfile.client .
docker build -t server -f Dockerfile.server .
```

#### Secure

- Server
```
docker run -p 8080:8080 server /grpc_server \
  --grpcport=:8080 --targetAudience=https://foo.bar \
  --usetls=true --validateToken=true
```

- Client
```
docker run --net=host \
  -v `pwd`/certs:/certs -t client /grpc_client \
  --address localhost:8080  --usetls=true  \
  --servername grpc.domain.com --audience https://foo.bar \
  --cacert tls-ca.crt --serviceAccount /certs/grpc_client.json
```


### Python

The provided `grpc_client.py` already includes the compiled protobuf with grpc support so you can just use what is provided.

Edit the following file an

```
 python grpc_client.py 
```

If needed, you can install the protoc support compiler, first run
```
python -m pip install grpcio-tools google-auth

python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. echo.proto
```

References:
- [google.auth.transport.grpc module])https://google-auth.readthedocs.io/en/latest/reference/google.auth.transport.grpc.html)


### Java

The provided `src/main/java/com/test/TestApp.java` include a sample of using IDTokens

to run, just execute `mvn clean install exec:java`


If you want to regenerate the proto, [download the plugin](https://github.com/grpc/grpc-java/tree/master/compiler), then execute the compiler

```bash
wget https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.49.1/protoc-gen-grpc-java-1.49.1-linux-x86_64.exe -P /tmp/
chmod u+x /tmp/protoc-gen-grpc-java-1.49.1-linux-x86_64.exe

protoc --plugin=protoc-gen-grpc-java=/tmp/protoc-gen-grpc-java-1.49.1-linux-x86_64.exe --java_out=src/main/java  \
    --grpc-java_out=lite:src/main/java/ --proto_path=echo/ echo/echo.proto
```

### Nodejs

Node already has support for id_tokens so running the sample is pretty easy


```bash
npm i
node app.js
```

### Envoy

A sample envoy proxy configuration is proivded here which you can use that proxy to do JWT/id_token validation.

To run, first startup the server in _insecure_ mode on port `:8080`

- Server

```bash
go run  src/grpc_server.go --grpcport :8080 \
  --cert $CERTS/server_crt.pem --key ../certs/server_key.pem  \
  --targetAudience https://foo.bar --usetls=false --validateToken=false
```

Now run the envoy proxy which will listen on `:18080`

- Envoy

```
envoy -c envoy_config.yaml
```

Now run the client to connect to the proxy
- Client

```bash
go run src/grpc_client.go \
  --address localhost:18080 --usetls=true --cacert ../certs/tls-ca.crt \
  --servername grpc.domain.com --audience  https://foo.bar --serviceAccount=../certs//grpc_client.json
```

## Conclusion

Thats it, not much of a conclusion as this is just a concise demonstration on how to easy get an `id_token` for use against your service hosted anywhere or against google API services that speak gRPC and accept id_tokens.


## References

- [Authenticating using Google OpenID Connect Tokens](https://medium.com/google-cloud/authenticating-using-google-openid-connect-tokens-e7675051213b)
- [gRPC Authentication](https://grpc.io/docs/guides/auth/)
- [gRPC Auth Support](https://github.com/grpc/grpc-go/blob/master/Documentation/grpc-auth-support.md)
- [Introduction to oauth on gRPC](https://texlution.com/post/oauth-and-grpc-go/)
- [Google OIDC Tokens with golang](https://github.com/salrashid123/oauth2)


```
export GRPC_VERBOSITY=DEBUG
export GRPC_TRACE=tcp,secure_endpoint,transport_security
```
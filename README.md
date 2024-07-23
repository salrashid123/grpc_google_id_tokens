# gRPC Authentication with Google OpenID Connect tokens


This is a continuation of an article i wrote about [Authenticating using Google OpenID Connect Tokens](https://blog.salrashid.dev/en/articles/2019/google_id_token/).  That article specifically focused on how to get ID Tokens using a combination of
_google-auth-*_ libraries and directly acquiring the token and applying them to [gRPC](https://grpc.io/) clients. 

## gRPC Authentication

A bit of background..[gRPC Authentication](https://grpc.io/docs/guides/auth/) covers basically two built-in mechanisms:  

* _SSL/TLS_:  This covers basic TLS and mTLS with the gRPC server
* _Token-based authentication with Google_: This is oauth2 flows where an `access_token` is acquired and emitted to a Google Service.

For the token based authentication, an oauth2 `access_token` gets embedded inside a gRPC header itself similar to HTTP/Rest api calls.  You can see an example of how the HTTP2 frame carries the `Authorization: Bearer token` capability [here](https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-HTTP2.md#example).

Notice the Token based authentication emits an `access_token` intended to get sent to a Google API or service...what we want to do in this article is to emit an `id_token` to a service _we_ run on somewhere we can host gRPC.  At the moment, that somewhere is really on a GCE, GKE and Cloud Run or an arbitrary service that you run somewhere that happens to process google `id_token`.  For a primer on `access_token` vs `id_token`, review the article cited in the first paragraph.

Anyway, we need an `id_token` for gRPC and while we can [Extending gRPC to support other authentication mechanisms](https://grpc.io/docs/guides/auth/)), there's an easier way to acquire and use an `id_token` within your clients.

## gRPC Client/Server samples using google-auth-* libraries for id_token Authentication

The code samples provided here demonstrates how to get and use a Google OIDC token using _Google's own provided authentication library set_.  Acquiring an ID token is well documented but not how to get one with google's own auth libraries and then using them with a gRPC Credential object.

So, whats contained here:

- **A)**  gRPC Server in golang that can optionally revalidate any inbound `id_token`

- **B)**  grpc Clients in golang, java, python, nodejs that will acquire Google ID tokens using Google Auth libraries and use that token transparently in making unary calls to **A**

- **C)**  [Envoy](https://www.envoyproxy.io/) configuration that will accept validate google-issued id_tokens embedded within gRPC requests.

Use **A** as a sample app that demonstrates gRPC app which also does automatic (re)validation of an authorization header.  If you are running your gRPC server behind an application that checks the request already (eg, istio), then there is no need to revaliate but thats subject to your paranoia.

Essentially this repo shows:

1.  Client                         -->   Server
  
    Any of [`go,python,java,node`] -->  [`go :8081`]

2.  Client                         -->  Proxy      -->   Server
  
    Any of [`go,python,java,node`] -->  [`Envoy :8080`]  -->   [`go :8081`] 


The client will first use google credentials to acquire an id_token and then embed that into the gRPC header.  THis step is done automatically for you by just specifying the credential type to use.  The samples demonstrate `ServiceAccountCredentials` but you are free to use any credential type except user-based tokens (which cannot proivde id_tokens with named audiences).

The important aspects to note in each call is the client:  each language sample here shows how to get an `id_token` using google apis and then add it into the grpc transport.  For example, in golang:


```golang
import "google.golang.org/api/idtoken"

		idTokenSource, err := idtoken.NewTokenSource(ctx, targetAudience, idtoken.WithCredentialsFile(serviceAccount))

		ce := credentials.NewTLS(&tlsCfg)
		conn, err = grpc.Dial(*address,
			grpc.WithTransportCredentials(ce),
			grpc.WithPerRPCCredentials(oauth.TokenSource{
				idTokenSource,
			},
			),
		)
```

>> **NOTE**: the examples here uses an actual service account *key* to get an id_token to use. There are many other ways to get an id_token depending on where you are running and what credential source you are using.  For more information, see [Sources of id_tokens](https://blog.salrashid.dev/en/articles/2019/google_id_token/#sources-of-google-issued-id-tokens).  You cna also acqurie an id_token where the service account is embedded inside an HSM or a Trusted Platform Module (TPM) (see example [here](https://github.com/salrashid123/gce_metadata_server/blob/0d0c12d3360de5a64dcbfd1203bae2f704460386/server.go#L672))


Also see [gRPC Authentication with Cloud Run](https://github.com/salrashid123/cloud_run_grpc_auth)

---

## Implementations

The snippets are stand alone and uses self-signed certificates with SNI bindings for serverName=`grpc.doman.com`.  Each of the language snippets shows how to specify a self-signed CA as well as how to specify the SNI header.

First step is to download a [ServiceAccount JSON](https://cloud.google.com/iam/docs/creating-managing-service-account-keys) file from any google cloud project.  Once you have that, run the gRPC server and any client you want.

### Setup - create ServiceAccount

First create a service account key

```bash
cd certs/
export PROJECT_ID=`gcloud config get-value core/project`
export PROJECT_NUMBER=`gcloud projects describe $PROJECT_ID --format='value(projectNumber)'`

gcloud iam service-accounts create grpc-client
gcloud iam service-accounts keys  create certs/grpc_client.json --iam-account=grpc-client@$PROJECT_ID.iam.gserviceaccount.com
```

For all examples below, run a 'server' in golang:

- Server

```bash
go run src/grpc_server.go \
   --grpcport=:8081 --targetAudience=https://foo.bar \
   --validateToken=true --cert ../certs/server_crt.pem --key ../certs/server_key.pem 
```

### Golang

To run the client:

- Client

```bash
go run src/grpc_client.go --address localhost:8081 \
    --servername grpc.domain.com --audience https://foo.bar \
	--cacert ../certs/tls-ca.crt --serviceAccount /path/to/grpc_client.json 
```

### Python

The provided `grpc_client.py` already includes the compiled protobuf with grpc support so you can just use what is provided.

```bash
pip3 install httplib2 protobuf google-auth grpcio --upgrade

python grpc_client.py 
```

If needed, you can install the protoc support compiler, first run

```bash
pip3 install httplib2 protobuf google-auth grpcio --upgrade

python3 grpc_client.py
```

### Java

The provided `src/main/java/com/test/TestApp.java` include a sample of using IDTokens

to run, just execute

```bash
mvn clean install exec:java
```

### Nodejs

Node already has support for id_tokens so running the sample is pretty easy


```bash
npm i
node app.js
```

### Envoy

A sample envoy proxy configuration is proivded here which you can use that proxy to do JWT/id_token validation.

in this case, the client will connect to envoy, envohy will decode and validate the jwt header and then *forward* the header-as is to the backend/upstream

- Envoy

```bash
# first get docker binry if you don't have it, i like to extract it from docker
### yeah, i use linux alot
# docker cp `docker create envoyproxy/envoy-dev:latest`:/usr/local/bin/envoy .

/path/to/envoy -c envoy_config.yaml -l debug
```

Now run the client to connect to the proxy (note, we're using port :8080 which is where envoy is listening)

- Client

```bash
go run src/grpc_client.go --address localhost:8080  \
    --servername localhost --audience https://foo.bar  \
	   --cacert ../certs/tls-ca.crt --serviceAccount  ../certs/grpc_client.json 
```

---

Thats it, not much of a conclusion as this is just a concise demonstration on how to easy get an `id_token` for use against your service hosted anywhere or against google API services that speak gRPC and accept id_tokens.


## References

- [Authenticating using Google OpenID Connect Tokens](https://medium.com/google-cloud/authenticating-using-google-openid-connect-tokens-e7675051213b)
- [gRPC Authentication](https://grpc.io/docs/guides/auth/)
- [gRPC Auth Support](https://github.com/grpc/grpc-go/blob/master/Documentation/grpc-auth-support.md)
- [Introduction to oauth on gRPC](https://texlution.com/post/oauth-and-grpc-go/)
- [Google OIDC Tokens with golang](https://github.com/salrashid123/oauth2)


The following is a specific end-to-end example for Cloud Run

- [gRPC Authentication with Cloud Run](https://github.com/salrashid123/cloud_run_grpc_auth)

```bash
export GRPC_VERBOSITY=DEBUG
export GRPC_TRACE=tcp,secure_endpoint,transport_security
```
import echo_pb2_grpc
import google.auth as google_auth
import google.auth.transport.requests as google_auth_transport_requests
import google.oauth2.credentials
import grpc
import httplib2
from echo_pb2 import EchoReply, EchoRequest
from google import auth as google_auth
from google.auth.transport import grpc as google_auth_transport_grpc
from google.auth.transport import requests as google_auth_transport_requests
from google.oauth2 import id_token, service_account


address = 'localhost:8080'
target_audience = 'https://foo.bar'
server_sni_name = 'grpc.domain.com'

usetls = True
ca_certificate = '../certs/tls-ca.crt'

svcAccountFile =  '../certs/grpc_client.json'

channel = None
if usetls:
  request = google_auth_transport_requests.Request()
  id_creds = service_account.IDTokenCredentials.from_service_account_file(
    svcAccountFile,
    target_audience=target_audience)

  with open(ca_certificate, 'rb') as f:
    trusted_certs = f.read()
  ssl_credentials = grpc.ssl_channel_credentials(root_certificates=trusted_certs)

  grpc_channel_options = (('grpc.ssl_target_name_override', server_sni_name,),)
  channel = google_auth_transport_grpc.secure_authorized_channel(
    credentials=id_creds, request=request, target=address, ssl_credentials=ssl_credentials, options=grpc_channel_options)  
else:
  channel = grpc.insecure_channel(address)

stub = echo_pb2_grpc.EchoServerStub(channel)
response = stub.SayHello(EchoRequest(name='you'))
print("Echo client received: " + response.message)

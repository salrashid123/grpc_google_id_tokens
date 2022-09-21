
const { GoogleAuth, JWT } = require('google-auth-library');
var grpc = require('grpc');
const fs = require('fs');
var protoLoader = require('@grpc/proto-loader');
var PROTO_PATH = __dirname + '/echo.proto';

var target_audience = 'https://foo.bar';
var address = 'localhost:8080';

var svcAccountFile = '../certs/grpc_client.json';

var certificateFile = '../certs/tls-ca.crt';
var sniServerName = 'grpc.domain.com';

var usetls = true;

var packageDefinition = protoLoader.loadSync(
  PROTO_PATH,
  {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
  });
var protoDescriptor = grpc.loadPackageDefinition(packageDefinition);

var echo = protoDescriptor.echo;


async function main() {
  var client;

  const keys = require(svcAccountFile);
  const opts = {
    "email": keys.client_email,
    "key": keys.private_key,
    "additionalClaims": { "target_audience": target_audience }
  }
  const jwtopts = new JWT(opts);

  //const tokenInfo = await jwtopts.authorizeAsync();
  //console.log(tokenInfo.id_token);

  const auth = new GoogleAuth({
    clientOptions: jwtopts,
    keyFilename: svcAccountFile
  });

  var sniopts = { 'grpc.ssl_target_name_override' : sniServerName};

  if (usetls) {
    var ssl_creds = grpc.credentials.createSsl( fs.readFileSync(certificateFile), null, null);
    var call_creds = grpc.credentials.createFromGoogleCredential(auth);
    var combined_creds = grpc.credentials.combineChannelCredentials(ssl_creds, call_creds);
    client = new echo.EchoServer(address, combined_creds, sniopts);
  } else {
    client = new echo.EchoServer(address, grpc.credentials.createInsecure());
  }

  client.sayHello({ name: 'echo from client' }, function (err, response) {
    if (err) {
      console.log('There was an error in RPC ', err);
      return;
    }
    console.log('EchoResponse:', response.message);
  });

}

main().catch(console.error);
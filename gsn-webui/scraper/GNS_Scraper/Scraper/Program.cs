// See https://aka.ms/new-console-template for more information
// GSN = {
//     "CLIENT_ID""web-gui-public",
//     'CLIENT_SECRET': 'web-gui-public',
//     'SERVICE_URL_PUBLIC': 'http://localhost:9000/ws/', # used for in-browser redirects
//     'SERVICE_URL_LOCAL': 'http://localhost:9000/ws/',  # used for on-server direct calls
//     'WEBUI_URL': 'http://localhost:4200/',             # used for in-browser redirects
//     'MAX_QUERY_SIZE': 5000,
// oauth_client_id = settings.GSN['CLIENT_ID']
// oauth_client_secret = settings.GSN['CLIENT_SECRET']
// oauth_redirection_url = settings.GSN['WEBUI_URL']
// oauth_sensors_url = settings.GSN['SERVICE_URL_LOCAL'] + "api/sensors"
// oauth_auth_url = settings.GSN['SERVICE_URL_PUBLIC'] + "oauth2/auth"
// oauth_token_url = settings.GSN['SERVICE_URL_LOCAL'] + "oauth2/token"
// oauth_user_url = settings.GSN['SERVICE_URL_LOCAL'] + "api/user"
// api_websocket = re.sub(r"http(s)?://", "ws://", settings.GSN['SERVICE_URL_PUBLIC'])
// max_query_size = settings.GSN['MAX_QUERY_SIZE']

using System.Net.Http.Headers;
using Scraper;


var client = new HttpClient();
var baseUri = "http://walker.uibk.ac.at:9000/ws/";
var clientId = "web-gui";
var clientSecret = "gns_webgui";

var startDateTime = new DateTimeOffset(DateTime.Today.AddDays(-14));

client.BaseAddress = new Uri(baseUri);

var token = await PermasenseApi.GetAccessToken(client, clientId, clientSecret);

if (!string.IsNullOrEmpty(token))
{
    client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
}

var sensors = await PermasenseApi.GetAllSensors(client);

if (sensors != null)
{
    foreach (var sensor in sensors.features)
    {
        await PermasenseApi.GetAndSaveSensorCsv(client,
            sensor.properties.vs_name,
            startDateTime.UtcDateTime);
    }
}
else
{
    Console.WriteLine("Could not find a sensor");
}


/*foreach (var filePath in Directory.GetFiles("../../../data", "*.csv"))
{
    var fileName = filePath.Split('\\');
    string csvData = File.ReadAllText(filePath);
    await DatabaseConnector.ImportCsv(fileName[^1], csvData);
}*/


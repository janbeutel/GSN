
using System.Text.Json;
using System.Web;

namespace Scraper;

public static class PermasenseApi
{
    public static async Task<string?> GetAccessToken(HttpClient client, string clientId, string clientSecret)
    {
        var requestContent = new FormUrlEncodedContent(new[]
        {
            new KeyValuePair<string, string>("grant_type", "client_credentials"),
            new KeyValuePair<string, string>("client_id", clientId),
            new KeyValuePair<string, string>("client_secret", clientSecret)
        });

        var response = await client.PostAsync("oauth2/token", requestContent);
        response.EnsureSuccessStatusCode();

        var json = await response.Content.ReadAsStringAsync();
        var tokenData = JsonSerializer.Deserialize<Dictionary<string, object>>(json);
        return tokenData?["access_token"].ToString();
    }

    public static async Task<SensorModel?> GetAllSensors(HttpClient client)
    {
        var response = await client.GetAsync("api/sensors");
        response.EnsureSuccessStatusCode();

        var content = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<SensorModel>(content);
    }

    public static async Task GetAndSaveSensorCsv(HttpClient httpClient, string sensorName, DateTime? from = null, DateTime? to = null,
        int? size = null)
    {
        var query = HttpUtility.ParseQueryString(string.Empty);
        if(from != null) query["from"] = from.Value.ToString("yyyy-MM-ddThh:mm:ss");
        if(to != null) query["to"] = to.Value.ToString("yyyy-MM-ddThh:mm:ss");
        if (size != null) query["size"] = size.ToString();

        var requestUrl = "api/sensors/" + sensorName + "/data?" + query.ToString();

        Console.WriteLine($"Try get values for sensor {sensorName}");
        
        var response = await httpClient.GetAsync(requestUrl);
        if (!response.IsSuccessStatusCode)
        {
            var errorContent = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"Error at fetching data for {sensorName}: {errorContent}");
            return;
        }
        
        var content = await response.Content.ReadAsStringAsync();
        
        var sensorData = JsonSerializer.Deserialize<SensorDataModel>(content);

        if (sensorData != null)
            await CsvWriter.WriteToFile(sensorName, sensorData);
        
        Console.WriteLine($"Successful retrieved data for {sensorName}");
    }
    
    

    
}
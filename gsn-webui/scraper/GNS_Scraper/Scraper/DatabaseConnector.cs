using System.Text;
using QuestDB;

namespace Scraper;

public static class DatabaseConnector
{
    public static async Task WriteToDb(string name, SensorDataModel sensorDataModel)
    {
        using var dbClient = await LineTcpSender.ConnectAsync(
            "localhost",9009,
            tlsMode: TlsMode.Disable,
            bufferOverflowHandling: BufferOverflowHandling.SendImmediately);
        
        dbClient.Table(name);
        foreach (var values in sensorDataModel.Properties.Values)
        {
            
            for (int j = 0; j < values.Length; j++)
            {
                var field = sensorDataModel.Properties.Fields[j];
                var value = values.ElementAt(j)?.ToString();
                if (value != null)
                {
                    switch (field.Type)
                    {
                        case "time":
                        case "BIGINT":
                            dbClient.Column(field.Name, long.Parse(value));
                            break;
                        case "INTEGER":
                        case "TINYINT":
                        case "SMALLINT":
                            dbClient.Column(field.Name, int.Parse(value));
                            break;
                        default:
                            dbClient.Column(field.Name, value);
                            break;
                    }
                }
            }
            await dbClient.SendAsync();
        }
    }

    public static async Task ImportCsv(string sensorName, string csvData)
    {
        try
        {
            var url = "http://lochmatter.uibk.ac.at:9000/imp?name=" + Path.GetFileNameWithoutExtension(sensorName);
            var client = new HttpClient();

            var content = new StringContent(csvData, Encoding.UTF8, "text/plain");

            var response = await client.PostAsync(url, content);

            if (response.IsSuccessStatusCode)
            {
                Console.WriteLine($"CSV data uploaded successfully for sensorName {sensorName}.");
            }
            else
            {
                Console.WriteLine($"Failed to upload CSV data. Status Code: {response.StatusCode}");
            }
        }
        catch (Exception e)
        {
            Console.WriteLine($"Exception at uploading csv to db: {e.Message}");
        }
        
    }
}
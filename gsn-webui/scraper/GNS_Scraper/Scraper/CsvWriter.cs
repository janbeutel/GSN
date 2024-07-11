namespace Scraper;

public static class CsvWriter
{
    public static async Task WriteToFile(string name, SensorDataModel sensorData)
    {
        if (sensorData.Properties.Values.Length == 0)
        {
            Console.WriteLine($"Sensor data of sensor {name} will not be written, because there is no data.");
        }
        
        await using StreamWriter writer = new StreamWriter($"../../../data/{name}.csv");
        // Write fields
        var fieldLine = string.Join(",", sensorData!.Properties.Fields.Select(field =>
            $"{field.Name}"));
        await writer.WriteLineAsync(fieldLine);

        PreprocessTimeStamps(sensorData);
        
        // Write values or no data message
        if (sensorData.Properties.Values.Length > 0)
        {
            foreach (var value in sensorData.Properties.Values)
            {
                await writer.WriteLineAsync(string.Join(",", value));
            }
        }
    }

    private static void PreprocessTimeStamps(SensorDataModel sensorDataModel)
    {
        var generationTimeIndex =
            sensorDataModel.Properties.Fields.ToList().FindIndex(x => x.Name.Equals("generation_time"));
        var timeZone = TimeZoneInfo.FindSystemTimeZoneById("Central European Standard Time");
        
        if (generationTimeIndex >= 0)
        {
            foreach (var values in sensorDataModel.Properties.Values)
            {
                var date = DateTimeOffset.FromUnixTimeMilliseconds(
                    long.Parse(values[generationTimeIndex]!.ToString()!));
                var offset = timeZone.GetUtcOffset(date);
                var formattedDate = date.ToString("yyyy-MM-ddTHH:mm:ss.fff") + "+" + offset.ToString("hhmm");
                values[generationTimeIndex] = formattedDate;
                /*values[generationTimeIndex] =
                    values[generationTimeIndex] != null
                        ? long.Parse(values[generationTimeIndex]!.ToString()!) * 1000 : null;*/
            }
        }
    }
}
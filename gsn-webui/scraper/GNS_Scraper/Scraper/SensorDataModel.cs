using System.Text.Json.Serialization;

namespace Scraper;

public class SensorDataModel
{
    [JsonPropertyName("properties")]
    public PropertiesModel Properties { get; set; }
}

public class PropertiesModel
{
    [JsonPropertyName("values")]
    public object?[][] Values { get; set; }
    
    [JsonPropertyName("fields")]
    public FieldModel[] Fields { get; set; }
}

public class FieldModel
{
    [JsonPropertyName("name")]
    public string Name { get; set; }
    [JsonPropertyName("type")]
    public string Type { get; set; }
    [JsonPropertyName("unit")]
    public string Unit { get; set; }
}
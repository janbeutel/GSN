public class SensorModel
{
    public string type { get; set; }
    public Features[] features { get; set; }

    public long GetMinStartTime()
    {
        return features.Select(f => f.properties.stats.start_datetime).Min();
    }
}

public class Features
{
    public string type { get; set; }
    public Properties properties { get; set; }
    public Geometry? geometry { get; set; }
    public int total_size { get; set; }
    public int page_size { get; set; }
}

public class Properties
{
    public string vs_name { get; set; }
    public object[] values { get; set; }
    public Fields[] fields { get; set; }
    public Stats stats { get; set; }
    public string description { get; set; }
    public string geographical { get; set; }
    public string latitude { get; set; }
    public string longitude { get; set; }
}

public class Fields
{
    public string name { get; set; }
    public string type { get; set; }
    public string unit { get; set; }
}

public class Stats
{
    public long start_datetime { get; set; }
    public long end_datetime { get; set; }
}

public class Geometry
{
    public string type { get; set; }
    public double[] coordinates { get; set; }
}
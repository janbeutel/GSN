"use client";
import React, { Suspense, useEffect, useRef, useState, useMemo } from "react";
import { ErrorBoundary } from "react-error-boundary";
import UplotReact from "uplot-react";
import "uplot/dist/uPlot.min.css";
import {
  useQueryClient,
  useQueryErrorResetBoundary,
  useSuspenseInfiniteQuery,
} from "@tanstack/react-query";
import { Button } from "./ui/button";
import { DataSelector } from "./data-selector";
import { Sensor } from "@/lib/sensor-utils";
import uPlot from "uplot";
import ChartMenu from "./chart-menu";
import { CardTitle } from "./ui/card";


const initialOpts: uPlot.Options = {
  title: "Matterhorn Temperaturechain",
  width: 800,
  height: 600,
  ms: 1,
  series: [{}],
  axes: [{}],
};

function hashStringToColor(str) {
  if (!str) {
    return '#000000'; // Return black color for empty or undefined strings
  }

  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }

  let color = '#';
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xff;
    color += ('00' + value.toString(16)).slice(-2);
  }

  return color;
}

async function fetchChartData({ pageParam, selectedSensor, resolution }: { pageParam: string, selectedSensor: Sensor, resolution: string }) {

  const chartDataMap = new Map();

  if (selectedSensor) {
    const numericFieldTypes = ["DOUBLE", "INTEGER", "BIGINT", "SMALLINT"];
    const numericFields = selectedSensor.fields.filter(field => numericFieldTypes.includes(field.type));
    const fieldsString = numericFields.map(field => field.name).join(',');

    const response = await fetch(`http://localhost:3000/api/timeseries?sensorName=${selectedSensor.name}&startTime=${selectedSensor.startTime}&endTime=${selectedSensor.endTime}&fields=${fieldsString}&resolution=${resolution}`);

    if (!response.ok) {
      throw new Error("Error fetching data");
    }
    let data = await response.json();
    data = data.rows;

    const count = data.length;

    chartDataMap.set('timed', Array(count).fill(0));
    numericFields.forEach(field => {
      chartDataMap.set(field.name, Array(count).fill(0));
    });

    for (let i = 0; i < count; i++) {
      chartDataMap.get('timed')[i] = new Date(Number(data[i]['monthly'])).getTime();
      numericFields.forEach(field => {
        chartDataMap.get(field.name)[i] = parseFloat(data[i][field.name]) || 0;
      });
    }

    return chartDataMap;
  }
  return chartDataMap;
}

const DataComponent = ({ width, selectedSensor, resolution }: { width: number, selectedSensor: Sensor, resolution: string }) => {

  if (!selectedSensor) {
    return null;
  }
  
  const numericFieldTypes = ["DOUBLE", "INTEGER", "BIGINT", "SMALLINT"];
  // Memoize numeric fields to avoid re-calculation on every render
  const numericFields = useMemo(() => selectedSensor.fields.filter(field => numericFieldTypes.includes(field.type)), [selectedSensor]);
  
  var chartRef = useRef<uPlot | null>(null);
  const queryClient = useQueryClient();
  
  const { data } = useSuspenseInfiniteQuery({
    queryKey: ["chartData", selectedSensor.name, resolution],
    queryFn: ({ pageParam }) => fetchChartData({ pageParam, selectedSensor, resolution }),
    getNextPageParam: (lastPage, pages) => lastPage.length > 0 ? "next" : undefined,
    initialPageParam: "start",
  });

  var [selectedValues, setSelectedValues] = useState<string[]>([]);
  const [zoomLevel, setZoomLevel] = useState({ min: null, max: null });

  useEffect(() => {
    if (chartRef.current) {
      chartRef.current.setSize({ width, height: 600 });
    }
  }, [width, chartRef]);

  useEffect(() => {
    setSelectedValues(numericFields.map(field => field.name));
  }, [selectedSensor, numericFields]);

  const chartOptions = useMemo(() => {
    const series = [{ label: 'timed', stroke: 'black' }];
    const axes = [{}];

    selectedValues.forEach((field, index) => {
      const color = hashStringToColor(field);
      series.push({ label: field, stroke: color, scale: field });
      if (index === 0) {
        //axes.push({ values: data.pages[0]?.get('timed') });
      }
    });

    initialOpts.width = width;
    initialOpts.title = selectedSensor.name;

    return {
      ...initialOpts,
      series,
      axes,
    };
  }, [selectedValues, data]);

  const handleAddRow = (row: string) => {
    setSelectedValues(prevSelectedValues => [...prevSelectedValues, row]);
  };

  const handleRemoveRow = (row: string) => {
    setSelectedValues(prevSelectedValues => prevSelectedValues.filter(value => value !== row));
  };

  const handleResetDataRows = () => setSelectedValues([]);
  
  const resetZoomLevel = () => {
    setZoomLevel({ min: null, max: null });
  };

  useEffect(() => {
    const u = chartRef.current;
    if (!u) return;
    u.over.ondblclick = (e) => {
      const valuesArray = Array.from(data.pages[0].values());
      u.setData(valuesArray);
      resetZoomLevel(); // Reset zoom level on double-click
    };
  }, [data, resetZoomLevel]);

  useEffect(() => {

    const u = chartRef.current;

    if (!u) return;
    const handleZoomAndFetch = async () => {
      
      if (u.select.width > 0) {

        const chartDataMap = new Map();

        let min = u.posToVal(u.select.left, "x");
        let max = u.posToVal(u.select.left + u.select.width, "x");

        const fieldsString = selectedValues.map(field => field).join(',');

        setZoomLevel({ min, max });

        const resp = await fetch(
          `http://localhost:3000/api/timeseries?sensorName=${selectedSensor.name}&startTime=${min}&endTime=${max}&fields=${fieldsString}&resolution=${resolution}`
        );

        let data = await resp.json();
        data = data.rows;

        const count = data.length;
    
        chartDataMap.set('timed', Array(count).fill(0));
        selectedValues.forEach(field => {
          chartDataMap.set(field, Array(count).fill(0));
        });
    
        for (let i = 0; i < count; i++) {
          chartDataMap.get('timed')[i] = new Date(Number(data[i]['monthly'])).getTime();
          selectedValues.forEach(field => {
            chartDataMap.get(field)[i] = parseFloat(data[i][field]) || 0;
          });
        }

        const valuesArray = Array.from(chartDataMap.values());

        // set new data
        u.setData(valuesArray, false);

        // zoom to selection
        u.setScale("x", { min, max });

        // reset selection
        u.setSelect({ width: 0, height: 0 }, false);
      }
    };

    u.hooks.setSelect = [handleZoomAndFetch];

    // Apply the zoom level when it's not null
    const { min, max } = zoomLevel;
    if (min !== null && max !== null) {
      u.setScale("x", { min, max });
    }

    return () => {
      u.hooks.setSelect = [];
    };
  }, [selectedSensor.name, resolution, zoomLevel, selectedValues]);


  return (
    <>
      <div className="my-2">
        <DataSelector
          dataRows={numericFields.map(field => field.name)}
          selectedValues={selectedValues}
          onAddRow={handleAddRow}
          onRemoveRow={handleRemoveRow}
          resetDataRows={handleResetDataRows}
        />
      </div>
      <UplotReact
        options={chartOptions}
        data={chartOptions.series.map(series => data.pages[0]?.get(series.label) || [])}
        onCreate={(chart) => {
          chartRef.current = chart;
        }}
      />
    </>
  );
};


function ChartContainer({ selectedSensor, resolution }: { selectedSensor: Sensor, resolution: string }) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [containerWidth, setContainerWidth] = useState(0);

  useEffect(() => {
    const updateWidth = () => {
      if (containerRef.current) {
        setContainerWidth(containerRef.current.offsetWidth);
      }
    };

    const resizeObserver = new ResizeObserver(updateWidth);
    updateWidth();
    resizeObserver.observe(containerRef.current!);

    return () => resizeObserver.disconnect();
  }, []);

  return (
    <div ref={containerRef}>
      {containerWidth > 0 && selectedSensor && (
        <DataComponent width={containerWidth} selectedSensor={selectedSensor} resolution={resolution} />
      )}
    </div>
  );
}

const LoadingSpinner = () => {
  return <div>Loading...</div>;
};

export default function TimeseriesChart({ selectedSensor }: { selectedSensor: Sensor }) {
  const { reset } = useQueryErrorResetBoundary();
  const [resolution, setResolution] = useState("");

  return (
    <div>
      <div className="flex items-center justify-between px-1 -mb-1 mt-4">
        <CardTitle>Sensor Data</CardTitle>
        <ChartMenu resolution={resolution} setResolution={setResolution} />
      </div>
      <ErrorBoundary
        onReset={reset}
        fallbackRender={({ resetErrorBoundary }) => (
          <div className="flex flex-col gap-2 items-center p-4 bg-slate-100 rounded-lg h-96 justify-center">
            <h3 className="text-lg">
              Unfortunately there was an error fetching the data!
            </h3>
            <Button onClick={() => resetErrorBoundary()}>Try again</Button>
          </div>
        )}
      >
        <Suspense fallback={<LoadingSpinner />}>
          {selectedSensor ? (
            <ChartContainer selectedSensor={selectedSensor} resolution={resolution} />
          ) : (
            <div>No sensor selected</div>
          )}
        </Suspense>
      </ErrorBoundary>
    </div>
  );
}

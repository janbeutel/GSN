"use client";
import React, { Suspense, useEffect, useRef, useState } from "react";
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

const opts: uPlot.Options = {
  title: "Matterhorn Temperaturechain",
  width: 800,
  height: 600,
  ms: 1,
  series: [
    {},
    {
      label: "temperature_nearsurface_t1",
      stroke: "red",
      value: (_: any, rawValue: number) => (rawValue ? rawValue + " °C" : ""),
    },
  ],
};

async function fetchChartData({ pageParam }: { pageParam: string }) {
  const response = await fetch(`http://walker.uibk.ac.at:3000/api/temperature-rock`);
  // const response = await fetch(`http://walker.uibk.ac.at:3000/api/temperature_rock?dataRow=${pageParam}`);
  if (!response.ok) {
    throw new Error("Error fetching data");
  }
  const data = await response.json();
  const count = data.count;
  const chartData: [number[], number[]] = [Array(count), Array(count)];

  for (let i = 0; i < count; i++) {
    chartData[0][count - i - 1] = new Date(data.dataset[i][0]).getTime();
    chartData[1][count - i - 1] = data.dataset[i][1];
  }
  return chartData;
}

const DataComponent = ({ width }: { width: number }) => {
  const queryClient = useQueryClient();
  const { data, fetchNextPage } = useSuspenseInfiniteQuery({
    queryKey: ["chartData"],
    queryFn: fetchChartData,
    getNextPageParam: (lastPage, pages) => "temperature_nearsurface_t2",
    initialPageParam: "temperature_nearsurface_t1",
  });

  const chartRef = useRef<uPlot | null>(null);

  const [selectedValues, setSelectedValues] = useState<string[]>([
    "temperature_nearsurface_t1",
  ]);

  useEffect(() => {
    if (chartRef.current) {
      chartRef.current.setSize({ width, height: 600 });
    }
  }, [width]);

  useEffect(() => {
    if (chartRef.current) {
      chartRef.current.setData([
        data.pages[0][0],
        ...data.pages.map((page) => page[1]),
      ]);
    }
  }, [data.pages.length, data.pages]);

  return (
    <>
      <div className="my-2">
        <DataSelector
          dataRows={[
            "temperature_nearsurface_t1",
            "temperature_nearsurface_t2",
            "temperature_5cm",
            "temperature_10cm",
            "temperature_20cm",
            "temperature_30cm",
          ]}
          selectedValues={selectedValues}
          onAddRow={(row) => {
            setSelectedValues([...selectedValues, row]);
            fetchNextPage();
            chartRef.current?.addSeries(
              {
                label: row,
                stroke: "blue",
                value: (_: any, rawValue: number) =>
                  rawValue ? rawValue + " °C" : "",
              },
              data.pages.length,
            );
          }}
          onRemoveRow={(row) => {
            setSelectedValues(selectedValues.filter((v) => v !== row));
            queryClient.setQueryData(["chartData"], (da) => ({
              pages: (da as any).pages.slice(0, 1),
              pageParams: (da as any).pageParams.slice(0, 1),
            }));

            chartRef.current?.delSeries(1);
          }}
          resetDataRows={() => setSelectedValues([])}
        />
      </div>
      <UplotReact
        options={opts}
        data={[]}
        onCreate={(chart) => {
          chartRef.current = chart;
        }}
      />
    </>
  );
};

function ChartContainer() {
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
      {containerWidth > 0 && <DataComponent width={containerWidth} />}
    </div>
  );
}

const LoadingSpinner = () => {
  return <div>Loading...</div>;
};

export default function TimeseriesChart() {
  const { reset } = useQueryErrorResetBoundary();
  return (
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
        <ChartContainer />
      </Suspense>
    </ErrorBoundary>
  );
}

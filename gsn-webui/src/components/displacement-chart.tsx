"use client";
import React, { Suspense, useEffect, useRef, useState } from "react";
import { ErrorBoundary } from "react-error-boundary";
import UplotReact from "uplot-react";
import "uplot/dist/uPlot.min.css";
import {
  useQueryErrorResetBoundary,
  useSuspenseQuery,
} from "@tanstack/react-query";
import { Button } from "./ui/button";
import ChartMenu from "./chart-menu";
import { Icons } from "./icons";

const opts: uPlot.Options = {
  width: 1000,
  height: 400,
  ms: 1,
  series: [
    {},
    {
      label: "Displacement",
      stroke: "red",
      value: (_: any, rawValue: number) => (rawValue ? rawValue + " mm" : ""),
    },
  ],
};

async function fetchChartData(resolution: string) {

  const response = await fetch(`http://localhost:3000/api/postgres?resolution=${resolution}`);

  if (!response.ok) {
    throw new Error("Error fetching data");
  }
  var data = await response.json();
  data = data.rows;

  const count = data.length;
  const chartData: [number[], number[]] = [Array(count), Array(count)];

  for (let i = 0; i < count; i++) {
    chartData[0][i] = new Date(Number(data[i]['monthly'])).getTime();
    chartData[1][i] = data[i]['avg'];
  }

  return chartData;
}

const DataComponent = ({ width, resolution }: { width: number, resolution: string }) => {

  const { data } = useSuspenseQuery({
    queryKey: ["displacement", resolution],
    queryFn: () => fetchChartData(resolution),
  });

  const chartRef = useRef<uPlot | null>(null);
  const [zoomLevel, setZoomLevel] = useState({ min: null, max: null });

  const resetZoomLevel = () => {
    setZoomLevel({ min: null, max: null });
  };

  useEffect(() => {
    if (chartRef.current) {
      chartRef.current.setSize({ width, height: 400 });
    }
  }, [width]);

  useEffect(() => {
    const u = chartRef.current;
    if (!u) return;

    u.over.ondblclick = (e) => {
      u.setData(data);
      resetZoomLevel(); // Reset zoom level on double-click
    };
  }, [data, resetZoomLevel]);

  useEffect(() => {
    const u = chartRef.current;
    if (!u) return;

    const handleZoomAndFetch = async () => {
      if (u.select.width > 0) {
        let min = u.posToVal(u.select.left, "x");
        let max = u.posToVal(u.select.left + u.select.width, "x");

        setZoomLevel({ min, max });

        const resp = await fetch(
          `http://localhost:3000/api/postgres?startTime=${min}&endTime=${max}&resolution=${resolution}`
        );

        var newData = await resp.json();
        newData = newData.rows;

        const count = newData.length;
        const chartData: [number[], number[]] = [Array(count), Array(count)];

        for (let i = 0; i < count; i++) {
          chartData[0][i] = new Date(Number(newData[i]['monthly'])).getTime();
          chartData[1][i] = newData[i]['avg'];
        }
        // set new data
        u.setData(chartData, false);

        // zoom to selection
        u.setScale("x", { min, max });

        // reset selection
        u.setSelect({ width: 0, height: 0 }, false);
      } else {
        const { min, max } = zoomLevel;

        if (min !== null && max !== null) {

          const resp = await fetch(
            `http://localhost:3000/api/postgres?startTime=${min}&endTime=${max}&resolution=${resolution}`
          );

          var newData = await resp.json();
          newData = newData.rows;

          const count = newData.length;
          const chartData: [number[], number[]] = [Array(count), Array(count)];

          for (let i = 0; i < count; i++) {
            chartData[0][i] = new Date(Number(newData[i]['monthly'])).getTime();
            chartData[1][i] = newData[i]['avg'];
          }
          // set new data
          u.setData(chartData, false);

          // zoom to selection
          u.setScale("x", { min, max });
        }
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
  }, [resolution, zoomLevel]);

  return (
    <>
      <UplotReact
        options={opts}
        data={data}
        onCreate={(chart) => {
          chartRef.current = chart;
        }}
      />
    </>
  );
};


export function ChartContainer({resolution}:{resolution:string}) {
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
      {containerWidth > 0 && <DataComponent width={containerWidth} resolution={resolution}/>}
    </div>
  );
}

const LoadingSpinner = () => {
  return (
    <>
      <div className="h-80 bg-slate-100 animate-pulse m-4 flex justify-center align-middle rounded">
        <div className="flex items-center">
          <Icons.loading className="h-8 w-8 text-slate-300 animate-spin mr-2" />
          Loading...
        </div>
      </div>
      <div className="flex justify-center gap-4 pb-2">
        <div className="w-32 h-6 bg-slate-100 animate-pulse rounded" />
        <div className="w-20 h-6 bg-slate-100 animate-pulse rounded" />
      </div>
    </>
  );
};

export default function DisplacementChart() {
  const { reset } = useQueryErrorResetBoundary();
  const [resolution, setResolution] = useState("");

  return (
    <div className="border rounded p-2 bg-white h-[477px]">
      <div className="flex items-center justify-between px-1 -mb-1">
        <h2 className="font-bold">Displacement Matterhorn</h2>
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
          <ChartContainer resolution={resolution} />
        </Suspense>
      </ErrorBoundary>
    </div>
  );
}

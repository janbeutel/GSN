"use client";
import ChartMenu from "@/components/chart-menu";
import React from "react";

export default function GaugeChart({ value, text,titel }: { value: number, text:string,titel:string }) {
  const radius = 40;

  return (
    <div className="w-96 bg-white rounded border p-2 h-full">
      <div className="flex items-center justify-between px-1">
        <h2 className="font-bold">{titel}</h2>
        <ChartMenu resolution="dynamic" setResolution={(s) => {}} />
      </div>
      <div className="flex flex-col justify-center h-full">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 100 75"
          fill="none"
          stroke="currentColor"
          className="-mt-12"
        >
          <path
            fill="none"
            stroke="blue"
            strokeWidth={1}
            d={pathString(radius + 5, 160, 200, 0)}
          />

          <path
            fill="none"
            stroke="green"
            strokeWidth={1}
            d={pathString(radius + 5, 200, 320, 0)}
          />
          <path
            fill="none"
            stroke="orange"
            strokeWidth={1}
            d={pathString(radius + 5, 320, 20, 0)}
          />
          <path
            fill="none"
            stroke="#eee"
            strokeWidth={8}
            d={pathString(radius, 160, 20)}
          />
          <text
            x="50"
            y="50"
            textAnchor="middle"
            dominantBaseline="central"
            fontSize="80%"
            fontWeight="100"
          >
            {text}
          </text>
          <path
            fill="none"
            stroke="green"
            strokeWidth={8}
            d={pathString(radius, 160, 160+160*value, 0)}
          />
        </svg>
      </div>
    </div>
  );
}

function pathString(
  radius: number,
  startAngle: number,
  endAngle: number,
  largeArc?: 0 | 1,
) {
  var coords = getDialCoords(radius, startAngle, endAngle),
    start = coords.start,
    end = coords.end,
    largeArcFlag = typeof largeArc === "undefined" ? 1 : largeArc;

  return [
    "M",
    start.x,
    start.y,
    "A",
    radius,
    radius,
    0,
    largeArcFlag,
    1,
    end.x,
    end.y,
  ].join(" ");
}

function getDialCoords(radius: number, startAngle: number, endAngle: number) {
  const cx = 50,
    cy = 50;
  return {
    end: getCartesian(cx, cy, radius, endAngle),
    start: getCartesian(cx, cy, radius, startAngle),
  };
}

function getCartesian(cx: number, cy: number, radius: number, angle: number) {
  const rad = (angle * Math.PI) / 180;
  return {
    x: Math.round((cx + radius * Math.cos(rad)) * 1000) / 1000,
    y: Math.round((cy + radius * Math.sin(rad)) * 1000) / 1000,
  };
}

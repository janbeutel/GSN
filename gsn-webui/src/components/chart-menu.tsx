"use client";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Icons } from "./icons";
import { Button } from "./ui/button";
import { Switch } from "./ui/switch";

export const resolutions = [
  {
    value: "1M",
    label: "1 Month",
  },
  {
    value: "7d",
    label: "1 Week",
  },
  {
    value: "1d",
    label: "1 Day",
  },
  {
    value: "6h",
    label: "6 Hours",
  },
  {
    value: "1h",
    label: "1 Hour",
  },
  {
    value: "30m",
    label: "30 Minutes",
  },
  {
    value: "15m",
    label: "15 Minutes",
  },
  {
    value: "2m",
    label: "2 Minutes",
  },
  {
    value: "",
    label: "Full",
  },
];

export default function ChartMenu({
  resolution,
  setResolution,
}: {
  resolution: string;
  setResolution: (value: string) => void;
}) {
  const isDynamic = resolution === "dynamic";
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className="flex h-8 w-8 p-0 data-[state=open]:bg-muted"
        >
          <Icons.moreVert className="h-4 w-4 stroke-[3]" />
          <span className="sr-only">Open menu</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-[220px]">
        <DropdownMenuItem
          className="justify-between"
          onSelect={(e) => {
            if (isDynamic) e.preventDefault();
            isDynamic ? setResolution("") : setResolution("dynamic");
          }}
        >
          Dynamic Resolution
          <Switch checked={isDynamic} />
        </DropdownMenuItem>
        <DropdownMenuSub>
          <DropdownMenuSubTrigger
            disabled={isDynamic}
            className="aria-disabled:text-muted-foreground"
          >
            Resolution
          </DropdownMenuSubTrigger>
          <DropdownMenuSubContent>
            <DropdownMenuRadioGroup
              value={resolution}
              onValueChange={(v) => setResolution(v)}
            >
              {resolutions.map((label) => (
                <DropdownMenuRadioItem key={label.value} value={label.value}>
                  {label.label}
                </DropdownMenuRadioItem>
              ))}
            </DropdownMenuRadioGroup>
          </DropdownMenuSubContent>
        </DropdownMenuSub>
        <DropdownMenuSeparator />
        <DropdownMenuItem>
          <Icons.exportData className="w-4 h-4 mr-2 text-muted-foreground" />
          Export Data
        </DropdownMenuItem>
        <DropdownMenuItem>
          <Icons.exportImage className="w-4 h-4 mr-2 text-muted-foreground" />
          Export Image
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

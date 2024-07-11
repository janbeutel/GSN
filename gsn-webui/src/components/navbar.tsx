"use client";

import * as React from "react";
import Link from "next/link";

import { cn } from "@/lib/utils";
import { Icons } from "@/components/icons";
import {
  NavigationMenu,
  NavigationMenuContent,
  NavigationMenuItem,
  NavigationMenuLink,
  NavigationMenuList,
  NavigationMenuTrigger,
  navigationMenuTriggerStyle,
} from "@/components/ui/navigation-menu";

const dashboards: { title: string; href: string; description: string }[] = [
  {
    title: "Displacement",
    href: "/displacement",
    description:
      "Displays the current crack displacment.",
  },
  {
    title: "Temperatures",
    href: "/temperature-rock",
    description:
      "Displays the current temperature of the sensors in a graph and a table.",
  },
  {
    title: "Uptime",
    href: "/uptime",
    description: "Sensor status information",
  },
  {
    title: "Images",
    href: "images",
    description: "Displays the current images of several Webcams.",
  },
  {
    title: "Weather",
    href: "/weather-gauge",
    description: "Displays the current weather data on-site.",
  },
];

export function NavBar() {
  return (
    <NavigationMenu className="text-slate-700">
      <NavigationMenuList>
        <NavigationMenuItem>
          <NavigationMenuTrigger>Overview</NavigationMenuTrigger>
          <NavigationMenuContent>
            <ul className="grid gap-3 p-6 md:w-[400px] lg:w-[500px] lg:grid-cols-[.75fr_1fr]">
              <li className="row-span-3">
                <NavigationMenuLink asChild>
                  <a
                    className="flex h-full w-full select-none flex-col justify-end rounded-md bg-gradient-to-b from-sky-100/50 to-sky-200 p-6 no-underline outline-none focus:shadow-md"
                    href="https://essd.copernicus.org/articles/11/1203/2019/"
                  >
                    <Icons.article className="h-6 w-6" />
                    <div className="mb-2 mt-4 text-lg font-medium">
                      PermaSense
                    </div>
                    <p className="text-sm leading-tight text-muted-foreground">
                      A decade of detailed observations (2008–2018) in steep
                      bedrock permafrost at the Matterhorn
                    </p>
                  </a>
                </NavigationMenuLink>
              </li>
              <ListItem href="" title="Introduction">
                Learn more about the project and the objectives.
              </ListItem>
              <ListItem href="/sensor-dashboard" title="Sensors">
                View the current sensor data and locations.
              </ListItem>
              <ListItem href="" title="More Information">
                Learn more about the technologies and methodologies.
              </ListItem>
            </ul>
          </NavigationMenuContent>
        </NavigationMenuItem>
        <NavigationMenuItem>
          <NavigationMenuTrigger>Dashboards</NavigationMenuTrigger>
          <NavigationMenuContent>
            <ul className="grid w-[400px] gap-3 p-4 md:w-[500px] md:grid-cols-2 lg:w-[600px] ">
              {dashboards.map((component) => (
                <ListItem
                  key={component.title}
                  title={component.title}
                  href={component.href}
                >
                  {component.description}
                </ListItem>
              ))}
            </ul>
          </NavigationMenuContent>
        </NavigationMenuItem>
        {/* <NavigationMenuItem>
          <Link href="https://essd.copernicus.org/articles/11/1203/2019/" legacyBehavior passHref>
            <NavigationMenuLink className={navigationMenuTriggerStyle()}>
              Documentation
            </NavigationMenuLink>
          </Link>
        </NavigationMenuItem> */}
        <NavigationMenuItem>
          <NavigationMenuTrigger>Documentation</NavigationMenuTrigger>
          <NavigationMenuContent>
            <ul className="grid gap-3 p-6 md:w-[400px] lg:w-[500px] lg:grid-cols-[.75fr_1fr]">
              <li className="row-span-3">
                <NavigationMenuLink asChild>
                  <a
                    className="flex h-full w-full select-none flex-col justify-end rounded-md bg-gradient-to-b from-sky-100/50 to-sky-200 p-6 no-underline outline-none focus:shadow-md"
                    href="https://essd.copernicus.org/articles/11/1203/2019/"
                  >
                    <Icons.article className="h-6 w-6" />
                    <div className="mb-2 mt-4 text-lg font-medium">
                      PermaSense
                    </div>
                    <p className="text-sm leading-tight text-muted-foreground">
                      A decade of detailed observations (2008–2018) in steep
                      bedrock permafrost at the Matterhorn
                    </p>
                  </a>
                </NavigationMenuLink>
              </li>
              <ListItem href="" title="Introduction">
                Learn more about the project and the objectives.
              </ListItem>
              <ListItem href="" title="Wiki">
                In depth technical documentation.
              </ListItem>
              <ListItem href="https://git.uibk.ac.at/informatik/neslab/public/permasense/permasense_datamgr" title="PermaSense Data Manager">
                Tools for quering and post-processing PermaSense Data.
              </ListItem>
            </ul>
          </NavigationMenuContent>
        </NavigationMenuItem>
      </NavigationMenuList>
    </NavigationMenu>
  );
}

const ListItem = React.forwardRef<
  React.ElementRef<"a">,
  React.ComponentPropsWithoutRef<"a">
>(({ className, title, children, href, ...props }, ref) => {
  return (
    <li>
      <NavigationMenuLink asChild>
        <Link
          ref={ref}
          href={href!}
          className={cn(
            "block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
            className,
          )}
          {...props}
        >
          <div className="text-sm font-medium leading-none">{title}</div>
          <p className="line-clamp-2 text-sm leading-snug text-muted-foreground">
            {children}
          </p>
        </Link>
      </NavigationMenuLink>
    </li>
  );
});
ListItem.displayName = "ListItem";

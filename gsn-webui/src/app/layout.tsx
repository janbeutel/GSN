import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./global.css";
import { UserAccountNav } from "@/components/user-navigation";
import { NavBar } from "@/components/navbar";
import Link from "next/link";
import { Icons } from "@/components/icons";
import { Providers } from "./providers";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "PermaSense",
  description:
    "This data portal provides online data access to various PermaSense deployments.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <Providers>
          <div className="flex flex-col h-full bg-[url('/background.svg')] bg-slate-50">
            <header className="flex items-center justify-between px-5 py-3 bg-white shadow max-h-[60px]">
              <div className="flex items-center gap-16">
                <Link
                  className="flex items-center text-lg font-semibold"
                  href="/"
                >
                  <Icons.logo className="text-blue-700 w-8 h-8" />
                  <span className="ml-2">PermaSense</span>
                </Link>
                <NavBar />
              </div>
              <UserAccountNav />
            </header>
            {children}
          </div>
        </Providers>
      </body>
    </html>
  );
}

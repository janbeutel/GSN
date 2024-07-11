export function getOptimalResolution(start: number, end: number) {
  const diff = end - start;

  switch (true) {
    case diff==0:
      return "7d";
    case diff <  1000 * 60 * 60 * 10:
      return "2m";
    case diff <  1000 * 60 * 60 * 24 * 7:
      return "1h";
    case diff <  1000 * 60 * 60 * 24 * 30:
      return "6h";
    default:
      return "1d";
  }
}

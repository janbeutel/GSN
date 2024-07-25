export function getOptimalResolution(start: number, end: number) {
  const diff = end - start;

  switch (true) {
    // standard 1 month
    case diff==0:
      return 1000 * 60 * 60 * 24 * 28;
    // smaller than 10h
    case diff <  1000 * 60 * 60 * 10:
      return 1000 * 60 * 5
    // smaller than 1 day
    case diff <  1000 * 60 * 60 * 24 * 7:
      return 1000 * 60 * 60
    // smaller than 1 month
    case diff <  1000 * 60 * 60 * 24 * 28:
      return  1000 * 60 * 60 * 24;
    // smaller than 1 year
    case diff <  1000 * 60 * 60 * 24 * 364:
      return 1000 * 60 * 60 * 24 * 7;
    default:
      return 1000 * 60 * 60 * 24 * 28;
  }
}

export function getResolution(resolution: string) {
  switch (resolution) {
    case '1M':
      return 1000 * 60 * 60 * 24 * 28;
    case '7d':
      return 1000 * 60 * 60 * 24 * 7;
    case '1d':
      return 1000 * 60 * 60 * 24;
    case '6h':
      return  1000 * 60 * 60 * 6;
    case '1h':
      return 1000 * 60 * 60;
    default:
      return 1000 * 60 * 60 * 24 * 28;
  }
}


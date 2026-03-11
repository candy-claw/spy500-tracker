const API_BASE = '/api';

export interface Stock {
  symbol: string;
  name: string;
  sector: string;
  industry: string;
  createdAt: string;
}

export interface DailyPrice {
  id: number;
  symbol: string;
  tradeDate: string;
  peRatio: number | null;
  marketCap: string | null;
  eps: number | null;
  volume: number | null;
  dividend: number | null;
  createdAt: string;
}

export interface StockWithPrice extends Stock {
  dailyPrice: DailyPrice | null;
}

// Get all stocks
export async function getStocks(): Promise<Stock[]> {
  const res = await fetch(`${API_BASE}/stocks`);
  if (!res.ok) throw new Error('Failed to fetch stocks');
  return res.json();
}

// Get all stocks with their latest daily prices
export async function getStocksWithPrices(): Promise<StockWithPrice[]> {
  const res = await fetch(`${API_BASE}/stocks/with-prices`);
  if (!res.ok) throw new Error('Failed to fetch stocks with prices');
  return res.json();
}

// Get stock by symbol
export async function getStock(symbol: string): Promise<Stock> {
  const res = await fetch(`${API_BASE}/stocks/${symbol}`);
  if (!res.ok) throw new Error('Failed to fetch stock');
  return res.json();
}

// Get daily prices for a symbol
export async function getDailyPrices(symbol: string): Promise<DailyPrice[]> {
  const res = await fetch(`${API_BASE}/stocks/${symbol}/prices`);
  if (!res.ok) throw new Error('Failed to fetch daily prices');
  return res.json();
}

// Get latest price for a symbol
export async function getLatestPrice(symbol: string): Promise<DailyPrice | null> {
  const res = await fetch(`${API_BASE}/stocks/${symbol}/latest`);
  if (!res.ok) return null;
  return res.json();
}

// Trigger data fetch
export async function triggerFetch(): Promise<void> {
  const res = await fetch(`${API_BASE}/stocks/fetch`, { method: 'POST' });
  if (!res.ok) throw new Error('Failed to trigger fetch');
}

// Get top movers (stocks with biggest changes)
export async function getMovers(limit: number = 10): Promise<StockWithPrice[]> {
  const res = await fetch(`${API_BASE}/stocks/movers?limit=${limit}`);
  if (!res.ok) throw new Error('Failed to fetch movers');
  return res.json();
}

// Get top gainers
export async function getTopGainers(limit: number = 10): Promise<StockWithPrice[]> {
  try {
    const res = await fetch(`${API_BASE}/analysis/top-gainers?limit=${limit}`);
    if (!res.ok) throw new Error('Failed to fetch gainers');
    return res.json();
  } catch {
    return [];
  }
}

// Get top losers
export async function getTopLosers(limit: number = 10): Promise<StockWithPrice[]> {
  try {
    const res = await fetch(`${API_BASE}/analysis/top-losers?limit=${limit}`);
    if (!res.ok) throw new Error('Failed to fetch losers');
    return res.json();
  } catch {
    return [];
  }
}

// Format market cap (e.g., "3826.42B" -> "3.8T")
export function formatMarketCap(cap: string | null): string {
  if (!cap) return 'N/A';
  return cap;
}

// Format percentage
export function formatPercent(value: number | null): string {
  if (value === null) return 'N/A';
  return `${value.toFixed(2)}%`;
}

// Format number with commas
export function formatNumber(value: number | null): string {
  if (value === null) return 'N/A';
  return value.toLocaleString();
}

import { useState, useEffect } from 'react';
import { getStocksWithPrices, triggerFetch } from '../services/api';
import type { StockWithPrice } from '../services/api';
import StockTable from './StockTable';
import Movers from './Movers';

export default function Dashboard() {
  const [stocks, setStocks] = useState<StockWithPrice[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await getStocksWithPrices();
      setStocks(data);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleFetch = async () => {
    try {
      setFetching(true);
      await triggerFetch();
      await loadData();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to fetch data');
    } finally {
      setFetching(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  if (loading) {
    return <div className="dashboard loading">Loading stock data...</div>;
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>S&P 500 Stock Tracker</h1>
        <button 
          className="fetch-btn" 
          onClick={handleFetch}
          disabled={fetching}
        >
          {fetching ? 'Fetching...' : 'Refresh Data'}
        </button>
      </header>

      {error && <div className="error">{error}</div>}

      <Movers />

      <section className="stocks-section">
        <h2>All Stocks</h2>
        <StockTable stocks={stocks} />
      </section>
    </div>
  );
}

import { useState, useEffect } from 'react';
import type { StockWithPrice } from '../services/api';
import { getMovers } from '../services/api';

export default function Movers() {
  const [movers, setMovers] = useState<StockWithPrice[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadMovers = async () => {
      try {
        const data = await getMovers(10);
        setMovers(data);
      } catch (e) {
        console.error('Failed to load movers:', e);
      } finally {
        setLoading(false);
      }
    };
    loadMovers();
  }, []);

  if (loading || movers.length === 0) {
    return null;
  }

  return (
    <section className="movers-section">
      <h2>Top Stocks</h2>
      <div className="movers-grid">
        {movers.map(stock => (
          <div key={stock.symbol} className="mover-card">
            <div className="mover-symbol">{stock.symbol}</div>
            <div className="mover-name">{stock.name}</div>
            <div className="mover-details">
              <span>P/E: {stock.dailyPrice?.peRatio?.toFixed(2) ?? 'N/A'}</span>
              <span>Vol: {stock.dailyPrice?.volume?.toLocaleString() ?? 'N/A'}</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

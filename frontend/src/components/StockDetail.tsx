import { useState, useEffect } from 'react';
import type { Stock, DailyPrice } from '../services/api';
import { getStock, getDailyPrices, formatMarketCap, formatNumber } from '../services/api';

interface Props {
  symbol: string;
  onClose: () => void;
}

export default function StockDetail({ symbol, onClose }: Props) {
  const [stock, setStock] = useState<Stock | null>(null);
  const [prices, setPrices] = useState<DailyPrice[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [stockData, priceData] = await Promise.all([
          getStock(symbol),
          getDailyPrices(symbol)
        ]);
        setStock(stockData);
        setPrices(priceData.sort((a, b) => 
          new Date(a.tradeDate).getTime() - new Date(b.tradeDate).getTime()
        ));
      } catch (e) {
        console.error('Failed to load stock detail:', e);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [symbol]);

  if (loading) {
    return (
      <div className="stock-detail modal">
        <div className="modal-content">
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  const latest = prices[prices.length - 1];

  return (
    <div className="stock-detail modal" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <header className="modal-header">
          <h2>{symbol}</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </header>

        {stock && (
          <div className="stock-info">
            <p className="company-name">{stock.name}</p>
            <p className="sector-industry">{stock.sector} • {stock.industry}</p>
          </div>
        )}

        {latest && (
          <div className="price-grid">
            <div className="price-item">
              <span className="label">P/E Ratio</span>
              <span className="value">{latest.peRatio?.toFixed(2) ?? 'N/A'}</span>
            </div>
            <div className="price-item">
              <span className="label">Market Cap</span>
              <span className="value">{formatMarketCap(latest.marketCap)}</span>
            </div>
            <div className="price-item">
              <span className="label">EPS</span>
              <span className="value">{latest.eps?.toFixed(2) ?? 'N/A'}</span>
            </div>
            <div className="price-item">
              <span className="label">Volume</span>
              <span className="value">{formatNumber(latest.volume)}</span>
            </div>
            <div className="price-item">
              <span className="label">Dividend</span>
              <span className="value">{latest.dividend ? `${latest.dividend}%` : 'N/A'}</span>
            </div>
            <div className="price-item">
              <span className="label">Last Updated</span>
              <span className="value">{latest.tradeDate}</span>
            </div>
          </div>
        )}

        {prices.length > 0 && (
          <div className="price-history">
            <h3>Price History</h3>
            <table className="history-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>P/E</th>
                  <th>Market Cap</th>
                  <th>EPS</th>
                  <th>Volume</th>
                  <th>Dividend</th>
                </tr>
              </thead>
              <tbody>
                {prices.slice(-10).reverse().map(p => (
                  <tr key={p.id}>
                    <td>{p.tradeDate}</td>
                    <td>{p.peRatio?.toFixed(2) ?? 'N/A'}</td>
                    <td>{formatMarketCap(p.marketCap)}</td>
                    <td>{p.eps?.toFixed(2) ?? 'N/A'}</td>
                    <td>{formatNumber(p.volume)}</td>
                    <td>{p.dividend ? `${p.dividend}%` : 'N/A'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

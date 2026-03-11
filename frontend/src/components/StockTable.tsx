import { useState } from 'react';
import type { StockWithPrice } from '../services/api';
import { formatMarketCap, formatNumber } from '../services/api';
import StockDetail from './StockDetail';

interface Props {
  stocks: StockWithPrice[];
}

export default function StockTable({ stocks }: Props) {
  const [selectedSymbol, setSelectedSymbol] = useState<string | null>(null);
  const [sortField, setSortField] = useState<'symbol' | 'sector' | 'peRatio'>('symbol');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  const [filter, setFilter] = useState('');

  const sorted = [...stocks]
    .filter(s => 
      s.symbol.toLowerCase().includes(filter.toLowerCase()) ||
      s.sector?.toLowerCase().includes(filter.toLowerCase())
    )
    .sort((a, b) => {
      let cmp = 0;
      if (sortField === 'symbol') {
        cmp = a.symbol.localeCompare(b.symbol);
      } else if (sortField === 'sector') {
        cmp = (a.sector || '').localeCompare(b.sector || '');
      } else if (sortField === 'peRatio') {
        const aVal = a.dailyPrice?.peRatio ?? 0;
        const bVal = b.dailyPrice?.peRatio ?? 0;
        cmp = aVal - bVal;
      }
      return sortDir === 'asc' ? cmp : -cmp;
    });

  const handleSort = (field: 'symbol' | 'sector' | 'peRatio') => {
    if (sortField === field) {
      setSortDir(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDir('asc');
    }
  };

  return (
    <div className="stock-table-container">
      <div className="table-controls">
        <input
          type="text"
          placeholder="Filter stocks..."
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="filter-input"
        />
      </div>

      <table className="stock-table">
        <thead>
          <tr>
            <th onClick={() => handleSort('symbol')} className="sortable">
              Symbol {sortField === 'symbol' && (sortDir === 'asc' ? '▲' : '▼')}
            </th>
            <th>Name</th>
            <th onClick={() => handleSort('sector')} className="sortable">
              Sector {sortField === 'sector' && (sortDir === 'asc' ? '▲' : '▼')}
            </th>
            <th onClick={() => handleSort('peRatio')} className="sortable">
              P/E {sortField === 'peRatio' && (sortDir === 'asc' ? '▲' : '▼')}
            </th>
            <th>Market Cap</th>
            <th>EPS</th>
            <th>Volume</th>
            <th>Dividend</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map(stock => (
            <tr 
              key={stock.symbol} 
              onClick={() => setSelectedSymbol(stock.symbol)}
              className={selectedSymbol === stock.symbol ? 'selected' : ''}
            >
              <td className="symbol">{stock.symbol}</td>
              <td>{stock.name}</td>
              <td>{stock.sector}</td>
              <td>{stock.dailyPrice?.peRatio?.toFixed(2) ?? 'N/A'}</td>
              <td>{formatMarketCap(stock.dailyPrice?.marketCap ?? null)}</td>
              <td>{stock.dailyPrice?.eps?.toFixed(2) ?? 'N/A'}</td>
              <td>{formatNumber(stock.dailyPrice?.volume ?? null)}</td>
              <td>{stock.dailyPrice?.dividend ? `${stock.dailyPrice.dividend}%` : 'N/A'}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedSymbol && (
        <StockDetail 
          symbol={selectedSymbol} 
          onClose={() => setSelectedSymbol(null)} 
        />
      )}
    </div>
  );
}

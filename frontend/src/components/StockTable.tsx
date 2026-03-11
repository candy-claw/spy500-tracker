import { useState } from 'react';
import type { StockWithPrice } from '../services/api';
import { Table, Input, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { SearchOutlined } from '@ant-design/icons';

interface Props {
  stocks: StockWithPrice[];
}

export default function StockTable({ stocks }: Props) {
  const [search, setSearch] = useState('');

  const filtered = stocks.filter(s => 
    s.symbol.toLowerCase().includes(search.toLowerCase()) ||
    (s.name?.toLowerCase().includes(search.toLowerCase()) ?? false)
  );

  const columns: ColumnsType<StockWithPrice> = [
    {
      title: 'Symbol',
      dataIndex: 'symbol',
      key: 'symbol',
      sorter: (a, b) => a.symbol.localeCompare(b.symbol),
      render: (text) => <Tag color="blue" style={{ fontWeight: 'bold' }}>{text}</Tag>,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
    },
    {
      title: 'Sector',
      dataIndex: 'sector',
      key: 'sector',
      render: (text) => text ? <Tag color="default">{text}</Tag> : <Tag>-</Tag>,
    },
    {
      title: 'P/E',
      dataIndex: ['dailyPrice', 'peRatio'],
      key: 'peRatio',
      align: 'right',
      render: (val) => val ? val.toFixed(2) : '-',
    },
    {
      title: 'Market Cap',
      dataIndex: ['dailyPrice', 'marketCap'],
      key: 'marketCap',
      align: 'right',
      render: (val) => val || '-',
    },
    {
      title: 'EPS',
      dataIndex: ['dailyPrice', 'eps'],
      key: 'eps',
      align: 'right',
      render: (val) => val ? `$${val.toFixed(2)}` : '-',
    },
    {
      title: 'Volume',
      dataIndex: ['dailyPrice', 'volume'],
      key: 'volume',
      align: 'right',
      render: (val) => val ? val.toLocaleString() : '-',
    },
    {
      title: 'Dividend',
      dataIndex: ['dailyPrice', 'dividend'],
      key: 'dividend',
      align: 'right',
      render: (val) => val ? `${val.toFixed(2)}%` : '-',
    },
  ];

  return (
    <div>
      <Input
        placeholder="Search stocks..."
        prefix={<SearchOutlined />}
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        style={{ marginBottom: '16px', maxWidth: '300px' }}
        allowClear
      />
      <Table
        columns={columns}
        dataSource={filtered}
        rowKey="symbol"
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `Total ${total} stocks` }}
        scroll={{ x: 1000 }}
        size="middle"
      />
    </div>
  );
}

import { useState, useEffect } from 'react';
import { getTopGainers, getTopLosers } from '../services/api';
import type { StockWithPrice } from '../services/api';
import { Card, Table, Tag, Tabs } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';

export default function Movers() {
  const [gainers, setGainers] = useState<StockWithPrice[]>([]);
  const [losers, setLosers] = useState<StockWithPrice[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [g, l] = await Promise.all([getTopGainers(10), getTopLosers(10)]);
        setGainers(g);
        setLosers(l);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const columns = [
    {
      title: 'Symbol',
      dataIndex: 'symbol',
      key: 'symbol',
      render: (text: string) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
    },
    {
      title: 'Price',
      dataIndex: ['dailyPrice', 'closePrice'],
      key: 'price',
      align: 'right' as const,
      render: (val: number) => val ? `$${val.toFixed(2)}` : '-',
    },
  ];

  const items = [
    {
      key: 'gainers',
      label: (
        <span>
          <ArrowUpOutlined style={{ color: '#52c41a' }} /> Top Gainers
        </span>
      ),
      children: (
        <Table
          columns={columns}
          dataSource={gainers}
          rowKey="symbol"
          pagination={false}
          size="small"
          loading={loading}
        />
      ),
    },
    {
      key: 'losers',
      label: (
        <span>
          <ArrowDownOutlined style={{ color: '#ff4d4f' }} /> Top Losers
        </span>
      ),
      children: (
        <Table
          columns={columns}
          dataSource={losers}
          rowKey="symbol"
          pagination={false}
          size="small"
          loading={loading}
        />
      ),
    },
  ];

  return (
    <Card>
      <Tabs items={items} />
    </Card>
  );
}

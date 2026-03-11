import { useState, useEffect } from 'react';
import { getStocks } from './services/api';
import type { Stock } from './services/api';
import { Layout, Typography, Card, Table, Input, Tag, Row, Col, Statistic, Spin, Alert } from 'antd';
import { SearchOutlined, StockOutlined } from '@ant-design/icons';

const { Header, Content } = Layout;
const { Title } = Typography;

export default function Dashboard() {
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await getStocks();
      setStocks(data);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filtered = stocks.filter(s => 
    s.symbol.toLowerCase().includes(search.toLowerCase()) ||
    (s.name?.toLowerCase().includes(search.toLowerCase()) ?? false)
  );

  const columns = [
    {
      title: 'Symbol',
      dataIndex: 'symbol',
      key: 'symbol',
      sorter: (a: Stock, b: Stock) => a.symbol.localeCompare(b.symbol),
      render: (text: string) => <Tag color="blue" style={{ fontWeight: 'bold' }}>{text}</Tag>,
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
      render: (text: string) => text ? <Tag>{text}</Tag> : <Tag>-</Tag>,
    },
    {
      title: 'Industry',
      dataIndex: 'industry',
      key: 'industry',
      ellipsis: true,
      render: (text: string) => text || '-',
    },
  ];

  if (loading) {
    return (
      <Layout style={{ minHeight: '100vh', background: '#f0f2f5' }}>
        <Content style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '50px' }}>
          <Spin size="large" />
        </Content>
      </Layout>
    );
  }

  return (
    <Layout style={{ minHeight: '100vh', background: '#f0f2f5' }}>
      <Header style={{ background: '#001529', padding: '0 24px', display: 'flex', alignItems: 'center' }}>
        <StockOutlined style={{ fontSize: '28px', color: '#fff', marginRight: '12px' }} />
        <Title level={3} style={{ margin: 0, color: '#fff' }}>S&P 500 Stock Tracker</Title>
      </Header>

      <Content style={{ padding: '24px' }}>
        {error && (
          <Alert 
            message="Error" 
            description={error} 
            type="error" 
            showIcon 
            closable 
            style={{ marginBottom: '24px' }}
          />
        )}

        <Row gutter={[16, 16]} style={{ marginBottom: '24px' }}>
          <Col xs={24} sm={8}>
            <Card>
              <Statistic title="Total Stocks" value={stocks.length} valueStyle={{ color: '#3f8600' }} />
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card>
              <Statistic 
                title="With Sector Info" 
                value={stocks.filter(s => s.sector).length} 
                valueStyle={{ color: '#1890ff' }} 
              />
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card>
              <Statistic 
                title="Last Updated" 
                value="2026-03-03" 
                valueStyle={{ color: '#722ed1' }} 
              />
            </Card>
          </Col>
        </Row>

        <Card title="All S&P 500 Stocks">
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
            scroll={{ x: 800 }}
            size="middle"
          />
        </Card>
      </Content>
    </Layout>
  );
}

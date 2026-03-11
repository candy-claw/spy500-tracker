import { useState, useEffect } from 'react';
import { getStocksWithPrices, triggerFetch } from './services/api';
import type { StockWithPrice } from './services/api';
import StockTable from './components/StockTable';
import Movers from './components/Movers';
import { Layout, Typography, Button, Card, Row, Col, Statistic, Spin, Alert } from 'antd';
import { ReloadOutlined, StockOutlined } from '@ant-design/icons';

const { Header, Content } = Layout;
const { Title } = Typography;

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
      <Header style={{ background: '#001529', padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <StockOutlined style={{ fontSize: '28px', color: '#fff' }} />
          <Title level={3} style={{ margin: 0, color: '#fff' }}>S&P 500 Stock Tracker</Title>
        </div>
        <Button 
          type="primary" 
          icon={<ReloadOutlined />} 
          onClick={handleFetch}
          loading={fetching}
          size="large"
        >
          {fetching ? 'Refreshing...' : 'Refresh Data'}
        </Button>
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
                title="Stocks with Data" 
                value={stocks.filter(s => s.dailyPrice).length} 
                valueStyle={{ color: '#1890ff' }} 
              />
            </Card>
          </Col>
          <Col xs={24} sm={8}>
            <Card>
              <Statistic 
                title="Last Updated" 
                value={stocks[0]?.dailyPrice?.tradeDate || 'N/A'} 
                valueStyle={{ color: '#722ed1' }} 
              />
            </Card>
          </Col>
        </Row>

        <Movers />

        <Card title="All S&P 500 Stocks" style={{ marginTop: '24px' }}>
          <StockTable stocks={stocks} />
        </Card>
      </Content>
    </Layout>
  );
}

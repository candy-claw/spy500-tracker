# SPY500 Tracker

S&P 500 Stock Tracker - Real-time stock price tracking and analysis platform.

## Tech Stack

- **Backend**: Java Spring Boot + MySQL
- **Frontend**: React + TypeScript + AntDesign + @ant-design/charts

## Prerequisites

1. **Java 21**: `brew install openjdk@21`
2. **Node.js**: Already installed (v22.x)
3. **MySQL**: Already installed
4. **Maven**: For building the Spring Boot app

## Project Structure

```
SPY500Tracker/
├── backend/                 # Spring Boot application
│   ├── src/
│   │   └── main/
│   │       ├── java/com/spy500/tracker/
│   │       │   ├── controller/   # REST APIs
│   │       │   ├── service/     # Business logic
│   │       │   ├── repository/  # Data access
│   │       │   └── entity/      # JPA entities
│   │       └── resources/
│   │           └── application.properties
│   └── pom.xml
│
└── frontend/               # React application
    ├── src/
    │   ├── pages/          # Page components
    │   ├── services/       # API calls
    │   ├── App.tsx        # Main app with routing
    │   └── index.tsx      # Entry point
    └── package.json
```

## Quick Start

### 1. Start MySQL (if not running)

```bash
mysql.server start
# Or on macOS:
brew services start mysql
```

### 2. Build and Run Backend

```bash
cd ~/project/SPY500Tracker/backend

# Build the project
./mvnw clean package -DskipTests
# Or if maven is not in path:
mvn clean package -DskipTests

# Run the application
./mvnw spring-boot:run
# Or:
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Run Frontend

```bash
cd ~/project/SPY500Tracker/frontend

# Install dependencies (if not already)
npm install

# Start development server
npm start
```

The frontend will start on `http://localhost:3000`

## Initial Setup

1. Open browser to `http://localhost:3000`
2. Click **"Initialize Stocks"** button to load S&P 500 stock list
3. Click **"Fetch Prices"** button to fetch latest stock prices

## Features

### Dashboard
- Market summary (gainers/losers)
- Top gainers and losers tables
- Average market change

### Stock List
- All S&P 500 stocks
- Search by symbol or company name
- Sort by various fields

### Stock Detail
- Price chart with historical data
- Volume chart
- Technical analysis (SMA, high/low, average)
- Detailed price history table

### Query Page
- Quick search by symbol
- Real-time price lookup
- Market overview with filtering

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/stocks` | Get all stocks |
| `GET /api/stocks/{symbol}` | Get stock by symbol |
| `GET /api/stocks/{symbol}/prices?days=30` | Get price history |
| `GET /api/stocks/prices/latest` | Get latest prices |
| `POST /api/stocks/initialize` | Initialize S&P 500 stocks |
| `POST /api/stocks/fetch-prices` | Fetch latest prices |
| `GET /api/analysis/top-gainers?limit=10` | Top gainers |
| `GET /api/analysis/top-losers?limit=10` | Top losers |
| `GET /api/analysis/stock/{symbol}/analysis?days=30` | Stock analysis |
| `GET /api/analysis/market-summary` | Market summary |

## Troubleshooting

### MySQL Connection Issues
Make sure MySQL is running:
```bash
mysql.server status
```

### Port Already in Use
If port 8080 or 3000 is in use, you can change it:
- Backend: Edit `server.port` in `backend/src/main/resources/application.properties`
- Frontend: Edit in `frontend/package.json` or use `PORT=3001 npm start`

### Java not found
Add to your `~/.bashrc`:
```bash
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
```
Then run `source ~/.bashrc`

## License

MIT

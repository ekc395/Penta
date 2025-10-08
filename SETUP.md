# Penta Setup Guide

This guide will help you set up the Penta League of Legends champion recommendation system.

## 🚀 Quick Start

### 1. Prerequisites

- **Java 17+** - [Download here](https://adoptium.net/)
- **Node.js 18+** - [Download here](https://nodejs.org/)
- **Maven 3.6+** - [Download here](https://maven.apache.org/)
- **Riot API Key** - [Get one here](https://developer.riotgames.com/)

### 2. Get Your Riot API Key

1. Go to [Riot Developer Portal](https://developer.riotgames.com/)
2. Sign in with your Riot account
3. Create a new application
4. Copy your API key
5. Set it as an environment variable:
   ```bash
   export RIOT_API_KEY="your-api-key-here"
   ```

### 3. Backend Setup

```bash
cd backend

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will be available at `http://localhost:8080`

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will be available at `http://localhost:5173`

## 📊 Data Collection

### Initialize Champion Data

First, populate the database with champion information:

```bash
curl -X POST http://localhost:8080/api/data/champions/initialize
```

### Collect Player Data

Collect match data for specific players:

```bash
# Single player
curl -X POST "http://localhost:8080/api/data/player/SummonerName?region=NA&matchCount=20"

# Multiple players
curl -X POST http://localhost:8080/api/data/players \
  -H "Content-Type: application/json" \
  -d '{
    "summonerNames": ["Player1", "Player2", "Player3"],
    "region": "NA",
    "matchCount": 20
  }'
```

### High-Elo Data Collection

For comprehensive data, collect from high-elo players:

```bash
curl -X POST "http://localhost:8080/api/data/high-elo?region=NA&playersPerTier=50&matchesPerPlayer=30"
```

### Check Data Collection Status

```bash
curl http://localhost:8080/api/data/status
```

## 🎯 API Endpoints

### Data Collection
- `POST /api/data/champions/initialize` - Initialize champion data
- `POST /api/data/player/{summonerName}` - Collect player data
- `POST /api/data/players` - Collect multiple players data
- `POST /api/data/match/{matchId}` - Process specific match
- `POST /api/data/statistics/update` - Update champion statistics
- `GET /api/data/status` - Get collection status

### Recommendations
- `GET /api/recommendations/player/{summonerName}` - Get player recommendations
- `POST /api/recommendations/team` - Get team recommendations
- `GET /api/recommendations/counter/{championName}` - Get counter-pick recommendations

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the backend directory:

```env
RIOT_API_KEY=your-riot-api-key-here
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_CREDENTIALS_PATH=path/to/firebase-credentials.json
```

### Application Properties

Key configuration in `application.yml`:

```yaml
riot:
  api:
    key: ${RIOT_API_KEY:your-riot-api-key-here}
    base-url: https://americas.api.riotgames.com
    timeout: 10000

league:
  api:
    base-url: https://127.0.0.1:2999
    timeout: 5000
```

## 📈 Data Processing Pipeline

### 1. Match Data Collection
- Fetches match history from Riot API
- Processes individual match details
- Extracts participant and team statistics

### 2. Statistics Calculation
- **Champion Stats**: Win rates, pick rates, average performance
- **Matchup Data**: 1v1 champion performance comparisons
- **Synergy Analysis**: Team composition effectiveness

### 3. Recommendation Algorithm
- **Player Comfort (40%)**: Based on mastery and win rate
- **Team Synergy (30%)**: Composition effectiveness
- **Opponent Matchup (20%)**: Counter-pick advantages
- **Meta Score (10%)**: Current tier and performance

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 🚨 Troubleshooting

### Common Issues

1. **Riot API Rate Limits**
   - The API has rate limits (100 requests per 2 minutes)
   - Use the built-in rate limiting in the configuration
   - Consider implementing request queuing for large data collection

2. **Database Connection Issues**
   - Ensure H2 database is properly configured
   - Check application.yml for correct database settings

3. **Frontend Build Issues**
   - Clear node_modules and reinstall: `rm -rf node_modules && npm install`
   - Check Node.js version compatibility

4. **CORS Issues**
   - Backend is configured to allow frontend origin
   - If issues persist, check the CORS configuration in controllers

### Performance Optimization

1. **Database Indexing**
   - Add indexes for frequently queried fields
   - Optimize JPA queries

2. **Caching**
   - Implement Redis for frequently accessed data
   - Cache champion statistics and matchup data

3. **Async Processing**
   - Use async methods for data collection
   - Implement background job processing

## 📚 Next Steps

1. **Data Collection**: Start with a few high-elo players to build initial dataset
2. **Algorithm Tuning**: Adjust recommendation weights based on testing
3. **UI Enhancement**: Add more interactive features to the frontend
4. **Performance**: Implement caching and optimization
5. **Deployment**: Set up production environment

## 🔗 Useful Links

- [Riot API Documentation](https://developer.riotgames.com/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://reactjs.org/docs)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)

# Penta - League of Legends Champion Recommendation System

Penta is an intelligent champion recommendation system for League of Legends that analyzes player data, team composition, and opponent matchups to suggest the best champions for your games.

## 🎯 Features

- **Personalized Recommendations**: Get champion suggestions based on your playstyle, mastery, and win rates
- **Team Synergy Analysis**: Analyze how well champions work together in your team composition
- **Opponent Matchup Data**: Stay ahead with real-time matchup statistics and counter-pick suggestions
- **Meta Analysis**: Access current meta data and tier lists from u.gg
- **League Client Integration**: Connect with the League Client Update API for real-time game data

## 🛠️ Tech Stack

### Backend
- **Java 17** - Core programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Database access layer
- **H2 Database** - In-memory database for development
- **Maven** - Dependency management
- **Firebase Admin SDK** - Authentication and data storage
- **JSoup** - Web scraping for u.gg data

### Frontend
- **React 18** - UI framework
- **TypeScript** - Type-safe JavaScript
- **Vite** - Build tool and development server
- **Tailwind CSS** - Utility-first CSS framework
- **Framer Motion** - Animation library
- **React Query** - Data fetching and caching
- **React Router** - Client-side routing
- **Axios** - HTTP client

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6 or higher
- League of Legends client (for API integration)

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Install dependencies:
   ```bash
   mvn clean install
   ```

3. Configure environment variables:
   ```bash
   # Create application-local.yml
   cp src/main/resources/application.yml src/main/resources/application-local.yml
   ```

4. Update the configuration with your settings:
   ```yaml
   firebase:
     project-id: your-firebase-project-id
     credentials-path: path/to/your/firebase-credentials.json
   ```

5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The backend will be available at `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

The frontend will be available at `http://localhost:5173`

## 📁 Project Structure

```
Penta/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/com/penta/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── model/          # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   └── PentaApplication.java
│   └── pom.xml
├── frontend/               # React frontend
│   ├── src/
│   │   ├── components/     # React components
│   │   ├── pages/         # Page components
│   │   ├── services/      # API services
│   │   ├── types/         # TypeScript types
│   │   └── styles/        # CSS styles
│   └── package.json
└── README.md
```

## 🔧 API Endpoints

### Champion Recommendations
- `GET /api/recommendations/player/{summonerName}` - Get recommendations for a player
- `POST /api/recommendations/team` - Get team-based recommendations
- `GET /api/recommendations/counter/{championName}` - Get counter-pick recommendations

### Parameters
- `summonerName` - League of Legends summoner name
- `region` - Server region (NA, EUW, KR, etc.)
- `teamChampions` - List of allied champions
- `opponentChampions` - List of enemy champions
- `preferredRole` - Preferred role (TOP, JUNGLE, MID, ADC, SUPPORT)

## 🎮 Usage

1. **Search for a Player**: Enter a summoner name and select your region
2. **Set Preferences**: Choose your preferred role and add team/opponent champions
3. **Get Recommendations**: Receive personalized champion suggestions with detailed analysis
4. **Review Analysis**: See breakdown of player comfort, team synergy, and matchup scores

## 🔮 Recommendation Algorithm

The recommendation system considers multiple factors:

1. **Player Comfort (40%)**: Based on games played, win rate, and mastery level
2. **Team Synergy (30%)**: How well the champion works with your team composition
3. **Opponent Matchup (20%)**: Advantage/disadvantage against enemy champions
4. **Meta Score (10%)**: Current tier and performance in the meta

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

## 🚧 Development Status

- ✅ Backend setup with Spring Boot
- ✅ Frontend setup with React + TypeScript
- ✅ Basic UI components and pages
- 🔄 League Client Update API integration
- 🔄 u.gg data scraping implementation
- 🔄 Recommendation algorithm refinement
- 🔄 Firebase integration
- 🔄 Unit testing

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

This project is not affiliated with Riot Games. League of Legends is a trademark of Riot Games, Inc. All game data is used under fair use for educational and analytical purposes.

## 🔗 Links

- [League of Legends Official Website](https://www.leagueoflegends.com/)
- [u.gg](https://u.gg) - Data source for meta and matchup information
- [Riot Games API](https://developer.riotgames.com/) - Official League of Legends API
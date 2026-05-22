# Kalid AI Pro

**Kalid AI Pro** is an AI-powered chatbot designed specifically for **Afaan Oromo** (Oromo language) speakers. It leverages OpenAI's GPT-4 Mini model to provide intelligent responses exclusively in Oromo.

## Features

- 🗣️ **Oromo Language Support** - Full conversation in Afaan Oromo
- 🤖 **AI-Powered** - Built with OpenAI's GPT-4 Mini model
- 🌐 **Web Interface** - Clean, dark-themed UI
- ⚡ **Fast Responses** - Real-time chat experience
- 🚀 **Vercel Ready** - Easy deployment to Vercel

## Prerequisites

- Node.js (v14 or higher)
- OpenAI API Key

## Installation

1. Clone the repository:
```bash
git clone https://github.com/kottikunii1-star/Kalid-ai-app.git
cd Kalid-ai-app
```

2. Install dependencies:
```bash
npm install
```

3. Create a `.env` file (copy from `.env.example`):
```bash
cp .env.example .env
```

4. Add your OpenAI API key to `.env`:
```
OPENAI_API_KEY=your_api_key_here
PORT=5000
```

## Usage

### Local Development

```bash
npm start
# or
node server.js
```

The application will start on `http://localhost:5000`

### Deployment to Vercel

1. Connect your GitHub repository to Vercel at https://vercel.com/new
2. Select this repository
3. Add environment variables in Vercel dashboard:
   - `OPENAI_API_KEY` - Your OpenAI API key
4. Deploy!

## Project Structure

```
Kalid-ai-app/
├── server.js          # Express server with chat API
├── package.json       # Project dependencies
├── vercel.json        # Vercel deployment config
├── .env.example       # Environment variables template
├── .gitignore         # Git ignore rules
└── README.md          # This file
```

## API Endpoints

- **GET** `/` - Serves the web chat interface
- **POST** `/chat` - Sends a message and receives AI response
  - Request body: `{ "message": "Your message in Oromo" }`
  - Response: `{ "reply": "AI response in Oromo" }`

## Technologies Used

- **Express.js** - Web framework
- **OpenAI API** - AI engine
- **CORS** - Cross-origin resource sharing
- **dotenv** - Environment variable management
- **Vercel** - Deployment platform

## License

MIT

## Author

[kottikunii1-star](https://github.com/kottikunii1-star)

---

**Afaan Oromootiin:** Kalid AI Pro waa chatbot AI kan Afaan Oromoo dubbataniif qophaa'e. GPT-4 Mini fayyadamuun gaaffiiwwaan Oromoo keessatti deebii kenniti.

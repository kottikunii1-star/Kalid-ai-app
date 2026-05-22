const express = require("express");
const cors = require("cors");
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

app.get("/", (req, res) => {
    res.send(`
<!DOCTYPE html>
<html lang="om">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kalid AI Pro</title>
    <style>
        body { background: #0f172a; color: white; font-family: sans-serif; display: flex; flex-direction: column; height: 100vh; margin: 0; }
        header { background: #1e293b; padding: 15px; text-align: center; border-bottom: 3px solid #10b981; }
        #chat { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 10px; }
        .msg { padding: 12px; border-radius: 10px; max-width: 80%; }
        .user { align-self: flex-end; background: #10b981; }
        .ai { align-self: flex-start; background: #334155; }
        .input-box { padding: 20px; background: #1e293b; display: flex; gap: 10px; }
        input { flex: 1; padding: 12px; border-radius: 5px; border: none; outline: none; font-size: 16px; color: black; }
        button { padding: 12px 20px; background: #10b981; color: white; border: none; border-radius: 5px; font-weight: bold; cursor: pointer; }
    </style>
</head>
<body>
    <header><h1>Kalid AI Pro</h1></header>
    <div id="chat"><div class="msg ai">Akkam, ani Kalid AI Pro dha. Afaan Oromootiin maal siin gargaaru?</div></div>
    <div class="input-box">
        <input type="text" id="msgInput" placeholder="Ergaa barreessi...">
        <button onclick="send()">Ergi</button>
    </div>
    <script>
        async function send() {
            const input = document.getElementById("msgInput");
            const chat = document.getElementById("chat");
            if(!input.value) return;
            const userMsg = input.value;
            chat.innerHTML += "<div class='msg user'>" + userMsg + "</div>";
            input.value = "";
            chat.scrollTop = chat.scrollHeight;

            try {
                const res = await fetch("/chat", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ message: userMsg })
                });
                const data = await res.json();
                chat.innerHTML += "<div class='msg ai'>" + data.reply + "</div>";
            } catch (e) {
                chat.innerHTML += "<div class='msg ai'>Dogoggora uumameera.</div>";
            }
            chat.scrollTop = chat.scrollHeight;
        }
    </script>
</body>
</html>
    `);
});

app.post("/chat", async (req, res) => {
    try {
        const response = await fetch("https://api.openai.com/v1/chat/completions", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + process.env.OPENAI_API_KEY
            },
            body: JSON.stringify({
                model: "gpt-4o-mini",
                messages: [
                    { role: "system", content: "Ati Kalid AI Pro dha. Afaan Oromoo qofaan deebii gabaabaa kenni." },
                    { role: "user", content: req.body.message }
                ]
            })
        });
        const data = await response.json();
        res.json({ reply: data.choices[0].message.content });
    } catch (e) {
        res.json({ reply: "Dogoggora server!" });
    }
});

// Vercel irratti 'app' qofa export gochuun gahaadha
module.exports = app;

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log("Hojjechaa jira..."));

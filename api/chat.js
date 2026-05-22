const fetch = require('node-fetch');

module.exports = async (req, res) => {
    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method not allowed' });
    }

    try {
        const { message } = req.body;

        if (!message) {
            return res.status(400).json({ reply: 'Message required' });
        }

        const response = await fetch('https://api.openai.com/v1/chat/completions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`
            },
            body: JSON.stringify({
                model: 'gpt-4o-mini',
                messages: [
                    { role: 'system', content: 'Ati Kalid AI Pro dha. Afaan Oromoo qofaan deebii gabaabaa kenni.' },
                    { role: 'user', content: message }
                ]
            })
        });

        const data = await response.json();
        
        if (!response.ok) {
            return res.status(response.status).json({ reply: 'OpenAI API error' });
        }

        res.status(200).json({ reply: data.choices[0].message.content });
    } catch (error) {
        console.error('Error:', error);
        res.status(500).json({ reply: 'Dogoggora server!' });
    }
};
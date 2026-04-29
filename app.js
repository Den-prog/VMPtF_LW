const cors = require('cors');
const express = require('express');
const app = express();
const testRoute = require('./videos/test')
app.use(cors());
app.use(express.json());//рядок дозволяє серверу читати джейсон дані від користувача
app.use('/videos', testRoute);

app.use(express.static('uploads'));


const PORT = 5000;





app.get('/', (req, res) => {
    res.send('Сервер працює!');
});



app.listen(PORT, () => {
    console.log("Сервер запущено.");
})

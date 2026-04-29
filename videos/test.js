const express = require('express');
const router = express.Router();

const multer = require('multer');
const path = require('path');

const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/'); 
    },
    filename: function (req, file, cb) {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({ storage: storage });


router.post('/upload', upload.single('videoFile'), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ message: "Файл не було завантажено" });
    }

    const newVideoId = videolist.length > 0 ? videolist[videolist.length - 1].id + 1 : 1;
    
    const newVideoTitle = req.body.title || "Нове відео";
    
    const newVideo = {
        id: newVideoId,
        name: newVideoTitle,
        url: `http://localhost:5000/${req.file.filename}`, 
        comments: [], 
        sharedvideos: []
    };


    videolist.push(newVideo);
    

    res.json(newVideo);
});


const videolist = [
    { id: 1, name: "Відео про природу", url: "http://localhost:5000/nature.mp4", 
        comments: [{ text: " Дуже гарне відео!", author: "Олексій (admin)" }],
        sharedvideos: []
    },
    {
        id: 2, name: "Michael Jackson - Billie Jean (Official Video)",
        url: "http://localhost:5000/Michael%20Jackson%20-%20Billie%20Jean%20(Official%20Video).mp4", 
        comments: [{ text: " Класика поп музики!", author: "Дмитро (user)" }],
        sharedvideos: []
    }

];

const users = [
    { id: 1, name: "Олексій (admin)", role: "admin", password: "12345" },
    { id: 2, name: "Дмитро (user)", role: "user", password: "qwerty" }
]

router.post('/register', (req, res) => {
    const {name, password} = req.body;

    const userExists = users.find(u => u.name === name);
    if(userExists) return res.status(400).json({message: "Користувач вже існує!"});

    const newUser = {
        id: users.length - 1,
        name: name,
        role: "user",
        password: password
    };

    users.push(newUser);
     res.json({message: "Реєстрація успішна!", user: newUser});

})

router.post('/login', (req, res) => {
    const { name, password } = req.body;
    
    const user = users.find(u => u.name === name && u.password === password);

    if (user) {
        res.json({ message: "Вхід успішний", user: user });
    } else {
        res.status(401).json({ message: "Невірне ім'я або пароль" });
    }
});


router.get('/users', (req, res) => {
    res.json(users)
})

router.post('/:videoId/comments', (req, res) => {
    const videoId = parseInt(req.params.videoId);
    const video = videolist.find(v => v.id === videoId);

    if (video) {
        const newText = req.body.text;
        const commentAuthor = req.body.author;

        video.comments.push({
            text: newText,
            author: commentAuthor.name
        }); 
         res.json(video);
    }
    else{
        res.status(404).json({ Message: "Відео не знайдено" });
    }
   
})

router.post('/:videoId/share', (req, res) =>{
    const videoId = parseInt(req.params.videoId);
    const receiverId = req.body.receiverId;
    const senderName = req.body.senderName;

    const video = videolist.find(v => v.id === videoId);

   if (video) {
        //перевірка, чи не ділилися ми вже з ЦИМ користувачем
        const alreadyShared = video.sharedvideos.find(s => s.receiverId === receiverId);
        
        if (!alreadyShared) {
          //запис кому і від кого надіслалось
            video.sharedvideos.push({ 
                receiverId: receiverId, 
                sharedBy: senderName 
            });
            res.json({ message: "Відео поділено успішно", video: video });
        } else {
            res.status(400).json({ message: "Відео вже поділено з цим користувачем" });
        }
    } else {
        res.status(404).json({ message: "Відео не знайдено" });
    }

})

//маршрут для отримання поділених відео для користувача
router.get('/shared/:userId', (req, res) => {
    const userId = parseInt(req.params.userId);
    const sharedVideos = videolist.filter(video => 
        video.sharedvideos.some(shared => shared.receiverId === userId)
    );
    res.json(sharedVideos);
});

//маршрут для отримання списку відео
router.get('/', (req, res) => {
    res.json(videolist);
})


router.delete('/:videoId/comments/:commentIndex', (req, res) => {
    const videoId = parseInt(req.params.videoId);
    const commentIndex = parseInt(req.params.commentIndex);
    
    const video = videolist.find(v => v.id === videoId);
    if (video && video.comments[commentIndex]) {
        video.comments.splice(commentIndex, 1); 
        res.json(video); 
    } else {
        res.status(404).send("Коментар не знайдено");
    }
});

module.exports = router;
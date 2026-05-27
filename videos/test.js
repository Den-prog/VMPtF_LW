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
        sharedvideos: [],
        likes: 0,
        likedBy: []
    };


    videolist.push(newVideo);
    

    res.json(newVideo);
});


const videolist = [
    { id: 1, name: "Відео про природу", url: "http://localhost:5000/nature.mp4", 
        comments: [{ text: " Дуже гарне відео!", author: "Олексій (admin)" }],
        sharedvideos: [],
        likes: 0,
        likedBy: []
    },
    {
        id: 2, name: "Michael Jackson - Billie Jean (Official Video)",
        url: "http://localhost:5000/Michael%20Jackson%20-%20Billie%20Jean%20(Official%20Video).mp4", 
        comments: [{ text: " Класика поп музики!", author: "Дмитро (user)" }],
        sharedvideos: [],
        likes: 0,
        likedBy: []
    }

];

const users = [
    { id: 1, name: "Олексій", role: "admin", password: "12345" },//admin
    { id: 2, name: "Дмитро", role: "user", password: "qwerty" }//user
]

router.patch('/:id/like', (req, res) => {
    const videoId = parseInt(req.params.id);
   const action = req.body.action;
    const userId = req.body.userId ? parseInt(req.body.userId) : null;

    const video = videolist.find(v => v.id === videoId);

    if (!video) {
        return res.status(404).json({ message: "Відео не знайдено" });
    }

    if (userId != null) {
        const hasLiked = video.likedBy.includes(userId);

        if (hasLiked) {
            video.likedBy = video.likedBy.filter(id => id !== userId);
            if (video.likes > 0) video.likes -= 1;
        } else {
            video.likedBy.push(userId);
            video.likes += 1;
        }

        return res.json(video);
    }

    if (action === 'increment') {
        video.likes += 1;
    } else if (action === 'decrement' && video.likes > 0) {
        video.likes -= 1;
    }

    res.json(video);
})

router.post('/register', (req, res) => {
    const {name, password} = req.body;

    const userExists = users.find(u => u.name === name);
    if(userExists) return res.status(400).json({message: "Користувач вже існує!"});

    const newUserId = users.length > 0 ? users[users.length - 1].id + 1 : 1;
    const newUser = {
        id: newUserId,
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

//маршрут для видалення відео (Адмін)
router.delete('/:id', (req, res) => {
    const videoId = parseInt(req.params.id);
    const index = videolist.findIndex(v => v.id === videoId);
    
    if (index !== -1) {
        videolist.splice(index, 1);
        res.json({ message: "Відео видалено" });
    } else {
        res.status(404).json({ message: "Відео не знайдено" });
    }
});

//маршрут для повного очищення коментарів (Адмін)
router.delete('/:videoId/comments', (req, res) => {
    const videoId = parseInt(req.params.videoId);
    const video = videolist.find(v => v.id === videoId);
    
    if (video) {
        video.comments = [];
        res.json(video);
    } else {
        res.status(404).json({ message: "Відео не знайдено" });
    }
});




module.exports = router;
import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from 'react';

function App() {
  const [videos, setVideos] = useState([]);
  const [newComments, setNewComments] = useState({});
  const [users, setUsers] = useState([]);
  const [currentUser, setCurrentUser] = useState('')// зберігання ім'я обраного користувача
  const [shareReceivers, setShareReceivers] = useState({});
  const [uploadFile, setUploadFile] = useState(null);
  const [uploadTitle, setUploadTitle] = useState('');
  const [isAuth, setIsAuth] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');


  const handleAuth = () => {
    const url = authMode === 'login' ? 'login' : 'register';

    fetch(`http://localhost:5000/videos/${url}`, {
      method: "POST",
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: username, password: password })
    })
      .then(res => {
        if (!res.ok) throw new Error("Помилка авторизації");
        return res.json();
      })
      .then(data => {
        setCurrentUser(data.user);
        setIsAuth(true);
        alert(data.message);
      })
      .catch(err => alert(err.message));
  }

  const handleCommentChange = (videoId, text) => {
    setNewComments({
      ...newComments,
      [videoId]: text

    });
  };

  const handleShareChange = (videoId, receiverId) => {
    setShareReceivers({
      ...shareReceivers,
      [videoId]: receiverId
    });
  };

  const submitComment = (videoId) => {
    const commentText = newComments[videoId];
    if (!commentText) return;

    //POST query
    fetch(`http://localhost:5000/videos/${videoId}/comments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json' //серверу віправдляємо джейсон
      },
      body: JSON.stringify({ text: commentText, author: currentUser })//текст в джейсон
    })
      .then(response => response.json())
      .then(updatedVideo => {
        const newVideos = videos.map(v =>
          v.id === videoId ? updatedVideo : v
        );
        setVideos(newVideos);
        //очищення поле воду для конкертного відео
        setNewComments({
          ...newComments,
          [videoId]: ''
        });
      })
      .catch(error => console.error("Помилка:", error));
  };

  const deleteComment = (videoId, commentIndex) => {
    fetch(`http://localhost:5000/videos/${videoId}/comments/${commentIndex}`, {
      method: 'DELETE',

    })
      .then(res => {
        if (!res.ok) throw new Error("Помилка при видаленні");
        return res.json();
      })
      .then(updatedVideo => {
        const newVideos = videos.map(v => v.id === videoId ? updatedVideo : v);
        setVideos(newVideos);
      })
      .catch(error => console.error("Помилка: ", error));
  }

  const shareVideo = (videoId) => {
    //отримуємо айді користувача, якому хочемо надіслати 
    const receiverId = shareReceivers[videoId];

    if (!receiverId) return;

    fetch(`http://localhost:5000/videos/${videoId}/share`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        receiverId: parseInt(receiverId),
        senderName: currentUser.name

      })
    })
      .then(response => {
        if (!response.ok) {
          //оброботка помилки, або просто помилка або вже надіслано
          throw new Error("Помилка або відео вже надіслано");
        }
        return response.json();
      })
      .then(data => {
        console.log(data.message);
        const updatedVideo = data.video;
        const newVideos = videos.map(v => v.id === videoId ? updatedVideo : v);
        setVideos(newVideos);
        setShareReceivers({
          ...shareReceivers,
          [videoId]: ''
        });
      })
      .catch(error => alert(error.message));
  };

  const handleUpload = () => {
    if (!uploadFile) {
      alert("Будь ласка, оберіть файл!");
      return;
    }

    const formData = new FormData();
    formData.append('videoFile', uploadFile);
    formData.append('title', uploadTitle);

    fetch('http://localhost:5000/videos/upload', {
      method: 'POST',
      body: formData
    })
      .then(response => {
        if (!response.ok) throw new Error("Помилка завантаження");
        return response.json();
      })
      .then(newVideo => {
        setVideos([...videos, newVideo]);
        setUploadFile(null);
        setUploadTitle('');
        alert("Відео успішно завантажено!");
      })
      .catch(error => console.error("Помилка:", error));
  };

  useEffect(() => {
    fetch('http://localhost:5000/videos')
      .then(response => response.json())
      .then(data => {
        setVideos(data);
      })
      .catch(error => console.error("Помилка: ", error));


    fetch('http://localhost:5000/videos/users')
      .then(response => response.json())
      .then(data => {
        setUsers(data);
        if (data.length > 0) {
          setCurrentUser(data[0]);
        }
      })
      .catch(error => console.error("Помилка: ", error));
  }, []);

  //достаємо тільки ті відео якими поділилися з поточним користувачем
  const sharedVideos = videos.filter(video =>
    video.sharedvideos && video.sharedvideos.some(share => share.receiverId === currentUser?.id)
  );

  return (
    <div className="App">
      {!isAuth ? (
        <div className="auth-form">
          <h2>{authMode === 'login' ? 'Вхід' : 'Реєстрація'}</h2>
          <input type="text" placeholder="Ім'я" onChange={e => setUsername(e.target.value)} />
          <input type="password" placeholder="Пароль" onChange={e => setPassword(e.target.value)} />
          <button onClick={handleAuth}>Підтвердити</button>
          <p onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}>
            {authMode === 'login' ? 'Немає акаунта? Зареєструйся' : 'Вже є акаунт? Увійди'}
          </p>
        </div>
      ) : (
        <>
          <header className="app-header">
            <div className='user-selector'>
              <span>Ви увійшли як: <strong>{currentUser?.name}</strong> </span>
              <span className={`role-badge ${currentUser?.role}`}>({currentUser?.role})</span>
              <button className="logout-btn" onClick={() => {
                setIsAuth(false);
                setCurrentUser(null);
              }}>Вийти</button>

            </div>

            <div className="upload-section" style={{ marginBottom: '30px', padding: '20px', backgroundColor: '#f0f0f0', borderRadius: '10px' }}>
              <h2>Завантажити нове відео</h2>
              <div style={{ display: 'flex', gap: '10px', alignItems: 'center', justifyContent: 'center' }}>
                <input
                  type="text"
                  placeholder="Назва відео"
                  value={uploadTitle}
                  onChange={(e) => setUploadTitle(e.target.value)}
                />
                <input
                  type="file"
                  accept="video/mp4"
                  onChange={(e) => setUploadFile(e.target.files[0])}
                />
                <button onClick={handleUpload}>Завантажити на сервер</button>
              </div>
            </div>

          </header>

          <h1>Мій Відеохостинг</h1>
          <div className="video-list">
            {videos.length === 0 ? (
              <p>Завантаження відео...</p>
            ) : (
              videos.map(video => (
                <div key={video.id} className="video-item">
                  <h3>{video.name}</h3>
                  <video width="500" controls>
                    <source src={video.url} type="video/mp4" />
                  </video>

                  <select
                    value={shareReceivers[video.id] || ''}
                    onChange={(e) => handleShareChange(video.id, e.target.value)}
                  >
                    <option value="">Оберіть користувача</option>
                    {users.filter(user => user.id !== currentUser?.id).map(user => (
                      <option key={user.id} value={user.id}>
                        {user.name}
                      </option>

                    ))}
                  </select>
                  <button onClick={() => shareVideo(video.id)}>
                    Поділитись
                  </button>

                  <div className="comment">
                    <h4>Коментарі:</h4>
                    {video.comments?.map((comment, index) => (
                      <p key={index}>
                        <strong>{comment.author}:</strong> {comment.text}
                        {currentUser?.role === 'admin' && (
                          <button onClick={() => deleteComment(video.id, index)}>🗑</button>
                        )}
                      </p>
                    ))}

                    <input type="text" placeholder='Написати коментар...' value={newComments[video.id] || ''} onChange={(e) => handleCommentChange(video.id, e.target.value)}></input>
                    <button onClick={() => submitComment(video.id)}>Відправити</button>
                  </div>
                </div>
              ))
            )}
          </div>

          {sharedVideos.length > 0 && (
            <div className="shared-video-list">
              <h2>Поділені з вами відео</h2>
              {sharedVideos.map(video => (
                <div key={`shared-${video.id}`} className="video-item">
                  <h3>{video.name} (поділено від: {video.sharedvideos.find(s => s.receiverId === currentUser.id)?.sharedBy})</h3>
                  <video width="500" controls>
                    <source src={video.url} type="video/mp4" />
                  </video>

                  <div className="comment">
                    <h4>Коментарі:</h4>
                    {video.comments?.map((comment, index) => (
                      <p key={index}>
                        <strong>{comment.author}:</strong> {comment.text}
                        {currentUser?.role === 'admin' && (
                          <button onClick={() => deleteComment(video.id, index)}>🗑</button>
                        )}
                      </p>
                    ))}

                    <input type="text" placeholder='Написати коментар...' value={newComments[video.id] || ''}
                      onChange={(e) => handleCommentChange(video.id, e.target.value)}>
                    </input>

                    <button onClick={() => submitComment(video.id)}>Відправити</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        
        </>
      )}

    </div >
  );

}

export default App;

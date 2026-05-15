package com.example.lw_3

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// НОВЕ: Імпорти для роботи з Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var myVideos: MutableList<Video>
    private lateinit var myAdapter: VideoAdapter

    private lateinit var activeUser: User
    private lateinit var allUsers: MutableList<User>

    // UI елементи
    private lateinit var rvVideos: RecyclerView
    private lateinit var tvHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvVideos = findViewById(R.id.rvVideos)
        rvVideos.layoutManager = LinearLayoutManager(this)
        tvHeader = findViewById(R.id.textView)

        val loginName = intent.getStringExtra("USER_NAME") ?: "Денис"
        val loginIsAdmin = intent.getBooleanExtra("IS_ADMIN", false)

        allUsers = mutableListOf(
            User(id = 1, name = loginName, role = if (loginIsAdmin) "admin" else "user"),
            User(id = 2, name = "Костя", role = "user"),
            User(id = 3, name = "Марія", role = "user")
        )
        activeUser = allUsers[0]

        // НОВЕ: Обов'язково ініціалізуємо порожній список перед завантаженням з мережі,
        // щоб програма не впала з помилкою UninitializedPropertyAccessException
        myVideos = mutableListOf()

        setupDeveloperPanel()

        // НОВЕ: Завантажуємо відео з нашого Express сервера замість локальних
        loadVideosFromServer()
        loadUsersFromServer()

        val videoPickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Введіть назву відео")
                val input = android.widget.EditText(this)
                builder.setView(input)

                builder.setPositiveButton("Зберегти") { _, _ ->
                    val customName = input.text.toString()
                    val fileName = customName.ifBlank { "Відео без назви" }

                    // Створюємо об'єкт відео для відправки на сервер
                    val newVideo = Video(
                        id = 0, // Сервер сам згенерує правильний ID
                        name = fileName,
                        url = uri.toString(),
                        comments = mutableListOf(),
                        sharedvideos = mutableListOf(),
                        author = activeUser.id
                    )

                    // НОВЕ: Відправляємо нове відео на сервер через Retrofit (POST-запит)
                    RetrofitClient.instance.addVideo(newVideo).enqueue(object : Callback<Video> {
                        override fun onResponse(call: Call<Video>, response: Response<Video>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@MainActivity, "Відео успішно завантажено!", Toast.LENGTH_SHORT).show()
                                loadVideosFromServer() // Оновлюємо список з сервера
                            } else {
                                Toast.makeText(this@MainActivity, "Помилка при збереженні", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Video>, t: Throwable) {
                            Toast.makeText(this@MainActivity, "Помилка мережі: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                builder.setNegativeButton("Скасувати") { dialog, _ ->
                    dialog.cancel()
                }
                builder.show()
            }
        }

        findViewById<Button>(R.id.btnAddVideo).setOnClickListener {
            videoPickerLauncher.launch("video/*")
        }
    }

    // НОВЕ: Метод для отримання відео з Express.js (GET-запит)
    private fun loadVideosFromServer() {
        RetrofitClient.instance.getVideos().enqueue(object : Callback<List<Video>> {
            override fun onResponse(call: Call<List<Video>>, response: Response<List<Video>>) {
                if (response.isSuccessful) {
                    val videosFromServer = response.body()
                    if (videosFromServer != null) {
                        myVideos.clear() // Очищаємо старі дані
                        myVideos.addAll(videosFromServer) // Додаємо ті, що прийшли з сервера
                        updateVideoList() // Перемальовуємо екран
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Не вдалося отримати відео", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Video>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Помилка з'єднання з сервером", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDeveloperPanel() {
        val spinner: Spinner? = findViewById(R.id.spinnerUserSelect)
        val adminSwitch: SwitchCompat? = findViewById(R.id.switchIsAdmin)

        if (spinner != null && adminSwitch != null) {

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allUsers.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            adminSwitch.isClickable = false

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    activeUser = allUsers[position]
                    if(activeUser.isAdmin){
                        adminSwitch.visibility = View.VISIBLE
                        adminSwitch.isChecked = true
                    } else {
                        adminSwitch.visibility = View.GONE
                        adminSwitch.isChecked = false
                    }
                    updateVideoList()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } else {
            updateVideoList()
        }
    }

    private fun updateVideoList() {
        val roleText = if (activeUser.isAdmin) "Адмін" else "Користувач"
        tvHeader.text = getString(R.string.header_format, activeUser.name, roleText)

        val filteredVideos = if (activeUser.isAdmin) {
            myVideos
        } else {
            myVideos.filter { it.id == 1 || it.author == activeUser.id || it.sharedvideos.any{shared -> shared.receiverId == activeUser.id} }.toMutableList()
        }

        myAdapter = VideoAdapter(filteredVideos.toMutableList(), activeUser.name, activeUser.isAdmin, allUsers) { position ->
            val videoToRemove = filteredVideos[position]
            myVideos.remove(videoToRemove)
            updateVideoList()
        }

        rvVideos.adapter = myAdapter
    }

    private fun loadUsersFromServer() {
        RetrofitClient.instance.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val usersFromServer = response.body()
                    if (usersFromServer != null) {
                        allUsers.clear()
                        allUsers.addAll(usersFromServer)
                        setupDeveloperPanel() 
                    }
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {}
        })
    }
}
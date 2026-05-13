package com.example.lw_3

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    // Основний список відео, який зберігається в пам'яті під час роботи програми
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
            User(id = 1, name = loginName, isAdmin = loginIsAdmin),
            User(id = 2, name = "Іван", isAdmin = false),
            User(id = 3, name = "Марія", isAdmin = false)
        )
        activeUser = allUsers[0]


        val localVideoUri = "android.resource://" + packageName + "/" + R.raw.test_video_bille_jean
        myVideos = mutableListOf(
            Video(id = 1, name = "Michael Jackson - Billie Jean", url = localVideoUri, comments = mutableListOf(), sharedVideos = mutableListOf())
        )


        setupDeveloperPanel()


        val videoPickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Введіть назву відео")
                val input = android.widget.EditText(this)
                builder.setView(input)

                builder.setPositiveButton("Зберегти") { dialog, _ ->
                    val customName = input.text.toString()
                    val fileName = if (customName.isNotBlank()) customName else "Відео без назви"
                    val newVideo = Video(
                        id = myVideos.size + 1,
                        name = fileName,
                        url = uri.toString(),
                        comments = mutableListOf(),
                        sharedVideos = mutableListOf(),
                        authorId = activeUser.id
                    )
                    myVideos.add(newVideo)
                    updateVideoList()
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


    private fun setupDeveloperPanel() {
        val spinner: Spinner? = findViewById(R.id.spinnerUserSelect)
        val adminSwitch: SwitchCompat? = findViewById(R.id.switchIsAdmin)

        if (spinner != null && adminSwitch != null) {

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allUsers.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter


            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    activeUser = allUsers[position]
                    adminSwitch.isChecked = activeUser.isAdmin
                    updateVideoList() // Оновлюємо відео під нового юзера
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


            adminSwitch.setOnCheckedChangeListener { _, isChecked ->
                activeUser = activeUser.copy(isAdmin = isChecked)

                val index = allUsers.indexOfFirst { it.id == activeUser.id }
                if (index != -1) allUsers[index] = activeUser

                updateVideoList()
            }
        } else {

            updateVideoList()
        }
    }


    private fun updateVideoList() {
        val roleText = if (activeUser.isAdmin) "Адмін" else "Користувач"
        tvHeader.text = "Відеохостинг | ${activeUser.name} ($roleText)"

        val filteredVideos = if (activeUser.isAdmin) {
            myVideos
        } else {

            myVideos.filter { it.id == 1 || it.authorId == activeUser.id || it.sharedVideos.contains(activeUser.id) }.toMutableList()
        }


        myAdapter = VideoAdapter(filteredVideos.toMutableList(), activeUser.name, activeUser.isAdmin) { position ->
            val videoToRemove = filteredVideos[position]
            myVideos.remove(videoToRemove)
            updateVideoList()
        }

        rvVideos.adapter = myAdapter
    }
}
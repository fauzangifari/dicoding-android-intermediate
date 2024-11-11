package com.dicoding.picodiploma.loginwithanimation.view.main

import MainViewModel
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.maps.MapsActivity
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var storiesListAdapter: StoriesListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observing user session state
        lifecycleScope.launch {
            viewModel.getSession().collectLatest { user ->
                if (!user.isLogin) {
                    startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                    finish()
                }
            }
        }

        // Initializing RecyclerView
        storiesListAdapter = StoriesListAdapter()
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = storiesListAdapter
        }

        // Fetch and display stories
        fetchStories(location = 0)

        // Add new story button click listener
        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

        // Collecting new story updates
        lifecycleScope.launch {
            viewModel.newStory.collectLatest { newStory ->
                newStory?.let {
                    storiesListAdapter.addStoryToTop(it)
                }
            }
        }
    }

    private fun fetchStories(location: Int?) {
        location?.let {
            viewModel.getStoriesStream(it).observe(this) { pagingData ->
                storiesListAdapter.submitData(lifecycle, pagingData)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                viewModel.logout()
                true
            }
            R.id.maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.location_0 -> {
                fetchStories(0)
                true
            }
            R.id.location_1 -> {
                fetchStories(1)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

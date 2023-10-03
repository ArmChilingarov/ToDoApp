package com.example.todolist.presentation

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.todolist.R
import com.example.todolist.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val viewModel: MainActivityViewModel by viewModels()
    var showMenuItems = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.appBarMain.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.base_fragment, R.id.completed_tasks_fragment
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.base_fragment -> {
                    supportActionBar?.setTitle(R.string.tasks)
                    showMenuItems = true
                }

                R.id.completed_tasks_fragment -> {
                    supportActionBar?.setTitle(R.string.completed)
                    showMenuItems = false

                }

                R.id.new_task_fragment -> {
                    supportActionBar?.setTitle(R.string.new_task)
                    showMenuItems = false
                }

                R.id.task_category_fragment -> {
                    showMenuItems = false
                    // handled in fragment
                }
            }
            invalidateMenu()
        }
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        menu?.forEach {
            it.isVisible = showMenuItems
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort -> {
                createSortPopupMenu()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun createSortPopupMenu() {
        PopupMenu(this, binding.appBarMain.toolbar, Gravity.END).apply {
            // MainActivity implements OnMenuItemClickListener.
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.by_date -> {
                        viewModel.sortTasks(MainActivityViewModel.Sort.DATE)
                        true
                    }

                    R.id.by_category -> {
                        viewModel.sortTasks(MainActivityViewModel.Sort.CATEGORY)
                        true
                    }

                    R.id.by_name -> {
                        viewModel.sortTasks(MainActivityViewModel.Sort.NAME)
                        true
                    }

                    else -> false
                }
            }
            inflate(R.menu.sort_menu)
            show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}
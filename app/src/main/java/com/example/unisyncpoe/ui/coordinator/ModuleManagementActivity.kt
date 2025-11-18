package com.example.unisyncpoe.ui.coordinator

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unisyncpoe.R
import com.example.unisyncpoe.databinding.ActivityModuleManagementBinding
import com.example.unisyncpoe.data.model.Module
import com.example.unisyncpoe.util.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Coordinator activity for managing modules
 */
@AndroidEntryPoint
class ModuleManagementActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityModuleManagementBinding
    private val viewModel: ModuleManagementViewModel by viewModels()
    
    @Inject
    lateinit var authManager: AuthManager
    
    private lateinit var modulesAdapter: ArrayAdapter<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
        initializeDummyModules()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.manage_modules)
        
        // Setup modules list
        modulesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        binding.listViewModules.adapter = modulesAdapter
        
        binding.listViewModules.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectModule(position)
            showModuleDetails(viewModel.getSelectedModule())
        }
        
        binding.fabAddModule.setOnClickListener {
            showAddModuleDialog()
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.loadModules()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.modules.collect { modules ->
                updateModulesList(modules)
            }
        }
    }
    
    private fun updateModulesList(modules: List<Module>) {
        val moduleStrings = modules.map { "${it.code} - ${it.name} (${it.credits} credits)" }
        modulesAdapter.clear()
        modulesAdapter.addAll(moduleStrings)
        modulesAdapter.notifyDataSetChanged()
    }
    
    private fun showModuleDetails(module: Module?) {
        if (module == null) return
        
        AlertDialog.Builder(this)
            .setTitle("Module Details")
            .setMessage("Code: ${module.code}\nName: ${module.name}\nCredits: ${module.credits}\nSemester: ${module.semesterId ?: "Not assigned"}")
            .setPositiveButton("Edit") { _, _ ->
                showEditModuleDialog(module)
            }
            .setNeutralButton("Delete") { _, _ ->
                showDeleteConfirmation(module)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAddModuleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_module, null)
        val etCode = dialogView.findViewById<android.widget.EditText>(R.id.etModuleCode)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etModuleName)
        val etCredits = dialogView.findViewById<android.widget.EditText>(R.id.etModuleCredits)
        val spinnerSemester = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerSemester)
        
        // Setup semester spinner with dummy data
        val semesters = arrayOf("Semester 1", "Semester 2", "Not assigned")
        val semesterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSemester.adapter = semesterAdapter
        
        AlertDialog.Builder(this)
            .setTitle("Add Module")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val code = etCode.text.toString().trim()
                val name = etName.text.toString().trim()
                val credits = etCredits.text.toString().toIntOrNull() ?: 0
                
                if (code.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                viewModel.addModule(code, name, credits)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditModuleDialog(module: Module) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_module, null)
        val etCode = dialogView.findViewById<android.widget.EditText>(R.id.etModuleCode)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etModuleName)
        val etCredits = dialogView.findViewById<android.widget.EditText>(R.id.etModuleCredits)
        val spinnerSemester = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerSemester)
        
        etCode.setText(module.code)
        etName.setText(module.name)
        etCredits.setText(module.credits.toString())
        
        // Setup semester spinner
        val semesters = arrayOf("Semester 1", "Semester 2", "Not assigned")
        val semesterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSemester.adapter = semesterAdapter
        
        AlertDialog.Builder(this)
            .setTitle("Edit Module")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val code = etCode.text.toString().trim()
                val name = etName.text.toString().trim()
                val credits = etCredits.text.toString().toIntOrNull() ?: 0
                
                if (code.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                viewModel.updateModule(module.id, code, name, credits)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteConfirmation(module: Module) {
        AlertDialog.Builder(this)
            .setTitle("Delete Module")
            .setMessage("Are you sure you want to delete ${module.code} - ${module.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteModule(module.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun initializeDummyModules() {
        viewModel.initializeDummyModules()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}


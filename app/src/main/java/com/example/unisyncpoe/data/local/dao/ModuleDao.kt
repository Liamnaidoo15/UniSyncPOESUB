package com.example.unisyncpoe.data.local.dao

import androidx.room.*
import com.example.unisyncpoe.data.model.Module
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Query("SELECT * FROM modules ORDER BY code ASC")
    fun getAllModules(): Flow<List<Module>>
    
    @Query("SELECT * FROM modules WHERE id = :id")
    suspend fun getModuleById(id: String): Module?
    
    @Query("SELECT * FROM modules WHERE semesterId = :semesterId")
    fun getModulesBySemester(semesterId: String): Flow<List<Module>>
    
    @Query("SELECT * FROM modules WHERE coordinatorId = :coordinatorId")
    fun getModulesByCoordinator(coordinatorId: String): Flow<List<Module>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: Module)
    
    @Update
    suspend fun updateModule(module: Module)
    
    @Delete
    suspend fun deleteModule(module: Module)
}


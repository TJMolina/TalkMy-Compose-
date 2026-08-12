package com.example.talkmy.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.talkmy.domain.models.Task

@Entity(tableName = "tasks_table")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "note") val note: String,
    @ColumnInfo(name = "date") val date: Long
)

/** Mapper: From Database Entity to Domain Model */
fun TaskEntity.toDomain() = Task(
    id = id,
    note = note,
    date = date
)

/** Mapper: From Domain Model to Database Entity */
fun Task.toDatabase() = TaskEntity(
    id = id,
    note = note,
    date = date
)

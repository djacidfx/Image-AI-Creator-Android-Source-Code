package br.com.codapp.imagecreator.data.local.entity

import androidx.room.*
import java.util.*

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val imageId: Long = 0,
    val url: String,
    var path: String,
    val createdAt: Date
)
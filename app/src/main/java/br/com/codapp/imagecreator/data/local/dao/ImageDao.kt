package br.com.codapp.imagecreator.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import br.com.codapp.imagecreator.data.local.entity.ImageEntity

@Dao
interface ImageDao {

    @Insert
    fun insertImage(imageEntity: ImageEntity)

    @Insert
    fun insertImageList(imageEntityList: List<ImageEntity>)

    @Delete
    fun deleteImage(imageEntity: ImageEntity)

    @Query("SELECT * FROM images")
    fun getAllImages(): List<ImageEntity>

}
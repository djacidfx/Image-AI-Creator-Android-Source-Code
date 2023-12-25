package br.com.codapp.imagecreator.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.codapp.imagecreator.data.local.converter.DateConverter
import br.com.codapp.imagecreator.data.local.dao.ImageDao
import br.com.codapp.imagecreator.data.local.entity.ImageEntity

@Database(entities = [
    ImageEntity::class
],
    version = 1
)
@TypeConverters(DateConverter::class)
abstract class AiDb : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}
package com.hasanbilgin.artbookkotlin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class ArtModel(

    @ColumnInfo(name = "artname") var artname: String,
    @ColumnInfo(name = "artistname") var artistname: String,
    @ColumnInfo(name = "year") var year: String,
    @ColumnInfo(name = "image") var image: ByteArray

) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0;
}

//class Art(val name: String, val id: Int) {
//}
package com.hasanbilgin.artbookkotlin.roomdb

import androidx.room.*
import com.hasanbilgin.artbookkotlin.model.ArtModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface ArtDao {


    //ArtModel modelin sınıf ismidir
    @Query("SELECT * FROM ArtModel")
    fun getAll(): Flowable<List<ArtModel>>

    @Query("SELECT * FROM ArtModel WHERE id = :id")
    fun getArtById(id: Int): Flowable<ArtModel>

    @Insert
    fun insert(artModel: ArtModel): Completable

    @Delete
    fun delete(artModel: ArtModel): Completable

    @Query("UPDATE ArtModel SET artname= :artName,artistName= :artistName,year= :year , image= :image WHERE id= :id")
    fun updateQuery(id: Int,artName:String,artistName:String,year:String,image:ByteArray): Completable

}
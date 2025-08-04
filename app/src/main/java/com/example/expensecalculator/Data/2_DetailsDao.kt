package com.example.expensecalculator.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.expensecalculator.Data.Detail

import kotlinx.coroutines.flow.Flow

@Dao
/*// Data Access Object . DAO is responsible for defining methods that access the database: insert, delete, update query etc. Room generated the actual code at the compile time . We can not use Room without a DAO
// we use an interface here because we have to define the method signature only here in the interface nothing other than that. The Room will generate the code at the compile time by itself
// we could also have used the abstract class other than the interface but still we have to used the abstract keyword everytime and the room will generate the logic in that case also so using an interface in an ideal thing here
 */


interface DetailDao{
    @Insert
    suspend fun insertDetail(detail: Detail)

    @Delete
    suspend fun deleteDetail(detail: Detail)

    @Update
    suspend fun updateDetail(detail: Detail)

    @Query("Select  * from details order by id desc")
    fun getAllDetails():Flow<List<Detail>>
}
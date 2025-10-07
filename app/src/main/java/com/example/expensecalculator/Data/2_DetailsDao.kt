package com.example.expensecalculator.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao

interface AccountDao {
    @Insert
    suspend fun addAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query("SELECT * FROM accounts ORDER BY id DESC")
    fun getAllAccounts(): Flow<List<Account>>

    @Transaction
    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccountWithExpenses(accountId: Int): Flow<AccountWithExpenses>
}
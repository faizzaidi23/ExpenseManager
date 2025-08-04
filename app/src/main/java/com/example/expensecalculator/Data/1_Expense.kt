package com.example.expensecalculator.Data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName="details")

data class Detail(
    @PrimaryKey(autoGenerate = true)
    val id:Int?=null,
    val name: String,
    val description:String?=null,
    val amount: Double
)

@Entity(
    tableName="expenses",
    //Designing a foreign key link here
    foreignKeys = [ // We can define more than one foreign key for a single entity. The foreignKeys parameter takes an Array of @ForeignKey annotations, which is why is is written inside the square brackets. We can simply add more ForeignKey definitions to this array
        ForeignKey(
            entity= Detail::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE // This is optional : This deletes the expenses if the parent account is deleted
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id:Int?=null,
    val description: String?=null,
    val amount: Double,
    val accountId: Int?=null
)
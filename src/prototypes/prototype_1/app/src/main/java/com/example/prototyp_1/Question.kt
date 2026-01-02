package com.example.prototyp_1

//Beinhaltet alle Fragetypen

import java.time.LocalDate

sealed class Question {
    abstract val id: Int
    abstract val title: String
    abstract val explanation: String
}

data class QuestionSlider(
    override val id: Int,
    override val title: String,
    override val explanation: String,
    val sliderMin: Float,
    val sliderMax: Float
) : Question()

data class QuestionTime(
    override val id: Int,
    override val title: String,
    override val explanation: String
) : Question()

data class QuestionMultipleChoice(
    override val id: Int,
    override val title: String,
    override val explanation: String,
    val choices: List<String>
) : Question()

data class QuestionYesNo(
    override val id: Int,
    override val title: String,
    override val explanation: String
) : Question()

data class QuestionDate(
    override val id: Int,
    override val title: String,
    override val explanation: String
) : Question()
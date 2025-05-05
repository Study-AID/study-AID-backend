from typing import List, Literal, Union

from pydantic import BaseModel


class TrueFalseQuestion(BaseModel):
    """Represents a true/false question"""
    type: Literal["true_or_false"]
    question: str
    answer: bool
    explanation: str


class MultipleChoiceOption(BaseModel):
    """Represents an option in a multiple choice question"""
    text: str
    is_correct: bool


class MultipleChoiceQuestion(BaseModel):
    """Represents a multiple choice question"""
    type: Literal["multiple_choice"]
    question: str
    options: List[MultipleChoiceOption]
    explanation: str


class ShortAnswerQuestion(BaseModel):
    """Represents a short answer question"""
    type: Literal["short_answer"]
    question: str
    answer: str
    explanation: str


class EssayQuestion(BaseModel):
    """Represents an essay question"""
    type: Literal["essay"]
    question: str
    answer: str
    explanation: str


QuizQuestion = Union[TrueFalseQuestion, MultipleChoiceQuestion, ShortAnswerQuestion, EssayQuestion]


class QuizResponse(BaseModel):
    """Represents the complete quiz response"""
    quiz_questions: List[QuizQuestion]

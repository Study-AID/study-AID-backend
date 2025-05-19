from typing import List, Literal, Union

from pydantic import BaseModel


class TrueFalseQuestion(BaseModel):
    """Represents a true/false question"""
    question_type: Literal["true_or_false"]
    question: str
    answer: bool
    explanation: str
    points: float = 10.0


class MultipleChoiceOption(BaseModel):
    """Represents an option in a multiple choice question"""
    text: str
    is_correct: bool


class MultipleChoiceQuestion(BaseModel):
    """Represents a multiple choice question"""
    question_type: Literal["multiple_choice"]
    question: str
    options: List[MultipleChoiceOption]
    explanation: str
    points: float = 10.0


class ShortAnswerQuestion(BaseModel):
    """Represents a short answer question"""
    question_type: Literal["short_answer"]
    question: str
    answer: str
    explanation: str
    points: float = 10.0


class EssayQuestion(BaseModel):
    """Represents an essay question"""
    question_type: Literal["essay"]
    question: str
    answer: str
    explanation: str
    points: float = 20.0


ExamQuestion = Union[TrueFalseQuestion, MultipleChoiceQuestion, ShortAnswerQuestion, EssayQuestion]


class ExamResponse(BaseModel):
    """Represents the complete exam response"""
    exam_questions: List[ExamQuestion]

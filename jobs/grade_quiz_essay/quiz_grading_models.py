from typing import List
from pydantic import BaseModel


class ScoringCriterion(BaseModel):
    """개별 채점 기준 항목"""
    name: str  # 예: "장단점 언급"
    description: str  # 예: "각 정규화 방법의 장단점을 최소 1개 이상 언급"
    max_points: float  # 예: 4.0
    earned_points: float  # 예: 1.0


class EssayCriteriaAnalysis(BaseModel):
    """구조화된 채점 기준 결과"""
    criteria: List[ScoringCriterion]  # 위의 ScoringCriterion 모델 리스트
    analysis: str      # 사용자 답변에 대한 종합 평가(각 기준에 대해 사용자 답변을 평가한 것들의 집합)
                       # 예: 정규화의 기본 개념과 예시는 잘 이해하고 있으나, 단점에 대한 설명이 부족하며, 한 가지의 정규화 방법만 언급했습니다.



class GradeEssayResponse(BaseModel):
    score: float # quiz_responses.score에 저장하는 용도
    essay_criteria_analysis: EssayCriteriaAnalysis # quiz_responses.essay_criteria_analysis에 저장하는 용도
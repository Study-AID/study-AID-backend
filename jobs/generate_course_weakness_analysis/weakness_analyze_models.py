from pydantic import BaseModel

class CourseWeaknessAnalysisResponse(BaseModel):
    weaknesses: str      # 약점 (텍스트)
    suggestions: str     # 학습 제안 (텍스트)
    analyzed_at: str     # 분석 일시
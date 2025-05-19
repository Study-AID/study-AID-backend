"""
파싱된 텍스트를 위한 데이터 모델 정의
"""

from typing import List
from pydantic import BaseModel


class ParsedPage(BaseModel):
    """특정 페이지의 파싱된 텍스트를 나타냅니다"""
    page_number: int
    text: str


class ParsedText(BaseModel):
    """강의 자료의 전체 파싱된 텍스트를 나타냅니다"""
    total_pages: int
    pages: List[ParsedPage]

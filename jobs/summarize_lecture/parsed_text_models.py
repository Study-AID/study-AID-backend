from pydantic import BaseModel
from typing import List


class ParsedPage(BaseModel):
    pageNumber: int
    text: str


class ParsedText(BaseModel):
    totalPages: int
    pages: List[ParsedPage]

from pydantic import BaseModel
from typing import List


class ParsedPage(BaseModel):
    page_number: int
    text: str


class ParsedText(BaseModel):
    total_pages: int
    pages: List[ParsedPage]

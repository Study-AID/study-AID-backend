from typing import List

from pydantic import BaseModel


class ParsedPage(BaseModel):
    page_number: int
    text: str


class ParsedText(BaseModel):
    total_pages: int
    pages: List[ParsedPage]

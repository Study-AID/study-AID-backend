from typing import List, Optional, Any
from pydantic import BaseModel


class Metadata(BaseModel):
    model: str
    created_at: str


class PageRange(BaseModel):
    start_page: int
    end_page: int


class Keyword(BaseModel):
    keyword: str
    description: str
    relevance: float
    page_range: PageRange


class TopicDetails(BaseModel):
    title: str
    description: str
    page_range: PageRange
    additional_details: List[str] = []
    sub_topics: List['TopicDetails'] = []


class Summary(BaseModel):
    metadata: Metadata
    overview: str
    keywords: List[Keyword]
    topics: List[TopicDetails]
    additional_references: List[str] = []

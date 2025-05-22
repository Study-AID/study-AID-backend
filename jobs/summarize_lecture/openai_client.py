import concurrent.futures
import json
import logging
import os
import random
import time
import yaml
from datetime import datetime
from openai import OpenAI
from tenacity import retry, stop_after_attempt, wait_random_exponential, retry_if_exception_type
from typing import List, Dict, Any, Optional

from summary_models import Summary

logger = logging.getLogger(__name__)

OVERVIEW_MERGE_SYSTEM_PROMPT = """당신은 문서 요약을 통합하는 전문가입니다. 여러 요약을 하나의 일관되고 자연스러운 요약으로 통합해주세요."""

OVERVIEW_MERGE_USER_PROMPT_TEMPLATE = """다음은 긴 문서의 여러 섹션에 대한 요약입니다. 이 요약들을 하나의 일관되고 자연스러운 전체 요약으로 통합해주세요.
문체와 어조를 일관되게 유지하고, 중복된 내용은 제거하며, 모든 중요한 정보를 포함해야 합니다.
결과는 하나의 통합된 개요여야 합니다.

섹션 요약:

{overviews_json}
"""


class OpenAIClient:
    def __init__(self):
        self.api_key = os.environ.get("OPENAI_API_KEY")
        if not self.api_key:
            raise ValueError("OPENAI_API_KEY environment variable not set")

        # OpenAI 클라이언트 생성 시 타임아웃 설정 추가
        self.client = OpenAI(
            api_key=self.api_key,
            timeout=300.0,  # 5분 타임아웃
            max_retries=0,   # 재시도 비활성화
        )
        self.model = os.environ.get("OPENAI_MODEL", "gpt-4o")

        # Maximum number of concurrent API calls
        self.max_concurrent = int(os.environ.get("MAX_CONCURRENT_CHUNKS", "1"))  # 기본값을 1로 낮춤

        # Prompt cache to avoid redundant loading
        self._prompt_cache = {}

    def load_prompt_template(self, prompt_path):
        """Load a prompt template from a file, with caching."""
        # Check if the template is already cached
        if prompt_path in self._prompt_cache:
            logger.debug(f"Using cached prompt template for {prompt_path}")
            return self._prompt_cache[prompt_path]

        try:
            with open(prompt_path, "r", encoding="utf-8") as file:
                template = yaml.safe_load(file)
                # Cache the template for future use
                self._prompt_cache[prompt_path] = template
                return template
        except Exception as e:
            logger.error(f"Error loading prompt template: {e}")
            raise

    # TODO(mj): pass the langauge user selected.
    def generate_summary(self, formatted_content, prompt_path, language="한국어"):
        """Generate a summary for lecture content. For backward compatibility."""
        try:
            # Create a single chunk with the entire content
            chunk = {
                "chunk_id": 0,
                "total_chunks": 1,
                "start_page": formatted_content[0]["page"] if formatted_content else 1,
                "end_page": formatted_content[-1]["page"] if formatted_content else 1,
                "formatted_content": formatted_content
            }

            # Use the chunk-based method for consistency
            return self.generate_chunk_summary(chunk, prompt_path, language)
        except Exception as e:
            logger.error(f"Error generating summary: {e}")
            raise

    def _add_jitter_delay(self, chunk_id=0, total_chunks=1):
        """Add random jitter delay before API request to prevent thundering herd"""
        if total_chunks > 1:  # 병렬 처리일 때만 적용
            # 청크 ID에 따라 기본 딜레이 + 랜덤 jitter
            base_delay = chunk_id * 0.5  # 각 청크마다 0.5초씩 차이
            jitter = random.uniform(0, 2.0)  # 0-2초 랜덤 딜레이
            total_delay = base_delay + jitter

            logger.info(f"Adding jitter delay of {total_delay:.2f}s for chunk {chunk_id + 1}/{total_chunks}")
            time.sleep(total_delay)

    @retry(wait=wait_random_exponential(min=5, max=60), stop=stop_after_attempt(2),
           retry=retry_if_exception_type((TimeoutError, ConnectionError)))
    def _complete_with_backoff(self, **kwargs):
        """Make an OpenAI API request with exponential backoff retry logic."""
        try:
            logger.info(f"Making OpenAI API request for model: {kwargs.get('model', self.model)}")
            response = self.client.chat.completions.create(**kwargs)
            logger.info("OpenAI API request successful")
            return response
        except Exception as e:
            logger.warning(f"OpenAI API request failed: {type(e).__name__}: {str(e)}")
            # 재시도 가능한 예외인지 확인
            if isinstance(e, (TimeoutError, ConnectionError)):
                logger.warning("Will retry due to timeout/connection error")
                raise
            else:
                logger.error("Non-retryable error, failing immediately")
                raise

    # TODO(mj): pass the langauge user selected.
    def generate_chunk_summary(self, chunk, prompt_path, language="한국어"):
        """Generate a summary for a single chunk of lecture content."""
        try:
            template = self.load_prompt_template(prompt_path)
            model_name = template.get("model", self.model)

            # Extract chunk metadata
            chunk_id = chunk["chunk_id"]
            total_chunks = chunk["total_chunks"]
            start_page = chunk["start_page"]
            end_page = chunk["end_page"]
            formatted_content = chunk["formatted_content"]

            # Log chunk processing start
            logger.info(f"Processing chunk {chunk_id + 1}/{total_chunks} (pages {start_page}-{end_page})")

            # Add jitter delay for parallel processing
            self._add_jitter_delay(chunk_id, total_chunks)

            # Format the user message with the lecture content
            user_message = template["user"].format(
                language=language,
                lecture_content=json.dumps(formatted_content, ensure_ascii=False, separators=(',', ':'))
            )

            # Add chunk context to the user message if this is part of multiple chunks
            if total_chunks > 1:
                chunk_context = f"This is chunk {chunk_id + 1} of {total_chunks} from the lecture material, covering pages {start_page} to {end_page}."
                user_message = f"{chunk_context}\n\n{user_message}"

            # Call the OpenAI API with retry logic
            response = self._complete_with_backoff(
                model=model_name,
                messages=[
                    {
                        "role": "system",
                        "content": template["system"],
                    },
                    {
                        "role": "user",
                        "content": user_message,
                    }
                ],
                temperature=template["temperature"],
                max_tokens=template["max_tokens"],
                response_format={"type": "json_object"}
            )

            # Get the JSON response from the LLM
            response_content = response.choices[0].message.content

            # Parse the JSON response
            summary_json = json.loads(response_content)

            # Add metadata
            if "metadata" not in summary_json:
                summary_json["metadata"] = {
                    "model": model_name,
                    "created_at": datetime.now().isoformat()
                }

            # Add chunk information to metadata if this is part of multiple chunks
            if total_chunks > 1:
                summary_json["metadata"]["chunk_info"] = {
                    "chunk_id": chunk_id,
                    "total_chunks": total_chunks,
                    "start_page": start_page,
                    "end_page": end_page
                }

            # Create a Summary object from the JSON response
            summary_obj = Summary.model_validate(summary_json)

            # Return the validated summary object
            return summary_obj
        except Exception as e:
            logger.error(f"Error generating summary for chunk {chunk['chunk_id']}: {e}")
            raise

    def merge_summaries(self, summaries: List[Summary]) -> Summary:
        """Merge multiple summaries from different chunks into a single summary."""
        if not summaries:
            raise ValueError("No summaries provided for merging")

        if len(summaries) == 1:
            return summaries[0]

        # Use the metadata from the first summary
        merged_metadata = summaries[0].metadata.model_dump()
        merged_metadata["created_at"] = datetime.now().isoformat()

        # Regenerate a consolidated overview using the OpenAI API
        overview_parts = [summary.overview for summary in summaries]

        # Only regenerate if there are multiple chunks
        if len(overview_parts) > 1:
            try:
                logger.info("Regenerating consolidated overview from multiple chunks...")

                # Call OpenAI API to regenerate the overview
                response = self._complete_with_backoff(
                    model=merged_metadata.get("model", self.model),
                    messages=[
                        {
                            "role": "system",
                            "content": OVERVIEW_MERGE_SYSTEM_PROMPT
                        },
                        {
                            "role": "user",
                            "content": OVERVIEW_MERGE_USER_PROMPT_TEMPLATE.format(
                                overviews_json=json.dumps(overview_parts, ensure_ascii=False, separators=(',', ':'))
                            )
                        }
                    ],
                    temperature=0.3,  # 낮은 temperature로 일관성 있는 결과 생성
                    max_tokens=1024  # 충분한 토큰 할당
                )

                # Get the merged overview from the response
                merged_overview = response.choices[0].message.content.strip()
                logger.info("Successfully regenerated consolidated overview")

            except Exception as e:
                # Fallback to simple concatenation if regeneration fails
                logger.warning(f"Failed to regenerate overview: {e}. Falling back to simple concatenation.")
                merged_overview = "\n\n".join(overview_parts)
        else:
            # If there's only one overview, use it directly
            merged_overview = overview_parts[0]

        # Combine keywords (removing duplicates)
        all_keywords = []
        seen_keywords = set()
        for summary in summaries:
            for keyword in summary.keywords:
                # Skip if we've already seen this keyword
                if keyword.keyword in seen_keywords:
                    continue
                seen_keywords.add(keyword.keyword)
                all_keywords.append(keyword)

        # Combine topics
        all_topics = []
        for summary in summaries:
            all_topics.extend(summary.topics)

        # Combine additional references
        all_references = []
        seen_references = set()
        for summary in summaries:
            for ref in summary.additional_references:
                if ref not in seen_references:
                    seen_references.add(ref)
                    all_references.append(ref)

        # Create the merged summary
        merged_summary = Summary(
            metadata={
                "model": merged_metadata["model"],
                "created_at": merged_metadata["created_at"]
            },
            overview=merged_overview,
            keywords=all_keywords,
            topics=all_topics,
            additional_references=all_references
        )

        return merged_summary

    def process_chunks_in_parallel(self, chunks, prompt_path, language="한국어"):
        """Process multiple chunks in parallel and return merged summary."""
        # If there's only one chunk, just process it directly
        if len(chunks) == 1:
            return self.generate_chunk_summary(chunks[0], prompt_path, language)

        results = []

        with concurrent.futures.ThreadPoolExecutor(max_workers=self.max_concurrent) as executor:
            # Start processing all chunks
            future_to_chunk = {}
            for chunk in chunks:
                future = executor.submit(
                    self.generate_chunk_summary,
                    chunk,
                    prompt_path,
                    language
                )
                future_to_chunk[future] = chunk

            # Process results as they complete
            for future in concurrent.futures.as_completed(future_to_chunk):
                chunk = future_to_chunk[future]
                try:
                    summary = future.result()
                    results.append((chunk["chunk_id"], summary))
                    logger.info(
                        f"Completed chunk {chunk['chunk_id'] + 1}/{len(chunks)} (pages {chunk['start_page']}-{chunk['end_page']})")
                except Exception as e:
                    logger.error(f"Error processing chunk {chunk['chunk_id']}: {e}")
                    raise

        # Sort results by chunk_id to maintain order
        results.sort(key=lambda x: x[0])
        summaries = [summary for _, summary in results]

        # Log merging process
        logger.info(f"All {len(chunks)} chunks processed. Merging summaries...")

        # Merge all summaries
        merged_summary = self.merge_summaries(summaries)
        logger.info("Summary merging completed successfully")

        return merged_summary

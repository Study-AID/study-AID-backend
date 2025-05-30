import concurrent.futures
import json
import logging
import os
import random
from datetime import datetime
from typing import List, Dict, Any

import yaml
from openai import OpenAI, RateLimitError
from tenacity import retry, stop_after_attempt, wait_random_exponential, retry_if_exception_type

from quiz_models import QuizResponse

logger = logging.getLogger(__name__)


class OpenAIClient:
    def __init__(self):
        self.api_key = os.environ.get("OPENAI_API_KEY")
        if not self.api_key:
            raise ValueError("OPENAI_API_KEY environment variable not set")

        # OpenAI 클라이언트 생성 시 타임아웃 설정 추가
        self.client = OpenAI(
            api_key=self.api_key,
            timeout=300.0,  # 5분 타임아웃
            max_retries=0,
        )
        self.model = os.environ.get("OPENAI_MODEL", "gpt-4o")

        # Maximum number of concurrent API calls
        self.max_concurrent = int(os.environ.get("MAX_CONCURRENT_CHUNKS", "2"))

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

    @retry(
        wait=wait_random_exponential(min=10, max=60),
        stop=stop_after_attempt(2),
        retry=retry_if_exception_type((RateLimitError, TimeoutError, ConnectionError))
    )
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
            if isinstance(e, (RateLimitError, TimeoutError, ConnectionError)):
                logger.warning("Will retry due to rate limit/timeout/connection error")
                raise
            else:
                logger.error("Non-retryable error, failing immediately")
                raise

    def _distribute_question_counts(self, question_counts: Dict[str, int], total_chunks: int) -> List[Dict[str, int]]:
        """Distribute question counts across chunks as evenly as possible."""
        chunk_counts = []

        for chunk_id in range(total_chunks):
            chunk_count = {}

            for question_type, total_count in question_counts.items():
                if total_count == 0:
                    chunk_count[question_type] = 0
                    continue

                # Calculate base count per chunk
                base_count = total_count // total_chunks
                remainder = total_count % total_chunks

                # Add one extra question to the first 'remainder' chunks
                if chunk_id < remainder:
                    chunk_count[question_type] = base_count + 1
                else:
                    chunk_count[question_type] = base_count

            chunk_counts.append(chunk_count)

        # Log distribution
        logger.info(f"Question distribution across {total_chunks} chunks:")
        for i, counts in enumerate(chunk_counts):
            total_for_chunk = sum(counts.values())
            logger.info(f"  Chunk {i + 1}: {counts} (total: {total_for_chunk})")

        return chunk_counts

    def generate_chunk_quiz(self, chunk: Dict[str, Any], question_counts: Dict[str, int], prompt_path: str,
                            language="한국어") -> QuizResponse:
        """Generate quiz questions for a single chunk of lecture content."""
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
            logger.info(f"Question counts for this chunk: {question_counts}")

            # Convert formatted_content to JSON string for the prompt
            lecture_content_json = json.dumps(formatted_content, ensure_ascii=False, separators=(',', ':'))

            # Format the user message with the lecture content and question counts by type
            user_message = template["user"].format(
                language=language,
                lecture_content=lecture_content_json,
                true_or_false_count=question_counts.get('true_or_false_count', 0),
                multiple_choice_count=question_counts.get('multiple_choice_count', 0),
                short_answer_count=question_counts.get('short_answer_count', 0),
                essay_count=question_counts.get('essay_count', 0)
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
            quiz_data = json.loads(response_content)

            # Add metadata
            if "metadata" not in quiz_data:
                quiz_data["metadata"] = {
                    "model": model_name,
                    "created_at": datetime.now().isoformat(),
                    "language": language
                }

            # Add chunk information to metadata if this is part of multiple chunks
            if total_chunks > 1:
                quiz_data["metadata"]["chunk_info"] = {
                    "chunk_id": chunk_id,
                    "total_chunks": total_chunks,
                    "start_page": start_page,
                    "end_page": end_page
                }

            # Create a QuizResponse object from the JSON response
            quiz_response = QuizResponse.model_validate(quiz_data)

            logger.info(f"Generated {len(quiz_response.quiz_questions)} questions for chunk {chunk_id + 1}")
            return quiz_response

        except Exception as e:
            logger.error(f"Error generating quiz for chunk {chunk['chunk_id']}: {e}")
            raise

    def merge_quiz_responses(self, quiz_responses: List[QuizResponse]) -> QuizResponse:
        """Merge multiple quiz responses from different chunks into a single response."""
        if not quiz_responses:
            raise ValueError("No quiz responses provided for merging")

        if len(quiz_responses) == 1:
            return quiz_responses[0]

        # Combine all quiz questions
        all_questions = []
        for quiz_response in quiz_responses:
            all_questions.extend(quiz_response.quiz_questions)

        # Shuffle questions to mix questions from different chunks
        random.shuffle(all_questions)

        # Create the merged quiz response
        merged_quiz = QuizResponse(
            quiz_questions=all_questions
        )

        logger.info(f"Merged {len(quiz_responses)} quiz chunks into single quiz with {len(all_questions)} questions")
        return merged_quiz

    def process_chunks_in_parallel(self, chunks: List[Dict[str, Any]], question_counts: Dict[str, int],
                                   prompt_path: str, language="한국어") -> QuizResponse:
        """Process multiple chunks in parallel and return merged quiz response."""
        # If there's only one chunk, just process it directly
        if len(chunks) == 1:
            chunk_question_counts = question_counts.copy()
            return self.generate_chunk_quiz(chunks[0], chunk_question_counts, prompt_path, language)

        # Distribute question counts across chunks
        distributed_counts = self._distribute_question_counts(question_counts, len(chunks))

        results = []

        with concurrent.futures.ThreadPoolExecutor(max_workers=self.max_concurrent) as executor:
            # Start processing all chunks
            future_to_chunk = {}
            for i, chunk in enumerate(chunks):
                chunk_counts = distributed_counts[i]
                future = executor.submit(
                    self.generate_chunk_quiz,
                    chunk,
                    chunk_counts,
                    prompt_path,
                    language
                )
                future_to_chunk[future] = chunk

            # Process results as they complete
            for future in concurrent.futures.as_completed(future_to_chunk):
                chunk = future_to_chunk[future]
                try:
                    quiz_response = future.result()
                    results.append((chunk["chunk_id"], quiz_response))
                    logger.info(
                        f"Completed chunk {chunk['chunk_id'] + 1}/{len(chunks)} (pages {chunk['start_page']}-{chunk['end_page']})")
                except Exception as e:
                    logger.error(f"Error processing chunk {chunk['chunk_id']}: {e}")
                    raise

        # Sort results by chunk_id to maintain order for logging
        results.sort(key=lambda x: x[0])
        quiz_responses = [quiz_response for _, quiz_response in results]

        # Log merging process
        logger.info(f"All {len(chunks)} chunks processed. Merging quiz responses...")

        # Merge all quiz responses
        merged_quiz = self.merge_quiz_responses(quiz_responses)
        logger.info("Quiz merging completed successfully")

        return merged_quiz

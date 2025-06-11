import json
import logging
import os

import yaml
from openai import OpenAI
from tenacity import retry, stop_after_attempt, wait_random_exponential, retry_if_exception_type

from weakness_analyze_models import CourseWeaknessAnalysisResponse

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
            max_retries=0,   # 재시도 비활성화
        )
        self.model = os.environ.get("OPENAI_MODEL", "gpt-4o")

    def load_prompt_template(self, prompt_path):
        try:
            with open(prompt_path, "r", encoding="utf-8") as file:
                template = yaml.safe_load(file)
                return template
        except Exception as e:
            logger.error(f"Error loading prompt template: {e}")
            raise

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


    def analyze_new_weakness(self, course_name, new_incorrect_items, prompt_path):
        """
        새로운 틀린 문제들을 기반으로 약점 분석을 수행합니다.
        """
        try:
            template = self.load_prompt_template(prompt_path)

            user_message = template["user"].format(
                course_name=course_name,
                new_incorrect_items=new_incorrect_items
            )

            completion = self._complete_with_backoff(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": template["system"].format(course_name=course_name),
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

            response_text = completion.choices[0].message.content

            try:
                new_analysis_data = json.loads(response_text)
            except json.JSONDecodeError as e:
                logger.error(f"Failed to parse JSON response: {e}")
                logger.error(f"Response content: {response_text}")
                raise ValueError(f"Invalid JSON response from OpenAI: {e}")

            logger.info("Course weakness analysis completed successfully")
            return new_analysis_data

        except Exception as e:
            logger.error(f"Error in analyze_new_weakness: {e}")
            raise
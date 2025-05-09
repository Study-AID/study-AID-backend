import json
import logging
import os
from datetime import datetime

import yaml
from openai import OpenAI

from summary_models import Summary, Metadata

logger = logging.getLogger(__name__)


class OpenAIClient:
    def __init__(self):
        self.api_key = os.environ.get("OPENAI_API_KEY")
        if not self.api_key:
            raise ValueError("OPENAI_API_KEY environment variable not set")

        self.client = OpenAI(api_key=self.api_key)
        self.model = os.environ.get("OPENAI_MODEL", "gpt-4o")

    def load_prompt_template(self, prompt_path):
        try:
            with open(prompt_path, "r", encoding="utf-8") as file:
                template = yaml.safe_load(file)
                return template
        except Exception as e:
            logger.error(f"Error loading prompt template: {e}")
            raise

    def generate_summary(self, lecture_content, prompt_path):
        try:
            template = self.load_prompt_template(prompt_path)
            model_name = template.get("model", self.model)

            # Format the user message with the lecture content
            user_message = template["user"].format(lecture_content=lecture_content)

            # Create the completion with JSON response format
            response = self.client.chat.completions.create(
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

            # Create a Summary object from the JSON response
            summary_obj = Summary.model_validate(summary_json)

            # Return the validated summary object
            return summary_obj
        except Exception as e:
            logger.error(f"Error generating summary: {e}")
            raise

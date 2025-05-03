import logging
import os

import yaml
from openai import OpenAI

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

            # Format the user message with the lecture content
            user_message = template["user"].format(lecture_content=lecture_content)

            # Create the completion
            # TODO(mj): structure the summary with custom format.
            response = self.client.chat.completions.create(
                model=self.model, # NOTE(mj): from each template?
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
            )

            # Get the summary from the response
            summary = response.choices[0].message.content

            # Format with assistant's template pattern if needed
            # TODO(mj): use structured output instead of string.
            if "A" in template:
                # Remove the placeholder from the A template
                formatted_summary = template["A"].replace("{summary}", summary)
                return formatted_summary

            return summary

        except Exception as e:
            logger.error(f"Error generating summary: {e}")
            raise

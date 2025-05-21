"""
PDF Chunker module for optimally splitting PDF content into manageable chunks.
"""
import logging
import math
from typing import List, Dict, Any

from parsed_text_models import ParsedText

logger = logging.getLogger(__name__)


class PDFChunker:
    """
    Class for splitting PDF content into optimal chunks for processing.
    """

    def __init__(self, default_chunk_size: int = 40):
        self.default_chunk_size = default_chunk_size

    def calculate_optimal_chunks(self, total_pages: int, chunk_size: int = None) -> List[int]:
        """
        Calculate optimal chunk sizes to avoid small final chunks.

        Args:
            total_pages: Total number of pages in the PDF
            chunk_size: Desired chunk size (uses default if None)

        Returns:
            List of page counts for each chunk
        """
        if chunk_size is None:
            chunk_size = self.default_chunk_size

        # If total pages is less than chunk size, just use one chunk
        if total_pages <= chunk_size:
            return [total_pages]

        # Calculate number of chunks needed
        num_chunks = math.ceil(total_pages / chunk_size)

        # If using the default size would result in a tiny last chunk,
        # distribute pages more evenly
        if total_pages % chunk_size < chunk_size * 0.25 and num_chunks > 1:
            # More optimal distribution - divide pages evenly
            new_chunk_size = math.ceil(total_pages / (num_chunks - 1))

            # Check if we can handle all pages with one fewer chunk
            if (num_chunks - 1) * new_chunk_size >= total_pages:
                num_chunks -= 1
                return self._distribute_pages(total_pages, num_chunks)

        # Otherwise use the standard distribution
        return self._distribute_pages(total_pages, num_chunks)

    def _distribute_pages(self, total_pages: int, num_chunks: int) -> List[int]:
        """
        Distribute pages evenly across the given number of chunks.

        Args:
            total_pages: Total number of pages
            num_chunks: Number of chunks to create

        Returns:
            List of page counts for each chunk
        """
        base_pages_per_chunk = total_pages // num_chunks
        remainder = total_pages % num_chunks

        # Create a list of chunk sizes
        chunk_sizes = []
        for i in range(num_chunks):
            # Add one extra page to the first 'remainder' chunks
            if i < remainder:
                chunk_sizes.append(base_pages_per_chunk + 1)
            else:
                chunk_sizes.append(base_pages_per_chunk)

        return chunk_sizes

    def split_parsed_text(self, parsed_text: ParsedText, chunk_size: int = None) -> List[Dict[str, Any]]:
        """
        Split the parsed text into chunks based on calculated optimal chunk sizes.

        Args:
            parsed_text: The ParsedText object containing all pages
            chunk_size: Desired chunk size (uses default if None)

        Returns:
            List of dictionaries containing formatted content for each chunk
        """
        if chunk_size is None:
            chunk_size = self.default_chunk_size

        # Calculate optimal chunk sizes
        chunk_sizes = self.calculate_optimal_chunks(parsed_text.total_pages, chunk_size)
        logger.info(f"Split PDF into {len(chunk_sizes)} chunks of sizes: {chunk_sizes}")

        # Create the chunks
        chunks = []
        start_idx = 0

        for i, size in enumerate(chunk_sizes):
            end_idx = start_idx + size

            # Extract pages for this chunk
            chunk_pages = parsed_text.pages[start_idx:end_idx]

            # Format for openai input
            formatted_content = []
            for page in chunk_pages:
                formatted_content.append({
                    "page": page.page_number,  # Keep original page numbers
                    "content": page.text
                })

            # Create chunk metadata
            chunk = {
                "chunk_id": i,
                "total_chunks": len(chunk_sizes),
                "start_page": chunk_pages[0].page_number,
                "end_page": chunk_pages[-1].page_number,
                "formatted_content": formatted_content
            }

            chunks.append(chunk)
            start_idx = end_idx

        return chunks

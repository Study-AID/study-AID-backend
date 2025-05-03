package com.example.api.external;

import com.example.api.external.dto.rag.RagAnswer;

public interface RagClient {
    RagAnswer query(String question, String parsedText);
}

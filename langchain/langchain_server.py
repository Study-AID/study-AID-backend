from flask import Flask, request, jsonify
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.schema import Document
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.memory import ConversationBufferWindowMemory

import os
import re

app = Flask(__name__)

in_memory_chat_message_history = {}
in_memory_vector_reference_context = {}

VECTOR_DIR = "/app/chroma_db"

embeddings = HuggingFaceEmbeddings(
    model_name="intfloat/e5-small-v2",
    encode_kwargs={"normalize_embeddings": True}
)

# 하이퍼 파라미터 chunk_size, chunk_overlap. 단위는 공백 포함 글자 수.
text_splitter = RecursiveCharacterTextSplitter(chunk_size=700, chunk_overlap=100)

def parse_pages(parsed_text):
    page_blocks = re.split(r"\[p\.(\d+)]", parsed_text)
    documents = []
    for i in range(1, len(page_blocks), 2):
        page_num = int(page_blocks[i])
        page_content = page_blocks[i + 1]
        chunks = text_splitter.split_text(page_content)
        for chunk in chunks:
            documents.append(Document(page_content=chunk, metadata={"page": page_num}))
    return documents

def format_buffer_window_messages_list(memory):
    msgs = memory.load_memory_variables({})["history"]  
    return [  
        {"role": "user" if m.type == "human" else "assistant", "message": m.content}
        for m in msgs
    ]  

@app.route("/vectors", methods=["POST"])
def vectorize_lectures():
    data = request.json
    lecture_id = data.get("lecture_id")
    parsed_text = data.get("parsed_text")

    if not lecture_id or not parsed_text:
        return jsonify({"error": "lecture_id and parsed_text are required"}), 400

    documents = parse_pages(parsed_text)
    volume_path = os.path.join(VECTOR_DIR, lecture_id)

    vectors = Chroma.from_documents(
        documents,
        embedding=embeddings,
        persist_directory=volume_path
    )
    in_memory_vector_reference_context[lecture_id] = vectors

    sample_vectors = []
    if documents:
        raw_vector = embeddings.embed_documents([documents[0].page_content])[0]  #변경
        sample_vectors = raw_vector[:3]

    return jsonify({
        "message": "Lecture vectorized successfully",
        "lecture_id": lecture_id,
        "total_chunks": len(documents),
        "sample_vectors": sample_vectors
    }), 200

@app.route("/references", methods=["POST"])
def find_references():
    data = request.json
    lecture_id = data.get("lectureId")
    question = data.get("question")

    # 하이퍼 파라미터 k. gpt에 넘겨줄 chunk 수. chunk 수 많을 수록 gpt 비용 증가. 3은 기본값이며 k는 서비스에서 넘겨줍니다.
    top_k = int(data.get("k", 3))

    if not lecture_id or not question:
        return jsonify({"error": "lectureId and question are required"}), 400

    if lecture_id not in in_memory_vector_reference_context:
        path = os.path.join(VECTOR_DIR, lecture_id)
        if not os.path.exists(path):
            return jsonify({"error": "Lecture index not found"}), 404
        in_memory_vector_reference_context[lecture_id] = Chroma(
            embedding_function=embeddings,
            persist_directory=path
        )

    vectorstore = in_memory_vector_reference_context[lecture_id]
    # 중복 출처는 없애기 위해 k*5개를 가져와서 중복 제거 후 k개만 리턴합니다.
    docs_and_scores = vectorstore.similarity_search_with_score(question, k=top_k*5)
    docs_and_scores.sort(key=lambda x: x[1], reverse=True)

    seen = set()
    results = []
    for doc, score in docs_and_scores:
        text = doc.page_content.strip()
        page = doc.metadata.get("page", -1)
        key = (text, page)
        if key not in seen:
            seen.add(key)
            results.append({"text": text, "page": page})
        if len(results) >= top_k:
            break

    return jsonify({
        "message": "References found successfully",
        "references": results
    }), 200

@app.route("/messages", methods=["POST"])
def append_message():
    data = request.json
    chat_id = data.get("chat_id")
    question = data.get("question")
    answer = data.get("answer")

    if not chat_id or question is None or answer is None:
        return jsonify({"error": "chat_id, question, and answer are required"}), 400

    memory = in_memory_chat_message_history.get(chat_id)
    if memory is None:
        # 하이퍼 파라미터 k. 대화 맥락 유지 위해 저장할 최근 qna item (질문-답변 쌍) 수. 이외 여러 전략(토큰 수로 끊기, 누적 대화 summary 등) 변경 가능합니다.
        memory = ConversationBufferWindowMemory(k=3, return_messages=True)
        in_memory_chat_message_history[chat_id] = memory  

    memory.chat_memory.add_user_message(question)
    memory.chat_memory.add_ai_message(answer)

    history = format_buffer_window_messages_list(memory)
    return jsonify({"chat_id": chat_id, "buffer_window_history": history}), 200

@app.route("/messages-history", methods=["GET"])
def get_message_history():
    chat_id = request.args.get("chat_id")

    if not chat_id:
        return jsonify({"error": "chat_id is required"}), 400

    memory = in_memory_chat_message_history.get(chat_id)
    if memory is None:
        return jsonify({"error": "Chat not found"}), 404

    history = format_buffer_window_messages_list(memory)
    return jsonify({"chat_id": chat_id, "buffer_window_history": history}), 200

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok"}), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

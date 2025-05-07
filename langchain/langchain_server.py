from flask import Flask, request, jsonify
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.schema import Document
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.memory import ConversationBufferWindowMemory

import os
import re

app = Flask(__name__)

in_memory_chat_message_context = {}
in_memory_vector_reference_context = {}

VECTOR_DIR = "/app/chroma_db"

# TODO (jin): optimize (hyperparameter, model, translation, memory) this logic for better UX, refactor names, add test code, swagger
embeddings = HuggingFaceEmbeddings(
    model_name="BAAI/bge-m3",
    encode_kwargs={"normalize_embeddings": True}
)

# 하이퍼 파라미터 chunk_size, chunk_overlap. 공백 포함 글자 수. 현재는 출처 페이지 제공을 위해 페이지별로 chunking합니다.
'''
 (영어로 한 슬라이드에 텍스트 꽉 차는 "확률과통계" 강의자료 1300자, 반 이상 차는 "시스템프로그래밍" 강의자료 500~750자)
 (한국어로 텍스트 1/3 차는 "발달심리학" 강의자료 160자) 
'''
# text_splitter = RecursiveCharacterTextSplitter(chunk_size=300, chunk_overlap=50)
text_splitter = RecursiveCharacterTextSplitter(chunk_size=700, chunk_overlap=100)

def parse_pages(parsed_text):
    page_blocks = re.split(r"\[p\.(\d+)]", parsed_text)
    documents = []
    for i in range(1, len(page_blocks), 2):
        page_num = int(page_blocks[i])
        page_content = re.sub(r"\s+", " ", page_blocks[i + 1]).strip()
        chunks = text_splitter.split_text(page_content)
        if not chunks:
            print(f"[Langchain] 페이지 {page_num}에서 chunk가 생성되지 않았습니다.")
        for chunk in chunks:
            documents.append(Document(page_content=chunk, metadata={"page": page_num}))
    return documents

def format_buffer_window_messages_list(memory):
    msgs = memory.load_memory_variables({})["history"] # ConversationBufferWindowMemory에서 사용하는 key 이름
    return [  
        {"role": "user" if m.type == "human" else "assistant", "content": m.content}
        for m in msgs
    ]  

@app.route("/vectors", methods=["POST"])
def vectorize_lectures():
    data = request.json
    lecture_id = data.get("lecture_id")
    parsed_text = data.get("parsed_text")

    print("\n===== [DEBUG] parsed_text from Spring =====", flush=True)
    print("repr:", repr(parsed_text)[:100], flush=True)
    print("raw:\n", parsed_text[:100] if parsed_text else "None", flush=True)
    print("==========================================\n", flush=True)

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
    lecture_id = data.get("lecture_id")
    question = data.get("question")
    # 하이퍼 파라미터 k. gpt에 넘겨줄 chunk 수. chunk 수 많을 수록 gpt 비용 증가. 3은 기본값이며 k는 서비스에서 넘겨줍니다.
    top_k = int(data.get("k", 3))
    min_similarity = float(data.get("min_similarity", 0.3))

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
    # 유사도로 변환 후 높은 순으로 정렬
    similarity_results = [(doc, 1.0 - score) for doc, score in docs_and_scores]
    similarity_results.sort(key=lambda x: x[1], reverse=True)

    seen = set()
    results = []
    for doc, similarity in similarity_results:
        if similarity >= min_similarity:
            text = doc.page_content.strip()
            page = doc.metadata.get("page", -1)
            key = (text, page)
            if key not in seen:
                seen.add(key)
                results.append({
                    "text": text,
                    "page": page,
                    "similarity": round(similarity, 4)
                })
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
    messages = data.get("messages", [])

    if not chat_id or not messages:
        return jsonify({"error": "chat_id and messages are required"}), 400

    memory = in_memory_chat_message_context.get(chat_id)
    if memory is None:
        # 하이퍼 파라미터 k. 대화 맥락 유지 위해 저장할 최근 qna item (질문-답변 쌍) 수. 이외 여러 전략(토큰 수로 끊기, 누적 대화 summary 등) 변경 가능합니다.
        memory = ConversationBufferWindowMemory(k=3, return_messages=True)
        in_memory_chat_message_context[chat_id] = memory

    # ConversationBufferWindowMemory가 요구하는 형식에 맞게 쌍으로 추가
    for msg in messages:
        role = msg.get("role")
        content = msg.get("content")

        if role == "user":
            memory.chat_memory.add_user_message(content)
        elif role == "assistant":
            memory.chat_memory.add_ai_message(content)

    context = format_buffer_window_messages_list(memory)
    return jsonify({"chat_id": chat_id, "buffer_window_context": context}), 200

@app.route("/messages-context", methods=["GET"])
def get_messages_context():
    chat_id = request.args.get("chat_id")

    if not chat_id:
        return jsonify({"error": "chat_id is required"}), 400

    memory = in_memory_chat_message_context.get(chat_id)
    if memory is None:
        print(f"[LangChain] chat_id={chat_id} 없음 → 새 대화 시작으로 간주합니다.", flush=True)
        return jsonify({"chat_id": chat_id, "buffer_window_context": []}), 200

    context = format_buffer_window_messages_list(memory)
    return jsonify({"chat_id": chat_id, "buffer_window_context": context}), 200

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok"}), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

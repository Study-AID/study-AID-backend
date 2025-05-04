from flask import Flask, request, jsonify
from langchain.embeddings import HuggingFaceEmbeddings
from langchain.memory import ConversationBufferWindowMemory
from langchain.vectorstores import Chroma
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.schema import Document
import os
import re

app = Flask(__name__)

in_memory_chat_message_history = {}
in_memory_vector_reference_context = {}
VECTOR_DIR = "/app/chroma_db"
embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")

'''
(영어로 한 슬라이드에 텍스트 꽉 차는 "확률과통계" 강의자료 1300자, 반 이상 차는 "시스템프로그래밍" 강의자료 500~750자)
(한국어로 텍스트 1/3 차는 "발달심리학" 강의자료 160자) 
'''
text_splitter = RecursiveCharacterTextSplitter(chunk_size=700, chunk_overlap=100) # 하이퍼 파라미터 chunk_size, chunk_overlap. 단위는 공백 포함 글자 수.

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

    return jsonify({"message": "Lecture vectorized successfully", "lecture_id": lecture_id}), 200


@app.route("/references", methods=["POST"])
def find_references():
    data = request.json
    lecture_id = data.get("lectureId")
    question = data.get("question")

    top_k = int(data.get("k", 3)) # 하이퍼 파라미터 k. gpt에 넘겨줄 chunk 수. chunk 수 많을 수록 gpt 비용 증가. 3은 기본값이며 k는 서비스에서 넘겨줌.

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
    docs = vectorstore.similarity_search(question, k=top_k)

    results = [
        {
            "text": doc.page_content,
            "page": doc.metadata.get("page", -1)
        }
        for doc in docs
    ]

    return jsonify({"references": results}), 200

@app.route("/history", methods=["POST"])
def generate_message_history():
    data = request.json
    lecture_id = data.get("lecture_id")
    chat_id = data.get("chat_id")
    question = data.get("question")
    history = data.get("chat_history", [])

    if not all([lecture_id, chat_id, question]):
        return jsonify({"error": "lecture_id, chat_id, and question are required"}), 400

    if chat_id not in in_memory_chat_message_history:
        in_memory_chat_message_history[chat_id] = ConversationBufferWindowMemory(
            memory_key="chat_history",
            return_messages=True,
            k=3 # 하이퍼 파라미터 k. 대화 맥락 유지 위해 저장할 qna item 수. 이외 여러 전략(토큰 수로 끊기, 누적 대화 summary 등) 변경 가능
        )
    memory = in_memory_chat_message_history[chat_id]

    for h in history:
        q, a = h.get("question"), h.get("answer")
        if q and a:
            memory.chat_memory.add_user_message(q)
            memory.chat_memory.add_ai_message(a)

    memory.chat_memory.add_user_message(question)

    context_messages = memory.chat_memory.messages
    messages_list = [
        {"role": "user" if m.type == "human" else "ai", "content": m.content}
        for m in context_messages
    ]

    return jsonify({"message_history": messages_list}), 200

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

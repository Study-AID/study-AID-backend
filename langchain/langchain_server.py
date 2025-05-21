from flask import Flask, request, jsonify
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.schema import Document
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.memory import ConversationBufferWindowMemory
import os
import re
import time
import json
import shutil

# TODO(jin): optimize (hyperparameter, model, memory, vectorDB) for better UX
# 하이퍼 파라미터 chunk_size, chunk_overlap. 공백 포함 글자 수. 현재는 출처 페이지 제공을 위해 페이지별로 chunking합니다.
"""
글자수 참고: 
-영어: 한 슬라이드에 글자 꽉 차는 강의자료 1300자, 2/3 차는 "시스템프로그래밍" 강의자료 500~750자)
-한국어: 한 슬라이드 1/3 차는 "발달심리학" 강의자료 160자
"""
# 임베딩 모델 변경 가능
# 하이퍼 파라미터 chroma db 인덱싱 및 유사도 검색 성능을 결정하는 hnsw 파라미터들. https://docs.trychroma.com/docs/collections/configure
# 하이퍼 파라미터 top_k. gpt에 넘겨줄 chunk 수. chunk 수 많을 수록 gpt 비용 증가. 3은 기본값이며 k는 서비스에서 넘겨줍니다.
# 하이퍼 파라미터 min_similarity. 유사도 기준. 0.3은 기본값이며 서비스에서 넘겨줍니다.

app = Flask(__name__)

in_memory_lecture_embeddings_cache = {}
VECTOR_DIR = "/app/chroma_db"
embeddings = HuggingFaceEmbeddings(
    model_name="BAAI/bge-m3",
    encode_kwargs={"normalize_embeddings": True}
)
text_splitter = RecursiveCharacterTextSplitter(chunk_size=700, chunk_overlap=100)

# 임베딩 모델 사전 로드 함수
def preload_embedding_model():
    print("[Langchain] 임베딩 모델 사전 로드 시작...", flush=True)
    start_time = time.time()

    try:
        sample_text = "This is a sample text to warm up the embedding model."
        _ = embeddings.embed_query(sample_text)
        print(f"[Langchain] 임베딩 모델 로드 완료: {time.time() - start_time:.2f}초", flush=True)
    except Exception as e:
        print(f"[Langchain] 임베딩 모델 로드 실패: {str(e)}", flush=True)

# 중앙화된 벡터 스토어 로드 함수(벡터 스토어를 메모리 또는 디스크에서 찾아 반환)
def get_vector_store(lecture_id, check_only=False):
    vector_store = in_memory_lecture_embeddings_cache.get(lecture_id)
    if vector_store is not None:
        print(f"[Langchain] 벡터 스토어 메모리 캐시 적중: lecture_id={lecture_id}", flush=True)
        return vector_store

    if check_only:
        path = os.path.join(VECTOR_DIR, lecture_id)
        return os.path.exists(path)

    path = os.path.join(VECTOR_DIR, lecture_id)

    if os.path.exists(path):
        try:
            print(f"[Langchain] 벡터 스토어 디스크에서 로드: lecture_id={lecture_id}", flush=True)
            vector_store = Chroma(embedding_function=embeddings, persist_directory=path)
            in_memory_lecture_embeddings_cache[lecture_id] = vector_store
            return vector_store
        except Exception as e:
            print(f"[Langchain] 벡터 스토어 로드 실패: {e}", flush=True)
            return None

    return None

# TODO(jin): optimize vector store preloading strategy for scalability. (popularity-based, LRU, etc.)
# 벡터 스토어 사전 로드 함수
def preload_vector_store():
    print("[Langchain] 벡터 스토어 사전 로드 시작...", flush=True)
    start_time = time.time()

    try:
        vector_dirs = [d for d in os.listdir(VECTOR_DIR) if os.path.isdir(os.path.join(VECTOR_DIR, d))]
        print(f"[Langchain] 발견된 벡터 스토어 디렉토리 수: {len(vector_dirs)}", flush=True)

        loaded_count = 0
        for lecture_id in vector_dirs:
            try:
                load_start = time.time()
                print(f"[Langchain] 강의 {lecture_id} 벡터 스토어 로드 중...", flush=True)
                vector_store = get_vector_store(lecture_id)

                if vector_store is not None:
                    loaded_count += 1
                    load_time = time.time() - load_start
                    print(f"[Langchain] 강의 {lecture_id} 벡터 스토어 로드 완료: {load_time:.2f}초", flush=True)
                else:
                    print(f"[Langchain] 강의 {lecture_id} 벡터 스토어 손상됨, 로드 실패", flush=True)
            except Exception as e:
                print(f"[Langchain] 강의 {lecture_id} 벡터 스토어 로드 실패: {str(e)}", flush=True)

        total_time = time.time() - start_time
        print(f"[Langchain] 벡터 스토어 사전 로드 완료: 총 {loaded_count}/{len(vector_dirs)}개, {total_time:.2f}초 소요", flush=True)
    except Exception as e:
        print(f"[Langchain] 벡터 스토어 사전 로드 중 오류 발생: {str(e)}", flush=True)

# 서버 시작 시 동기적으로 사전 로드 함수 실행
def initialize_server():
    print(f"[Langchain] 서버 초기화 시작 (PID: {os.getpid()})", flush=True)
    start_time = time.time()
    preload_embedding_model()
    preload_vector_store()
    total_time = time.time() - start_time
    print(f"[Langchain] 서버 초기화 완료: {total_time:.2f}초 소요 (PID: {os.getpid()})", flush=True)

# 동기적으로 초기화 실행
initialize_server()

# parsed_text JSON 전처리 및 Chroma DB 저장용 documents로 chunking (+중복 제거) 하는 함수
def split_and_extract_chunk_documents(parsed_text):
    documents = []
    seen_chunks = set()
    for page in parsed_text.get("pages", []):
        page_number = page.get("page_number")

        raw_page_text = page.get("text", "")

        #\n숫자 로 페이지 번호 나오는 경우 숫자 제거
        cleaned_page_text = re.sub(r"(\n|\s)\d{1,3}$", "", raw_page_text).strip()
        page_content = re.sub(r"\s+", " ", cleaned_page_text).strip()

        chunks = text_splitter.split_text(page_content)
        for chunk in chunks:
            stripped_chunk = chunk.strip()
            # 중복된 chunk는 저장하지 않음
            if stripped_chunk in seen_chunks:
                continue
            seen_chunks.add(stripped_chunk)

            documents.append(Document(
                page_content=stripped_chunk,
                metadata={
                    "page": page_number
                }
            ))
    return documents

# 강의자료의 parsed_text를 받아 벡터 임베딩하여 chroma db에 저장하는 API
@app.route("/lectures/<lecture_id>/embeddings", methods=["POST"])
def generate_lecture_embeddings(lecture_id):
    parsed_text = request.json.get("parsed_text")

    print("\n===== [DEBUG] parsed_text check =====", flush=True)
    print("Spring에서 전달받은 결과 (JSON 원형):\n", json.dumps(parsed_text, ensure_ascii=False)[:100] if parsed_text else "None", flush=True)
    print("Spring → Python 변환 결과 (Python dict):", repr(parsed_text)[:100], flush=True)
    print("==========================================\n", flush=True)

    if not parsed_text:
        return jsonify({"error": "parsed_text is required"}), 400

    # 강의자료 벡터 스토어 메모리 로드 확인
    if lecture_id in in_memory_lecture_embeddings_cache:
        print(f"[Langchain] 벡터 스토어 이미 메모리에 로드됨: {lecture_id}", flush=True)
        return jsonify({"message": "Vector store already loaded in memory"}), 200

    # 기존 벡터 스토어 존재 여부 체크(생성 필요성 판단을 위해), 손상된 경우 삭제 후 재생성
    volume_path = os.path.join(VECTOR_DIR, lecture_id)
    if os.path.exists(volume_path):
        try:
            vector_store = Chroma(embedding_function=embeddings, persist_directory=volume_path)
            in_memory_lecture_embeddings_cache[lecture_id] = vector_store
            print(f"[Langchain] 디스크 인덱스 유효: {lecture_id} → 캐시에 로드", flush=True)
            return jsonify({"message": "Vector store loaded from disk"}), 200
        except Exception as e:
            print(f"[Langchain] 기존 인덱스 손상 → 삭제 후 재생성: {e}", flush=True)
            shutil.rmtree(volume_path)

    # 강의자료 청크 생성
    try:
        documents = split_and_extract_chunk_documents(parsed_text)
        print(f"[Langchain] 강의자료 청크 생성 완료: {len(documents)}개", flush=True)

        # 벡터 스토어 생성
        print(f"[Langchain] 벡터 스토어 생성 시작: {lecture_id}", flush=True)

        vector_store = Chroma.from_documents(
            documents,
            embedding=embeddings,
            persist_directory=volume_path,
            collection_metadata={
                "hnsw:space": "cosine",  # 코사인 유사도 사용 (기본값: l2 (유클리드 거리))
                "hnsw:num_threads": 2    # 멀티스레드 사용 (기본값: 1)
              # "hnsw:M": 16, # 그래프 연결 수 (기본값: 16)
              # "hnsw:efSearch": 64 # 검색 정확도 (기본값: 100)
            }
        )

        in_memory_lecture_embeddings_cache[lecture_id] = vector_store
        print(f"[Langchain] 벡터 스토어 생성 및 메모리에 캐싱 완료: {lecture_id}", flush=True)

        sample_vectors = []
        if documents:
            vector = embeddings.embed_documents([documents[0].page_content])[0]
            sample_vectors = vector[:3]

        return jsonify({
            "message": "Lecture embeddings generated successfully",
            "total_chunks": len(documents),
            "sample_vectors": sample_vectors
        }), 200

    except Exception as e:
        print(f"[Langchain] 벡터 스토어 생성 실패: {e}", flush=True)
        error_details = str(e)
        if "StopIteration" in error_details:
            error_details += " (ChromaDB 내부 구성요소 불일치로 인한 오류일 수 있습니다)"
        return jsonify({
            "error": f"벡터 스토어 생성 실패: {error_details}"
        }), 500

# 강의자료 벡터 임베딩 생성 체크 API (Spring과의 is_vectorized 상태 일치 및 벡터 스토어 유효성 검사용)
@app.route("/lectures/<lecture_id>/embeddings/check", methods=["GET"])
def check_embedding_result_status(lecture_id):
    path = os.path.join(VECTOR_DIR, lecture_id)
    result = {
        "directory_exists": False, # 강의자료 벡터 스토어 디렉토리 존재 여부
        "loadable": False,         # 손상되지 않았는지, chroma_db에 정상 로드 가능한지
        "vector_count": 0          # 벡터 스토어에 저장된 벡터 개수
    }

    if not os.path.exists(path):
        return jsonify(result), 404
    result["directory_exists"] = True

    try:
        vector_store = Chroma(embedding_function=embeddings, persist_directory=path)
        result["loadable"] = True
        result["vector_count"] = vector_store._collection.count()
        return jsonify(result), 200
    except Exception as e:
        print(f"[Langchain] 벡터 스토어 로드 실패 (체크용): {e}", flush=True)
        return jsonify(result), 500

# TODO(jin): consider returning a safe fallback (e.g., empty references) after stabilizing performance for better UX
# 질문을 벡터 임베딩하여 강의자료의 임베딩 결과와 비교하여 유사도 검색으로 출처 찾는 API
@app.route("/lectures/<lecture_id>/references", methods=["POST"])
def find_references_in_lecture(lecture_id):
    data = request.json
    question = data.get("question")

    top_k = int(data.get("max_num_references", 3))
    min_similarity = float(data.get("min_similarity", 0.3))

    if not question:
        return jsonify({"error": "question is required"}), 400

    timings = {}

    # 1. 강의자료 벡터 스토어 로드
    vector_store = get_vector_store(lecture_id)
    if vector_store is None:
        return jsonify({"error": "Lecture vector store not found"}), 404

    # 2. 벡터 스토어에서 유사도 검색
    search_start = time.time()

    try:
        docs_and_scores = vector_store.similarity_search_with_score(question, k=top_k)
    except Exception as e:
        print(f"[Langchain] 출처 검색 실패: {str(e)}", flush=True)
        return jsonify({"error": f"Search failed: {str(e)}"}), 500

    timings['search_time'] = time.time() - search_start
    print(f"[Langchain] 출처 검색 시간: {timings['search_time']:.4f}초", flush=True)
    print(f"[Langchain] 출처 검색 결과 수: {len(docs_and_scores)}", flush=True)

    # 3. score -> similarity 변환(최소 similarity 필터링 위해) 및 유사도 내림차순 자동 정렬
    process_start = time.time()

    results = []
    for doc, score in docs_and_scores:
        similarity = 1.0 - score
        if similarity >= min_similarity:
            results.append({
                "text": doc.page_content,
                "page": doc.metadata.get("page", -1),
                "similarity": round(similarity, 4)
            })
            if len(results) >= top_k:
                break

    timings['result_processing'] = time.time() - process_start
    print(f"[Langchain] 결과 처리 시간: {timings['result_processing']:.4f}초", flush=True)
    print(f"[Langchain] 최종 결과 수: {len(results)}", flush=True)

    return jsonify({
        "message": "References found successfully",
        "references": results
    }), 200

in_memory_chat_message_context = {}

def format_buffer_window_messages_list(memory):
    msgs = memory.load_memory_variables({})["history"] # ConversationBufferWindowMemory에서 사용하는 key 이름
    return [
        {"role": "user" if m.type == "human" else "assistant", "content": m.content}
        for m in msgs
    ]

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
    loaded_vector_stores = len(in_memory_lecture_embeddings_cache)

    return jsonify({
        "status": "ok",
        "loaded_vector_stores": loaded_vector_stores,
        "embedding_model_loaded": True
    }), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

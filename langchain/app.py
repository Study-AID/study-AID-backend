from flask import Flask, request, jsonify
from langchain.vectorstores import Chroma
from langchain.embeddings import HuggingFaceEmbeddings
from langchain.text_splitter import RecursiveCharacterTextSplitter
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# 임베딩 모델 초기화 (무료 HuggingFace 모델 사용)
embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

# 벡터 저장소 초기화
vectorstore = {}  # 강의자료 ID를 키로 사용

@app.route('/health', methods=['GET'])
def health_check():
    """서버 상태 확인"""
    return jsonify({"status": "healthy"})

@app.route('/index', methods=['POST'])
def index_document():
    """강의자료 텍스트를 벡터 저장소에 인덱싱"""
    data = request.json
    lecture_id = data.get('lectureId')
    text_content = data.get('textContent')

    if not lecture_id or not text_content:
        return jsonify({"status": "error", "message": "Missing lectureId or textContent"}), 400

    try:
        logger.info(f"Indexing lecture {lecture_id}")

        # 텍스트 분할
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=1000,
            chunk_overlap=200
        )
        chunks = text_splitter.split_text(text_content)

        logger.info(f"Split into {len(chunks)} chunks")

        # 벡터 저장소 경로 설정 및 생성
        persist_directory = f"./chroma_db/{lecture_id}"
        os.makedirs(os.path.dirname(persist_directory), exist_ok=True)

        # Chroma DB에 저장
        vectorstore[lecture_id] = Chroma.from_texts(
            texts=chunks,
            embedding=embeddings,
            persist_directory=persist_directory
        )

        logger.info(f"Successfully indexed lecture {lecture_id}")

        return jsonify({
            "status": "success",
            "message": f"Indexed document for lecture {lecture_id}",
            "chunks": len(chunks)
        })

    except Exception as e:
        logger.error(f"Error indexing document: {str(e)}")
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/retrieve', methods=['POST'])
def retrieve_context():
    """질문에 관련된 문맥 검색"""
    data = request.json
    lecture_id = data.get('lectureId')
    question = data.get('question')
    k = data.get('k', 3)

    if not lecture_id or not question:
        return jsonify({"status": "error", "message": "Missing lectureId or question"}), 400

    try:
        logger.info(f"Retrieving context for lecture {lecture_id}")

        # 벡터 저장소가 메모리에 없으면 디스크에서 로드
        if lecture_id not in vectorstore:
            persist_directory = f"./chroma_db/{lecture_id}"
            if os.path.exists(persist_directory):
                vectorstore[lecture_id] = Chroma(
                    persist_directory=persist_directory,
                    embedding_function=embeddings
                )
                logger.info(f"Loaded vectorstore from disk for lecture {lecture_id}")
            else:
                return jsonify({
                    "status": "error",
                    "message": "Lecture material not indexed yet"
                }), 400

        # 관련 문맥 검색
        docs = vectorstore[lecture_id].similarity_search(question, k=k)
        contexts = [doc.page_content for doc in docs]

        logger.info(f"Retrieved {len(contexts)} context chunks")

        return jsonify({
            "status": "success",
            "contexts": contexts
        })

    except Exception as e:
        logger.error(f"Error retrieving context: {str(e)}")
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
FROM python:3.11-slim

# 필수 빌드 도구 및 컴파일러 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    g++ \
    gcc \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 캐시/로그 줄이기
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV TRANSFORMERS_CACHE=/tmp
ENV HF_HOME=/tmp/huggingface
ENV HF_HUB_DISABLE_SYMLINKS_WARNING=1

WORKDIR /app

# torch 먼저 설치 (CUDA 없이)
RUN pip install --no-cache-dir --upgrade pip \
 && pip install --no-cache-dir torch==2.1.0+cpu -f https://download.pytorch.org/whl/torch_stable.html

# 나머지 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 소스 코드 복사
COPY . .

EXPOSE 5000

CMD ["python", "langchain_server.py"]

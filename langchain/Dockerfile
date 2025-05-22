FROM python:3.11-slim

# 필수 빌드 도구 및 컴파일러 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    g++ \
    gcc \
    # CPU 수치 연산 최적화 필수 패키지
    libopenblas-dev \
    liblapack-dev \
    libgomp1 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 캐시/로그 줄이기
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV TRANSFORMERS_CACHE=/tmp
ENV HF_HOME=/tmp/huggingface
ENV HF_HUB_DISABLE_SYMLINKS_WARNING=1

# CPU 최적화 환경 변수 추가
ENV OMP_NUM_THREADS=4 \
    OPENBLAS_NUM_THREADS=4 \
    MKL_NUM_THREADS=4 \
    VECLIB_MAXIMUM_THREADS=4 \
    NUMEXPR_NUM_THREADS=4

WORKDIR /app

# torch 먼저 설치 (CUDA 없이)
RUN pip install --no-cache-dir --upgrade pip \
 && pip install --no-cache-dir torch==2.1.0+cpu -f https://download.pytorch.org/whl/torch_stable.html

# 나머지 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt gunicorn

# 소스 코드 복사
COPY . .

EXPOSE 5000

CMD ["gunicorn", "--workers=1", "--threads=1", "--worker-class=gthread", "--bind=0.0.0.0:5000",  "langchain_server:app", "--timeout=240"]

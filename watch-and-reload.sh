#!/bin/bash

echo "Starting watch mode for Java files..."

# 초기 빌드
gradle bootJar -x test

# JAR 파일 경로 확인
JAR_PATH=$(find build/libs -name "*.jar" | grep -v plain | head -1)
echo "JAR 파일: $JAR_PATH"

# JAR 파일이 존재하는지 확인
if [ ! -f "$JAR_PATH" ]; then
  echo "ERROR: 실행 가능한 JAR를 찾을 수 없습니다. 정확한 빌드를 확인하세요."
  ls -la build/libs/
  exit 1
fi

# 최초 실행
echo "애플리케이션 시작 중..."
java -jar -Dspring.profiles.active=local "$JAR_PATH" &
APP_PID=$!

# 종료 시 프로세스 정리
trap "kill $APP_PID; exit" SIGINT SIGTERM

# 변경 감지 및 재시작 루프
while true; do
  # Java 파일 변경 감시
  CHANGED=$(find src -name "*.java" -type f -newer "$JAR_PATH" 2>/dev/null)

  if [ -n "$CHANGED" ]; then
    echo "변경 감지됨: $CHANGED"
    echo "애플리케이션 재빌드 중..."

    # 이전 프로세스 종료
    kill $APP_PID
    wait $APP_PID 2>/dev/null

    # 새로 빌드
    gradle bootJar -x test

    # JAR 파일 경로 업데이트
    JAR_PATH=$(find build/libs -name "*.jar" | grep -v plain | head -1)

    # JAR 파일 확인
    if [ ! -f "$JAR_PATH" ]; then
      echo "ERROR: 실행 가능한 JAR를 찾을 수 없습니다!"
      ls -la build/libs/
      exit 1
    fi

    # 재시작
    echo "애플리케이션 재시작 중... (JAR: $JAR_PATH)"
    java -jar -Dspring.profiles.active=local "$JAR_PATH" &
    APP_PID=$!
  fi

  sleep 3
done

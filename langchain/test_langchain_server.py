import requests
import uuid
import json

BASE_URL = "http://langchain-server:5000"

sample_text = """
[p.1] 재귀 함수는 자기 자신을 호출하는 함수이다. 이 함수는 기저 사례를 갖고 있어야 한다.
[p.2] 반복문과 재귀 함수는 모두 동일한 결과를 낼 수 있지만, 재귀는 더 많은 메모리를 사용할 수 있다.
[p.3] 기저 사례는 재귀 함수가 종료되는 조건이다. 기저 사례가 없으면 무한 재귀에 빠질 수 있다.
[p.4] 반복문은 상태를 유지하기 위해 변수를 사용하지만, 재귀는 함수 호출 스택을 사용한다.
[p.5] 재귀 함수는 코드가 간결해질 수 있지만, 성능이 떨어질 수 있다.
[p.6] 재귀 함수는 주로 트리 구조를 탐색하는 데 유용하다.
[p.7] 반복문은 주로 순차적인 작업에 적합하다.
[p.8] 재귀 함수는 메모리 사용량이 많아질 수 있지만, 코드가 더 읽기 쉬울 수 있다.
[p.9] 반복문은 메모리 사용량이 적지만, 코드가 복잡해질 수 있다.
[p.10] 재귀 함수는 기저 사례를 통해 종료되며, 반복문은 조건문을 통해 종료된다.
[p.11] 재귀 함수는 스택 오버플로우를 유발할 수 있다.
[p.12] 반복문은 스택 오버플로우를 유발하지 않는다.
"""

test_lecture_id = str(uuid.uuid4())
test_chat_id = str(uuid.uuid4())

def pretty(res):
    print(json.dumps(res.json(), indent=2, ensure_ascii=False))

def test_health_check():
    response = requests.get(f"{BASE_URL}/health")
    print("✅ /health 응답:")
    pretty(response)
    assert response.status_code == 200
    assert response.json()["status"] == "ok"

def test_vectorize_lecture():
    response = requests.post(
        f"{BASE_URL}/vectors",
        json={"lecture_id": test_lecture_id, "parsed_text": sample_text}
    )
    print("✅ /vectors 응답:")
    pretty(response)
    assert response.status_code == 200
    data = response.json()
    assert data["message"] == "Lecture vectorized successfully"
    assert "total_chunks" in data
    assert "sample_vectors" in data
    assert len(data["sample_vectors"]) == 3

def test_find_references():
    response = requests.post(
        f"{BASE_URL}/references",
        json={"lectureId": test_lecture_id, "question": "재귀 함수랑 반복문의 차이를 정리해줘.", "k": 3}
    )
    print("✅ /references 응답:")
    pretty(response)
    assert response.status_code == 200
    refs = response.json()["references"]
    assert len(refs) <= 3
    for ref in refs:
        assert "text" in ref
        assert "page" in ref

def test_append_and_get_messages():
    response = requests.post(
        f"{BASE_URL}/messages",
        json={"chat_id": test_chat_id, "question": "재귀 함수란?", "answer": "재귀 함수는 자기 자신을 호출합니다."}
    )
    print("✅ /messages 응답:")
    pretty(response)
    assert response.status_code == 200
    data = response.json()
    history = data["buffer_window_history"]
    assert len(history) == 2

    response = requests.get(f"{BASE_URL}/messages-history?chat_id={test_chat_id}")
    print("✅ /messages-history 응답:")
    pretty(response)
    assert response.status_code == 200
    history = response.json()["buffer_window_history"]
    assert len(history) == 2

def test_message_buffer_window_limit():
    chat_id = str(uuid.uuid4())
    for i in range(5):
        requests.post(
            f"{BASE_URL}/messages",
            json={"chat_id": chat_id, "question": f"Q{i+1}", "answer": f"A{i+1}"}
        )

    response = requests.get(f"{BASE_URL}/messages-history?chat_id={chat_id}")
    print("✅ 버퍼 윈도우 최대치 테스트 응답:")
    pretty(response)
    history = response.json()["buffer_window_history"]
    assert len(history) == 6
    assert history[-2]["message"] == "Q5"
    assert history[-1]["message"] == "A5"

def test_error_cases():
    print("✅ 에러 테스트 시작")

    res = requests.post(f"{BASE_URL}/vectors", json={"parsed_text": sample_text})
    print("vectors 오류 테스트 - 강의 ID, 강의자료 parsed_text 누락 시:")
    pretty(res)
    assert res.status_code == 400

    res = requests.post(f"{BASE_URL}/references", json={"question": "재귀?"})
    print("references 오류 테스트 - 강의 ID, 질문 누락 시:")
    pretty(res)
    assert res.status_code == 400

    res = requests.post(
        f"{BASE_URL}/references",
        json={"lectureId": "invalid-id", "question": "재귀?"}
    )
    print("messages-history 조회 오류 테스트 - 존재하지 않는 강의 ID 입력 시")
    pretty(res)
    assert res.status_code == 404

    res = requests.get(f"{BASE_URL}/messages-history")
    print("messages-history 조회 테스트 - chat_id 누락 시")
    pretty(res)
    assert res.status_code == 400

if __name__ == "__main__":
    test_functions = [
        test_health_check,
        test_vectorize_lecture,
        test_find_references,
        test_append_and_get_messages,
        test_message_buffer_window_limit,
        test_error_cases
    ]

    for test_fn in test_functions:
        try:
            print(f"\n🚀 Running {test_fn.__name__}")
            test_fn()
            print(f"✅ {test_fn.__name__} PASSED\n")
        except Exception as e:
            print(f"❌ {test_fn.__name__} FAILED: {e}\n")

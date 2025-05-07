-- string1 유저의 강의 parsed_text 업데이트
UPDATE app.lectures
SET parsed_text = '[p.1]
재귀 함수는 자기 자신을 호출하는 함수이다. 이 함수는 기저 사례를 갖고 있어야 한다.

[p.2]
반복문과 재귀 함수는 모두 동일한 결과를 낼 수 있지만, 재귀는 더 많은 메모리를 사용할 수 있다.

[p.3]
기저 사례는 재귀 함수가 종료되는 조건이다. 기저 사례가 없으면 무한 재귀에 빠질 수 있다.

[p.4]
반복문은 상태를 유지하기 위해 변수를 사용하지만, 재귀는 함수 호출 스택을 사용한다.

[p.5]
재귀 함수는 코드가 간결해질 수 있지만, 성능이 떨어질 수 있다.

[p.6]
재귀 함수는 주로 트리 구조를 탐색하는 데 유용하다.

[p.7]
반복문은 주로 순차적인 작업에 적합하다.

[p.8]
재귀 함수는 메모리 사용량이 많아질 수 있지만, 코드가 더 읽기 쉬울 수 있다.

[p.9]
반복문은 메모리 사용량이 적지만, 코드가 복잡해질 수 있다.

[p.10]
재귀 함수는 기저 사례를 통해 종료되며, 반복문은 조건문을 통해 종료된다.

[p.11]
재귀 함수는 스택 오버플로우를 유발할 수 있다.

[p.12]
반복문은 스택 오버플로우를 유발하지 않는다.'
WHERE course_id = 'b1111111-1111-1111-1111-111111111111'
  AND user_id = (SELECT id FROM app.users WHERE name = 'string1');

-- string2 유저의 강의 parsed_text 업데이트
UPDATE app.lectures
SET parsed_text = '[p.1]
Has Winter Come to Samsung?

[p.2]
Samsung Electronics'' stock price, which stood at 83,100 won on August 1, plunged by nearly 40 percent to 49,900 won by November 14—the lowest price since May 2020. On October 8, Jeon Young-hyun, the head of Samsung Electronics'' Device Solution (DS) division apologized for an "Earning Shock" as preliminary result of the third quarter of this year fell well below market expectations. The apology included a commitment to rebuild and address the "technological competitiveness" and "organizational culture" issues.

[p.3]
Nonetheless, Samsung has maintained its position as the No. 1 player in the global Dynamic Random-Access Memory (DRAM) market for over 30 years, commanding a 41.1 percent share as of the third quarter of 20241). However, why is Samsung Electronics facing winter? In this article, The Ajou Globe (The AG) focuses on Samsung''s current technological competitiveness in the semiconductor market explaining key terms.

[p.4]
First, what is DRAM?  Key term 1: Memory semiconductor and non-memory semiconductor Semiconductors are materials that function as a conductor and insulator under specific conditions. They are categorized into memory and non-memory semiconductors. Memory semiconductors include DRAM,High-Bandwidth Memory (HBM), and many more. DRAM is a volatile memory device that loses stored data when power is cut off. It is widely used in computers for its large capacity and high speed. However, these days, in the age of Artificial Intelligence (AI), the most popular memory semiconductor is "HBM," not DRAM. Meanwhile, non-memory semiconductors include Graphics Processing Unit (GPU), Central Processing Unit (CPU), and many more. CPU interprets program instructions, controls the operation of the computer and handles arithmetic and logical operations. GPU is a computing device for graphics and is currently the most popular non-memory semiconductor. Because, unlike the CPU''s serial processing the GPU uses parallel processing, which is ideal for high-speed data processing required for AI training.

[p.5]
Why is HBM more popular than DRAM nowadays?

[p.6]
Key term 2: HBM The harmony between memory semiconductor''s information storage and non-memory semiconductor''s computational capabilities is important. Unfortunately, DRAM (memory semiconductor) cannot keep pace with GPU (non-memory semiconductor)''s data processing speed. Therefore, in 2013, SK Hynix developed the first HBM, which stores data while matching GPU''s speed by stacking multiple DRAM layers and creating passages between them to accelerate processing.'
WHERE course_id = 'b2222222-2222-2222-2222-222222222222'
  AND user_id = (SELECT id FROM app.users WHERE name = 'string2');

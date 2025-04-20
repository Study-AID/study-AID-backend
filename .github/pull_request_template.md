# Ticket

task-1-2-2

# Slack

없음

# Description

JWT 토큰 인증 기반 이메일 회원가입, 로그인, 로그아웃, 리프레시 API 개발


-AuthController 및 dto들, AuthService 및 구현체, RedisService 및 구현체
-Spring Security 관련 SecurityConfig
-application.yml 및 application-local.yml (중복 코드 제거, jwt 설정 추가)
-build.gradle, JwtProperties, JwtAuthenticationFilter, JwtUtils, JwtService 및 구현체
-.env.example 추가 및 docker-compose.yml 수정 (.env 불러올 수 있도록)
-exceptionHandler

# Checklist

머지 전 중요하게 확인할 사항은 없으나 아래 참고 부탁드립니다.
-회원가입, 로그인 API 호출 시 회원정보도 같이 열람 가능하게 구현했습니다. (회원정보 조회 API는 따로 구현하지 않았습니다.)
-SemesterControllerTest 하드코딩해주셨던 부분은 수정하려다가 일단 하지 않았습니다.
-Refresh Token도 일단 localStorage에 저장하는 방식으로 구현했습니다. (쿠키로 배포 전 전환 예정)
-테스트 및 swagger 업데이트 완료하였습니다.
-혹시 구조가 이상하다거나, 불필요한 부분이 있다면 말씀해주시면 감사하겠습니다.

# Spring Security 기반 시나리오 

## 요구사항
- Access / Refresh Token 발행과 검증에 관한 테스트 시나리오 작성하기
- 토큰 발급 및 검증 로직은 Service 계층에서 Test 진행 

## 회원 가입 
- url : /signup
- ResponseDto 에 authorities 포함 되어야 한다. 
## 로그인 
- url : /sign
- ResponseDto 에 token 값은 accessToken 이여야 한다. 
## 백엔드 개발 온보딩 과제(Java)

### EC2 배포 주소
- http://13.125.152.86:8080

### Swagger 주소
- http://13.125.152.86:8080/swagger-ui/index.html

### 구현 기능 
- [x] 회원가입, 로그인 API 구현
  - 로그인 시 AccessToken 헤더 반환
  - 로그인 시 RefreshToken을 Redis 저장후 쿠키 반환 
- [x] AccessToken 재발급 비지니스 로직 구현
  - Redis 에 저장된 RefreshToken 검증 후 AccessToken 재발급
- [x] Spring Security 필터를 통한 인가 필터 작성
- [x] Junit 을 통한 Mokito 기반 단위 테스트 작성
  - sign(토큰 발급)
  - verifyJwt(토큰 검증)
- [x] Swagger - UI 적용
- [x] EC2 배포 

### API 명세서
- 회원가입
  - http://13.125.152.86:8080/signup
  - 요구사항에 따름
- 로그인
  - http://13.125.152.86:8080/sign
  - 요구사항에 따름
- AccessToken 재발급
  - http://13.125.152.86:8080/access-token/reissue
  - Cookie 에 RefreshToken 넣어 주어야 함 
  - 재 생성된 AccessToken 반환



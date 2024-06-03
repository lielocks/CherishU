# 선물 탐색부터 구매까지의 과정에서 어려움을 느끼는 사람들을 위한 선물 큐레이션 웹서비스! CherishU

- [cherishu.com](https://cherishu.web.app/) 에서 확인하실 수 있어요.

---

## 🍻 Intro

<p align="center">
  <img width="300" alt="image" src="https://github.com/lielocks/CherishU/assets/107406265/2f010dcf-b13b-457d-82fe-bf636fd834dd">
</p>
<p align="center">
  <img src="https://img.shields.io/badge/react-v17.0.2-9cf?logo=react" alt="react" />
  <img src="https://img.shields.io/badge/spring_boot-v3.0.4-green?logo=springboot"  alt="spring-boot" />
  <img src="https://img.shields.io/badge/typescript-v4.3.5-blue?logo=typescript" alt="typescript"/>
  <img src="https://img.shields.io/badge/postgresql-v12-blue?logo=postgresql" alt="postgresql"/>
</p>


다양한 상황과 목적에 따라 **필터링** 이 가능하고 </br>
카테고리별 **다양한 상품 검색** 과 **달별 큐레이션 페이지** 를 제공하는 선물 큐레이션 웹 서비스 **CherishU** 입니다. 

가치 있는 선물을 하고 싶을때는? </br>
선물을 하는 사람과 받는 사람 모두 만족감을 느끼고 싶을 때는?

바로, ***CherishU***


</br>


## ⚡️ Skills



### Back-end

<p>
  <img src="https://user-images.githubusercontent.com/52682603/138834253-9bcd8b12-241f-41b2-85c4-d723a16bdb58.png" alt="spring_boot" width=15%>
  <img src="https://user-images.githubusercontent.com/52682603/138834267-c86e4b93-d826-4fd4-bcc8-1294f615a82d.png" alt="hibernate" width=15%>
  <img src="https://user-images.githubusercontent.com/52682603/138834280-73acd37b-97ef-4136-b58e-6138eb4fcc46.png" alt="query_dsl" width=15%>
  <img src="https://user-images.githubusercontent.com/52682603/138834265-5e9d309b-6b78-4c5e-adf3-981f705b7042.png" alt="flyway" width=15%>
  <img src="https://user-images.githubusercontent.com/52682603/138834240-a4d7218f-db96-4c51-83f5-9b80f6d38758.png" alt="spring_rest_docs" width=15%>
</p>

- **Springboot** 로 웹 어플리케이션 서버를 구축했어요.
- **Spring Data JPA(Hibernate)** 로 객체 지향 데이터 로직을 작성했어요.
- **QueryDSL** 로 컴파일 시점에 SQL 오류를 감지해요. 더 가독성 높은 코드를 작성할 수 있어요.
- Spring Jwt
- Spring Actuator (admin server)
- vault security 
- redis elasticache 메일 정보 및 인증 상태 관리
- 상품 이미지 firebase
- aws ses smtp 인증 메일 보내기 기능

---

### Infra Structure

<p>
  <img src="https://github.com/lielocks/CherishU/assets/107406265/e3dfc271-c192-4a84-90cd-3274a04f2690" alt="AWS_EC2" width=15% height="120">
  <img src="https://github.com/lielocks/CherishU/assets/107406265/a45081d1-3fab-4cbb-83b4-b3cac5f2589c" alt="AWS_SES" width=15% height="120">
</p>

- **AWS EC2** 를 사용해 서버를 구축했어요.
- **AWS SES** 를 사용


#### CI/CD

<p>
  <img src="https://user-images.githubusercontent.com/52682603/138834259-b48d26eb-b6e8-490c-a839-450d8ab9bfd2.png" alt="jenkins" width=15%>
  <img src="https://github.com/lielocks/CherishU/assets/107406265/f33f267c-1cb5-486a-8ad7-42c2afab3b24" alt="docker" width=15%>
  <img src="https://user-images.githubusercontent.com/52682603/138834229-e8a9dcb0-bdb8-4aec-9a3e-be1f9ff44149.png" alt="github_actions" width=15%>
</p>

- **Jenkins** 로 백엔드 코드의 지속적 배포와 무중단 배포를 진행해요.
- **Github Actions** 로 코드 퀄리티와 테스트를 검사해요.
- **Docker** 컨테이너를 활용하여 blue-green 를 통해 안전한 무중단 배포를 보장해요.

#### DB

<p>
  <img src="https://github.com/lielocks/CherishU/assets/107406265/abdae15f-4b59-4ed3-ba43-2d670ce3618a" alt="mysql" width=15%>
  <img src="https://github.com/lielocks/CherishU/assets/107406265/235396b4-a0e9-4af9-9c83-8e519ebb80ba" alt="elasticache" width=15%>
  <img src="https://github.com/lielocks/CherishU/assets/107406265/bf1b34b5-550b-4854-84e0-9ebe705eccb9" alt="firebase" width=15% height="120">
</p>

- 데이터 베이스는 **PostgreSQL**을 사용해요.
- elasticache
- **firebase**

#### Config

<p>
  <img src="https://github.com/lielocks/CherishU/assets/107406265/b536099d-90a6-4acf-8316-cf15424667fc" alt="nginx" width=15%>
  <img src="https://github.com/lielocks/CherishU/assets/107406265/29c36dca-021b-43f6-b6e4-ec82ab5f6da7" alt="actuator" width=15%>
</p>

- **vault**
- spring actuator

</br>


## 🌈 Members
|             [신봉규](https://github.com/shinb-bong)             |             [김용현](https://github.com/facewise)             |              [김아리](https://github.com/lielocks)               |
| :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
| <img src="https://github.com/lielocks/CherishU/assets/107406265/c4def301-1ac9-42d1-8ff4-336836a784f6" width=200px alt="_"/> | <img src="https://avatars.githubusercontent.com/u/62998666?v=4" width=200px alt="_"> | <img src="https://avatars.githubusercontent.com/u/107406265?v=4" width=200px alt="_"> |
|                           백엔드                           |                           백엔드                           |                           백엔드                           |

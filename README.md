# Project Name
맛동산 서버

## Table of Contents
[ 📝 Overview](#📝-overview)  
[ 📁 Project Structure](#📁-project-structure)  
[ 🚀 Getting Started](#🚀-getting-started)  
[ 💡 Motivation](#💡-motivation)  
[ 🎬 Demo](#🎬-demo)  
[ 🌐 Deployment](#🌐-deployment)  
[ 🤝 Contributing](#🤝-contributing)  
[ ❓ Troubleshooting & FAQ](#❓-troubleshooting-&-faq)  
[ 📈 Performance](#📈-performance)  

## 📝 Overview
맛동산 서버는 사용자 맞춤형 동화를 생성하고 관리하는 웹 애플리케이션입니다. 이 프로젝트의 주된 목적은 사용자가 동화를 생성하고, 수정하며, 다른 사용자와 공유할 수 있는 플랫폼을 제공하는 것입니다.

### Main Purpose
- 사용자가 동화를 생성하고, 수정하며, 다른 사용자와 공유할 수 있는 기능을 제공합니다.
- 동화 생성 과정에서 AI를 활용하여 창의적인 콘텐츠를 생성합니다.
- 어린이와 부모를 위한 교육적이고 재미있는 경험을 제공합니다.

### Key Features
- 동화 생성 및 수정 기능
- 동화에 대한 좋아요 및 댓글 기능
- 사용자 프로필 관리 및 자녀 등록 기능
- 동화 검색 및 필터링 기능

### Core Technology Stack
- Frontend: React
- Backend: Spring Boot
- Database: MongoDB
- Others: AWS S3, Redis

## 📁 Project Structure
[맛동산 서버]
├── 📁 com.example.matdongsanserver  
│   ├── 📁 common  
│   │   ├── 📁 api  
│   │   ├── 📁 config  
│   │   ├── 📁 exception  
│   │   ├── 📁 external  
│   │   ├── 📁 model  
│   │   └── 📁 utils  
│   ├── 📁 domain  
│   │   ├── 📁 auth  
│   │   ├── 📁 child  
│   │   ├── 📁 follow  
│   │   ├── 📁 library  
│   │   ├── 📁 module  
│   │   ├── 📁 story  
│   │   └── 📁 dashboard  
│   └── MatdongsanServerApplication.java  
└── ...

## 🚀 Getting Started

### Prerequisites
- 지원 운영 체제
  * Windows, macOS, Linux
- 필수 소프트웨어
  * 런타임 환경: Java
  * 버전 요구 사항: Java 17
  * 패키지 관리자: Gradle
- 시스템 종속성
  * 시스템 수준 라이브러리나 도구는 별도로 필요하지 않습니다.

### Installation
- Dockerfile이 있는 경우, 이를 사용할 수 있습니다.
- 모든 설치 방법은 Dockerfile에 포함되어 있습니다.

```bash
# 레포지토리 클론
git clone https://github.com/Nano2998/matdongsan-server/.git
cd matdongsan-server

# 필요한 패키지 설치
./gradlew build

# 환경 설정
# 환경 설정 명령어는 프로젝트의 요구 사항에 따라 다를 수 있습니다.
```

### Usage
```bash
# 실행 방법
./gradlew bootRun
```

## 💡 Motivation
이 프로젝트는 어린이들에게 맞춤형 동화를 제공하고, 부모와 자녀가 함께 즐길 수 있는 경험을 만들기 위해 시작되었습니다. AI 기술을 활용하여 창의적인 동화를 생성하고, 이를 통해 교육적 가치를 높이고자 합니다.

## 🎬 Demo
![Demo Video or Screenshot](path/to/demo.mp4)

## 🌐 Deployment
- AWS, Heroku와 같은 클라우드 플랫폼에 배포 가능합니다.
- 배포 단계는 다음과 같습니다:
  1. AWS S3에 정적 파일 업로드
  2. EC2 인스턴스에 Spring Boot 애플리케이션 배포
  3. 데이터베이스 연결 설정

## 🤝 Contributing
- 기여를 원하시는 분은 이슈를 생성하거나 Pull Request를 제출해 주세요.
- 코드 스타일은 Java의 표준 스타일을 따릅니다.
- Pull Request는 코드 리뷰 후 병합됩니다.

## ❓ Troubleshooting & FAQ
- **Q: 서버가 시작되지 않아요.**  
  A: Java 버전이 맞는지 확인하세요. Java 11 이상이 필요합니다.
  
- **Q: 데이터베이스 연결 오류가 발생합니다.**  
  A: 데이터베이스 설정을 확인하고, MongoDB가 실행 중인지 확인하세요.

## 📈 Performance
- 이 애플리케이션은 MongoDB를 사용하여 빠른 데이터 검색을 지원합니다.
- Redis를 사용하여 캐시를 관리하여 성능을 최적화합니다.
- 동화 생성 요청은 비동기적으로 처리되어 사용자 경험을 향상시킵니다.

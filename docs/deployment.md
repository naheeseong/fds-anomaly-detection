# AWS 배포 가이드

> 이 문서는 **배포 준비물**입니다. 실제 AWS 리소스(EC2/RDS)는 비용이 발생하고
> AWS 계정 자격증명이 필요하므로, 이 저장소를 체크아웃한 사람이 아래 절차를
> 본인의 AWS 계정에서 직접 수행해야 합니다.

## 0. 필요한 것
- AWS 계정, IAM 사용자(EC2/RDS/보안그룹 권한)
- 로컬에 AWS CLI 또는 콘솔 접근
- 이 저장소 (`git clone` 또는 Docker 이미지)

## 1. RDS (MySQL) 생성
1. RDS 콘솔 → MySQL 8.0 인스턴스 생성 (`db.t3.micro`로 시작 가능)
2. DB 이름: `fds_db`, 마스터 사용자/비밀번호 설정
3. 퍼블릭 액세스: 운영에서는 **비활성화** 권장 (EC2와 같은 VPC/보안그룹에서만 접근)
4. 보안 그룹 인바운드: 3306 포트를 EC2 보안그룹에서만 허용
5. 생성 후 `docs/database-schema.md`의 `CREATE TABLE` SQL을 적용:
   ```bash
   mysql -h <RDS 엔드포인트> -u <사용자> -p fds_db < schema.sql
   ```
   (schema.sql은 `docs/database-schema.md`의 3절 SQL을 그대로 옮겨 적용)

## 2. Redis
- 간단히: EC2 인스턴스에 `redis-server` 설치 후 로컬(127.0.0.1)로 사용
- 운영 권장: ElastiCache for Redis 생성 후 엔드포인트를 `REDIS_HOST`로 사용

## 3. EC2 인스턴스
1. Ubuntu 22.04 LTS, `t3.small` 이상 권장
2. 보안 그룹 인바운드 규칙:
   | 포트 | 용도 | 소스 |
   |------|------|------|
   | 22 | SSH | 내 IP만 |
   | 8080 | API/WebSocket/대시보드 | 0.0.0.0/0 (외부 접속용) |
3. EC2에 Docker 설치:
   ```bash
   sudo apt-get update && sudo apt-get install -y docker.io
   sudo systemctl enable --now docker
   ```

## 4. 이미지 빌드 & 실행
로컬 또는 EC2에서:
```bash
docker build -t fds-anomaly-detection .
```

EC2에서 실행 (환경변수는 실제 RDS/Redis 값으로 교체):
```bash
docker run -d --name fds \
  -p 8080:8080 \
  -e DB_HOST=<RDS 엔드포인트> \
  -e DB_NAME=fds_db \
  -e DB_USERNAME=<RDS 사용자> \
  -e DB_PASSWORD=<RDS 비밀번호> \
  -e REDIS_HOST=<Redis 엔드포인트 또는 127.0.0.1> \
  --restart unless-stopped \
  fds-anomaly-detection
```

`application-prod.properties`가 위 환경변수를 읽어 RDS/Redis에 연결한다
(`--spring.profiles.active=prod`는 `Dockerfile`의 `ENTRYPOINT`에 이미 포함됨).

## 5. 외부 접속 확인
```bash
curl http://<EC2 퍼블릭 IP>:8080/api/transactions
```
브라우저에서 `http://<EC2 퍼블릭 IP>:8080/` 접속 시 대시보드가 표시되고,
WebSocket은 `ws://<EC2 퍼블릭 IP>:8080/ws/transactions`로 자동 연결된다
(`src/main/resources/static/index.html`이 `location.host`를 그대로 사용하므로
별도 설정 없이 동작).

## 6. 운영 체크리스트
- [ ] RDS 퍼블릭 액세스 비활성화, EC2 보안그룹에서만 3306 허용
- [ ] `application-prod.properties`의 비밀번호를 커밋하지 말 것 (환경변수로만 주입)
- [ ] `spring.jpa.hibernate.ddl-auto=validate` 이므로 배포 전 반드시 스키마를 수동 적용
- [ ] 도메인 연결 시 EC2 앞단에 ALB/Nginx + HTTPS(ACM) 구성 권장 (현재는 HTTP:8080 직접 노출)
- [ ] systemd 또는 `docker run --restart unless-stopped`로 재기동 보장

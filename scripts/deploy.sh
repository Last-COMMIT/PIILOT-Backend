#!/bin/bash
# ===========================================
# PIILOT Backend 배포 스크립트
# ===========================================
# CodeDeploy Agent가 EC2-A에서 이 스크립트를 실행합니다.
#
# 동작 순서:
#   1. DockerHub에서 최신 이미지 pull
#   2. backend 컨테이너만 재시작 (DB, Redis는 건드리지 않음)
#   3. 오래된 이미지 정리 (디스크 공간 절약)
#   4. 헬스체크로 정상 작동 확인
# ===========================================

set -e

DEPLOY_DIR="/opt/piilot"
SERVICE_NAME="backend"
IMAGE="chanee29/piilot-backend:latest"

echo "[$(date)] =========================================="
echo "[$(date)] Backend 배포 시작"
echo "[$(date)] =========================================="

# docker-compose.yml이 있는 디렉토리로 이동
cd "$DEPLOY_DIR"

# 1. DockerHub에서 최신 이미지 받기
echo "[$(date)] Docker 이미지 pull 중: $IMAGE"
docker pull "$IMAGE"

# 2. backend 컨테이너만 재시작
# --no-deps: 의존 서비스(postgres, redis)는 건드리지 않음
# --force-recreate: 이미지가 바뀌었으니 컨테이너를 새로 만듦
echo "[$(date)] backend 컨테이너 재시작 중..."
docker compose up -d --no-deps --force-recreate "$SERVICE_NAME"

# 3. 사용하지 않는 오래된 이미지 정리 (디스크 공간 절약)
echo "[$(date)] 오래된 이미지 정리 중..."
docker image prune -f

# 4. 헬스체크: 5초 간격으로 최대 150초(30번) 동안 확인
echo "[$(date)] 헬스체크 시작 (최대 150초)..."
for i in $(seq 1 30); do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "[$(date)] Backend 헬스체크 성공! 배포 완료!"
        exit 0
    fi
    echo "[$(date)] 대기 중... ($i/30)"
    sleep 5
done

# 헬스체크 실패
echo "[$(date)] ERROR: Backend 헬스체크 실패 (150초 초과)"
echo "[$(date)] 컨테이너 로그 확인:"
docker logs --tail 50 piilot-backend
exit 1

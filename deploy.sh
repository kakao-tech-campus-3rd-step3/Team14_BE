#!/bin/bash

CURRENT=$(grep "set" /etc/nginx/conf.d/service-env.inc | awk '{print $3}' | sed 's/;//')

if [ "$CURRENT" = "blue" ]; then
  IDLE=green
  PORT=8081
else
  IDLE=blue
  PORT=8080
fi

echo "현재 서비스: $CURRENT → 신규 서비스: $IDLE ($PORT)"

docker pull $1
docker rm -f festa-pick-$IDLE || true
docker run -d --restart=always -e  TZ=Asia/Seoul --name festa-pick-$IDLE -p $PORT:8080 $1

for i in {1..20}; do
  echo "$i START (health check)"
  sleep 5
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:$PORT/management/health_check || true)
  RES=$(curl -s http://127.0.0.1:$PORT/management/health_check || true)
  echo "STATUS=$STATUS, BODY=$RES"
  if [[ "$STATUS" == "200" && $RES == *"UP"* ]]; then
    echo "Health check success"
    echo "set \$service_url $IDLE;" | sudo tee /etc/nginx/conf.d/service-env.inc
    sudo nginx -t && sudo nginx -s reload
    docker rm -f festa-pick-$CURRENT || true
    exit 0
  fi
done

echo "health check failed"
exit 1

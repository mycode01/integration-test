# 배포될 Elastic Beanstalk 생성 및 세팅

AWS console에서 EB를 생성한다.    
애플리케이션이름은 해당 환경의 이름으로 지정한다.    
기본구성의 플랫폼이 중요한데, 자신이 배포할 환경에 맞게 선택하면 된다.    
Spring의 경우 java, Tomcat 둘다 사용 가능한데,     
직접 Apache 웹 서버나 nginx를 이용하여 어플리케이션을 관리하고 싶다면 Java를,     
그렇지 않고 Tomcat에다 war로 어플리케이션을 올리고 싶다면 Tomcat을 선택하면 된다.    
이 문서에서는 Java환경을 설명한다.    
추가 옵션 구성을 눌러 필요한 환경 설정을 해준다. 특히 Loadbalancer나 VPC설정은 일단 EB가 실행된 이후에는 수정할수 없는 것 같다.    
VPC를 설정할 경우 배포에 사용될 S3 버킷, Jenkins서버와 같은 VPC를 이용하도록 하자.    
프리티어를 이용하는 경우 정해진 스펙대로만 사용가능하다.(VPC, LB사용불가)    
앱 생성 버튼을 누르고 잠시 기다리면 EB 환경이 생성된다.    
    
대시보드가 로딩되면 애플리케이션의 환경 이름을 기억한다. 해당 이름으로 애플리케이션을 배포한다.    
    
젠킨스로 돌아가 빌드 스크립트를 작성한다.    
```
chmod +x gradlew
# Build
./gradlew clean build

# Zip
cd ./build/libs
cp -r ../../.ebextensions .ebextensions
mv *.jar application.jar
zip -r ${PROJECT_NAME}.zip application.jar .ebextensions

# Upload S3
aws s3 cp ${PROJECT_NAME}.zip s3://${S3_NAME}/${PROJECT_NAME}-${GIT_COMMIT}-${BUILD_TAG}.zip \
--acl public-read-write \
--region ap-northeast-2

# Execute Beanstalk
aws elasticbeanstalk create-application-version \
--region ap-northeast-2 \
--application-name ${PROJECT_NAME} \
--version-label ${GIT_COMMIT}-${BUILD_TAG} \
--description ${PROJECT_NAME}-${GIT_COMMIT}-${BUILD_TAG} \
--source-bundle S3Bucket="${S3_NAME}",S3Key="${PROJECT_NAME}-${GIT_COMMIT}-${BUILD_TAG}.zip"

aws elasticbeanstalk update-environment \
--region ap-northeast-2 \
--environment-name ${ENV_NAME} \
--version-label ${GIT_COMMIT}-${BUILD_TAG}
```

${STRING} 은 젠킨스 빌드 설정에 미리 입력한 파라메터이거나 Env에 등록된 파라메터이다.    
위의 스크립트는 빌드가 완료되면 해당 바이너리와 .ebextension 스크립트를 압축하여 S3 버킷을 통해     
EB로 배포하는 내용이다. (빼먹었지만 배포될 바이너리가 보관될 S3 저장소가 필요하다)    
${S3_NAME}, ${PROJECT_NAME}, ${ENV_NAME} 은 젠킨스에서 설정한 빌드시 파라메터이다.    
위 내용중 S3_NAME은 버킷 저장소 이름, PROJECT_NAME은 EB를 생성할때 만든 어플리케이션 이름, ENV_NAME은 EB생성후 만들어진 환경이름이다.    
    
.ebextension은 배포시 실행될 스크립트나 현재 JAVA환경에서 설정될 nginx.conf의 설정파일을 담는다.    
배포될 번들이미지구조는 아래를 참고하자.     
```
~/workspace/my-app/
|-- .ebextensions
|   `-- nginx
|       `-- conf.d
|           `-- myconf.conf
|       `-- nginx.conf
`-- web.jar
```
스크립트는 alphabetical sort 순으로 실행되므로 순서가 중요하다면     
파일 이름을 00-set-timezone.config, 01-set-charsetting.config 이런식으로 짓도록 하자.    
    
기본설정으로 배포시 spring 어플리케이션의 포트가 8080을 listen하는데,     
nginx가 80포트를 listen 하고는 있지만 해당 커넥션을 8080으로 프록시해주지 않기때문에 정상적으로 리소스에 접근하지 못한다.    
때문에 .ebextension에 nginx설정을 추가 해서 배포하자.    
아래는 nginx.config의 전문이다.    
```
user                    nginx;
error_log               /var/log/nginx/error.log warn;
pid                     /var/run/nginx.pid;
worker_processes        auto;
worker_rlimit_nofile    33282;

events {
    worker_connections  1024;
}

http {
  include       /etc/nginx/mime.types;
  default_type  application/octet-stream;

  log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

  include       conf.d/*.conf;

  map $http_upgrade $connection_upgrade {
      default     "upgrade";
  }

  server {
      listen        80 default_server;
      root /var/app/current/public;

      location / {
                       proxy_pass          http://127.0.0.1:8080;
                       proxy_http_version  1.1;
       
                       proxy_set_header    Connection          $connection_upgrade;
                       proxy_set_header    Upgrade             $http_upgrade;
                       proxy_set_header    Host                $host;
                       proxy_set_header    X-Real-IP           $remote_addr;
                       proxy_set_header    X-Forwarded-For     $proxy_add_x_forwarded_for;
             }

      access_log    /var/log/nginx/access.log main;

      client_header_timeout 60;
      client_body_timeout   60;
      keepalive_timeout     60;
      gzip                  off;
      gzip_comp_level       4;

      # Include the Elastic Beanstalk generated locations
      include conf.d/elasticbeanstalk/01_static.conf;
      include conf.d/elasticbeanstalk/healthd.conf;
  }
}
```
한 인스턴스에 여러개의 어플리케이션을 서비스하고싶다면 nginx.conf를 쭉 작성해 나갈수도있고    
사이트마다 conf파일을 분리하여 include 시킬수도 있다.    


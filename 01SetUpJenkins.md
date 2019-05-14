# 젠킨스 셋업 with AWS EC2

젠킨스 서버는 그냥 AWS 기본 AMI로 생성.    
자바 버전 확인후 java 8이상의 버전으로 설치한다.    
젠킨스 설치후 service jenkins start, curl localhost:8080 을 확인, (젠킨스 기본포트는 8080이다)    
서버에서 작업을 할수도 있다 생각되면 nginx를 설치하여 80포트로 프록시 시켜준다. or 젠킨스 설정자체를 80포트로 수정하자.    
80포트로 프록시 된 것을 확인후 해당 서버 주소에 브라우저로 접속하여 시키는대로 계정만들고 플러그인을 설치하자.    

추후 GitHub과의 연동을 통해서 배포를 할 계획이라면 GitHub에 연동할 방법 중
id/pass보다는 SSH key를 이용하자.

CLI에 다음의 명령을 입력하여 private키와 public키를 생성한다.    
```ssh-keygen -t rsa -f id_rsa```    
그리고 젠킨스 credential로 개인키 파일의 내용을 추가한다.    
편의상 하나의 키로 글을 작성하였지만, 저장소가 여러개라면 프로젝트마다 키를 생성하여 등록할수 있다.    
이후 public 키를 관리될 Github repository의 setting > deploy key에 등록하도록 한다.

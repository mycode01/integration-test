# 젠킨스와 연동하기 위한 GitHub repository 설정

젠킨스 서버와 연결하기 위해서 발급한 SSH Public key를 Repository > Settings > deploy key에 등록한다.    
이렇게 까지만 하면 배포하기 위한 세팅은 끝나지만, 특정한 Branch에 변화가 생길경우 해당 Branch를 배포하고 싶다면
Settings > WebHooks 를 등록한다.

등록할 주소는 젠킨스서버의 특정한 주소가 되는데    
젠킨스서버주소/github-webhook/ 으로 지정한다. 젠킨스에서 제공하는 기능이므로 포트번호를 맞춰준다.

이후 딜리버리가 되는지 확인한 후, 문제가 있다면 AWS 보안그룹에 의한 block일수 있으므로    
https://api.github.com/meta 를 참고하여 화이트리스트를 작성하여 테스트한다.

    

여기까지 끝난다면 정해진 Repository에 변화가 생겼을 경우 Webhook으로 지정된 엔드포인트(젠킨스)에 메시지가 발생하고(post method)
해당 메시지를 수신하면 젠킨스는 확인하여 빌드유발 트리거를 통해 정해진 Branch를 빌드할 준비가 완료된다.

하지만 이 시점에 아직 젠킨스의 빌드 유발 트리거를 설정하지 않았기때문에 아직 불가하고,    
젠킨스에서 빌드하고싶은 프로젝트를 생성 후 소스코드 관리에서 GitHub repository와 credential을 지정하고 나면    
트리거에 의해서 젠킨스 빌드가 가능해짐.

빌드 스크립트를 작성하고 저장하고 난 후 Build with parameter 메뉴를 사용하여 테스트.    
권한 문제가 생길수있으므로 빌드 수행 배치파일에 (Gradle)    
`chmod +x gradlew` 로 실행권한을 주고 빌드스크립트를 작성한다.


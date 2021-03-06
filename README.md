# r2dbc + webflux 조합과 mybatis의 성능 비교

https://spring.io/guides/gs/accessing-data-r2dbc 에서 제공하는 샘플에 Mybatis와 Taurus 부하 테스트 설정을 추가.

## 환경

* Spring Boot 2.4.2
* r2dbc 
* Mybatis 2.1.4 
* H2 database
* Taurus BlazeMeter

## Reactive System

* Backend, Frontend 양쪽 다 Reactive 적용을 해야 의미가 있음
* 복잡한 MSA 아키텍처에서 유용함

## 부하 테스트
* ~/httpTest/taurus.sh 참고
	
~~~~
[concurrency: 500]
┌───────────────┬────────┬─────────┬────────┬───────────────────────────────────────────┐
│ label         │ status │    succ │ avg_rt │ error                                     │
├───────────────┼────────┼─────────┼────────┼───────────────────────────────────────────┤
│ /r2/customers │  FAIL  │ 100.00% │  0.122 │ Non HTTP response message: Read timed out │
└───────────────┴────────┴─────────┴────────┴───────────────────────────────────────────┘
┌───────────────┬────────┬─────────┬────────┬──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ label         │ status │    succ │ avg_rt │ error                                                                                                │
├───────────────┼────────┼─────────┼────────┼──────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ /mb/customers │  FAIL  │ 100.00% │  0.082 │ Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1] failed: connect timed out │
│               │        │         │        │ Non HTTP response message: Connection reset                                                          │
└───────────────┴────────┴─────────┴────────┴──────────────────────────────────────────────────────────────────────────────────────────────────────┘

[concurrency : 1000]
┌───────────────┬────────┬─────────┬────────┬───────────────────────────────────────────┐
│ label         │ status │    succ │ avg_rt │ error                                     │
├───────────────┼────────┼─────────┼────────┼───────────────────────────────────────────┤
│ /r2/customers │  FAIL  │ 100.00% │  0.245 │ Non HTTP response message: Read timed out │
└───────────────┴────────┴─────────┴────────┴───────────────────────────────────────────┘
┌───────────────┬────────┬────────┬────────┬──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ label         │ status │   succ │ avg_rt │ error                                                                                                │
├───────────────┼────────┼────────┼────────┼──────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ /mb/customers │  FAIL  │ 99.96% │  0.146 │ Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1] failed: connect timed out │
└───────────────┴────────┴────────┴────────┴──────────────────────────────────────────────────────────────────────────────────────────────────────┘

[concurrency : 2000]
┌───────────────┬────────┬─────────┬────────┬───────────────────────────────────────────┐
│ label         │ status │    succ │ avg_rt │ error                                     │
├───────────────┼────────┼─────────┼────────┼───────────────────────────────────────────┤
│ /r2/customers │  FAIL  │ 100.00% │  0.494 │ Non HTTP response message: Read timed out │
└───────────────┴────────┴─────────┴────────┴───────────────────────────────────────────┘
┌───────────────┬────────┬────────┬────────┬──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ label         │ status │   succ │ avg_rt │ error                                                                                                │
├───────────────┼────────┼────────┼────────┼──────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ /mb/customers │  FAIL  │ 99.65% │  0.259 │ Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1] failed: connect timed out │
└───────────────┴────────┴────────┴────────┴──────────────────────────────────────────────────────────────────────────────────────────────────────┘

[concurrency : 500, 실세상황 시뮬레이션을 위해 딜레이 추가]
* Mybatis : loop내에 Thread.sleep(10) 추가
* r2dbs : repository.findAll().delayElements(Duration.ofMillis(10));
┌───────────────┬────────┬────────┬────────┬──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ label         │ status │   succ │ avg_rt │ error                                                                                                │
├───────────────┼────────┼────────┼────────┼──────────────────────────────────────────────────────────────────────────────────────────────────────┤
│ /mb/customers │  FAIL  │ 99.85% │  0.306 │ Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1] failed: connect timed out │
│ /r2/customers │  FAIL  │ 95.55% │  0.863 │ The operation lasted too long                                                                        │
│               │        │        │        │ Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1] failed: connect timed out │
│               │        │        │        │ Non HTTP response message: Read timed out                                                            │
└───────────────┴────────┴────────┴────────┴──────────────────────────────────────────────────────────────────────────────────────────────────────┘
~~~~

## 현재까지의 결론

* 업무 로직이 전혀 없고 DB 쪽이 충분히 빠르다면 r2dbc보다 Mybatis쪽이 더 빠르고 처리량도 더 많다.
* 업무 로직이 있는 경우도 결과는 동일하다(시뮬레이션이 잘못되었을수도 있다).

## 기타

* h2 database 콘솔 : http://localhost:8080/h2-console
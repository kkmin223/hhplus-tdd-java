# hhplus-tdd-java
## 동시성 프로그래밍에서 문제가 발생하는 원인
CPU가 작업을 처리할 때 공유자원인 RAM에서 일부분을 성능이 좋은 CPU Cache Memory로 읽어 들인 이후에, 작업을 수행하고 CPU Cache Memory에 작업을 반영하고 이후에 RAM에 반영하게 됩니다.

위 작업이 멀티 스레드 환경에서 병렬적으로 수행하게 된다면, RAM에 있는 공유 자원에 접근할 때 가시성 문제와 원자성 문제가 밸생할 수 있습니다.

가시성 문제란 여러 개의 스레드가 병렬적으로 처리하게 되면서 CPU Cache Memory와 RAM의 데이터가 서로 일치하지 않게 되는 문제입니다.

원자성 문제는 한 스레드가 작업을 수행하는 동안 다른 스레드가 개입하여 공유 자원에 접근하여 같은 작업을 수행함으로써 발생하는 문제입니다.

자바는 static, heap, stack 영역으로 나누어져 있는데, 멀티 스레드 환경에서 static, heap 영역을 공유하므로 위와 같은 문제가 동일하게 발생할 수 있습니다.

## 자바에서 동시성 문제를 해결하는 키워드

### volatile
volatile는 가시성 문제를 해결하기 위한 키워드로, 해당 키워드를 사용한 변수는 메인 메모리로부터 읽을 수 있게 해줍니다.
위와 같은 방식으로 공유 데이터를 읽는 경우에는 동시성을 보장하지만 메인 메모리를 읽은 이후에 다른 스레드에 의해서 이 값이 변경하게 된다면 메인 메모리와 읽은 데이터가 일치하지 않게 되므로 해당 데이터를 수정하게 되면 동시성 문제는 동일하게 발생합니다.

### synchronized
synchronized는 원자성을 보장하기 위한 키워드로, 메소드 또는 블록에 붙여 지정한 범위에서 해당 자원을 사용할 때 다른 스레드가 동시에 사용할 수 없도록 Lock을 걸고 범위에서 벗어나면 Lock을 푸는 방식입니다.

사용하는 방식으로는 sychronized method, sychronized block, static sychronized method, static synchonized block 이 있고 각각 의 방식을 사용하면 클래스 단위 또는 인스턴스 단위로 lock을 공유하게 됩니다.

해당 방식은 클래스, 인스턴스 단위로 Lock을 걸게되면 다른 스레드의 접근을 완전히 막게 되기 때문에 큰 비용이 발생하게 됩니다.

### atomic
tomic은 원자성을 보장하기 위한 키워드로, synchronized와 달리 CAS(Compared And Swap)알고리즘으로 작동합니다.
CAS 알고리즘은 CPU Cache Memory와 RAM을 비교하여 일치한다면 CPU Cache Memory와 RAM에 적용하고, 일치하지 않는다면 재시도함으로써 어떠한 스레드에서 공유 자원에 읽기/쓰기 작업을 하더라도 원자성을 보장합니다.
자바의 Concurrent 패키지의 타입들이 CAS 알고리즘을 이용해 원자성을 보장합니다.

### ReentrantLock
ReentrantLock은 락을 획득/해제하는 시점을 명시적으로 관리할 수 있고, 동일한 스레드가 이미 획득한 락을 다시 획득할 수 있게 함으로써 데드락 상황을 방지합니다.

### ConcurrentHashMap
ConcurrentHashMap은 멀티 스레드 환경에서 여러 스레드가 동시에 읽고 쓰는 작업을 수행해도 데이터의 일관성을 유지하는 Map입니다.
ConcurrentHashMap은 Map단위가 아닌 버킷 단위로 락을 분할하여 put, remove 등의 작업도 버킷 단위로 병렬 처리가 가능합니다.

### ReentrantLock + ConcurrentHashMap
락을 관리할 수 있는 ReentrantLock과 멀티 스레드 환경에서 버킷 단위로 데이터의 일관성을 유지해주는 ConcurrentHashMap을 사용하면, 특정 기준(버킷)을 이용하여 락을 관리함으로써 락이 제한하는 범위를 최소한으로 잡을 수 있게 됩니다.

## 1주차 과제에서 사용할 동시성 제어 방법
1주차 과제에서 같은 유저가 포인트를 멀티 스레드 환경에서 수정하게 될 때 동시성 문제가 발생하게 됩니다.

공유 자원을 수정할 때 동시성 문제를 해결하기 위해서는 synchronized, ReentrantLock + ConcurrentHashMap을 사용할 수 있습니다.

synchronized를 사용하게 된다면 PointService 클래스 단위로 충전과 사용 메소드에 대해서 lock을 거는 방법을 사용할 수 있습니다.
이 때 발생할 수 있는 문제점은 A라는 사용자가 충전/사용을 할 때 B라는 사용자는 같은 데이터에 접근하지 않지만 A라는 사용자의 요청이 끝날 때까지 lock을 획들할 수 없게 되므로 동시성 문제는 해결 가능하지만 성능상 적합하지 않다고 생각합니다.

ReentrantLock + ConcurrentHashMap을 사용하게 되면 각 유저ID별로 버킷을 생성하여 락을 관리할 수 있게 됩니다. 유저는 다른 사람이 포인트를 충전/사용하여도 유저별 lock을 획득할 수 있게 되면서 LocK의 범위는 최소화하여 성능상에 이점을 얻을 수 있습니다.

위와 같은 이유로 ReentrantLock + ConcurrentHashMap을 사용하여 1주차 과제를 수행하였습니다.

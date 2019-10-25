Jenkins Concurrent Step Plugin
========
Jenkins plugin to synchronize status among parallel stages in pipeline.  
** This plugin steps can't recover from Jenkins crash and can only be used in pipeline script **

# Block and Asynchronous
This plugin doesn't block the pipeline main thread.
It leverages Jenkins asynchronous execution so the parallel stages continue to execute in other branches. 
# [Barrier](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CyclicBarrier.html)
```
def barrier = createBarrier count: 3;
boolean out = false
parallel(
        await1: {
            awaitBarrier barrier
            echo "out=${out}"
        },
        await2: {
            awaitBarrier (barrier){
                sleep 2 //simulate a long time execution.
            }
            echo "out=${out}"
        },
        await3: {
            awaitBarrier (barrier){
                sleep 3 //simulate a long time execution.
                out = true
            }
            echo "out=${out}"
        }
)
```
# [Latch](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CountDownLatch.html)
```
def latch = createLatch count: 2;
def var1 = false;
def var2 = false;
parallel(
        wait: {
            awaitLatch latch
            echo "var1=${var1}"
            echo "var2=${var2}"
        },
        countdown1: {
            countDownLatch (latch) {
                sleep 3 //simulate a long time execution.
                var1 = true
            }
        },
        countdown2: {
            countDownLatch (latch) {
                sleep 2 //simulate a long time execution.
                var2 = true
            }
        }
)
```
# [Semaphore](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Semaphore.html)
```
def semaphore = createSemaphore permit:1
def out2=0
parallel(
        semaphore1: {
            acquireSemaphore (semaphore){
                echo "out1 1"   //actions after acurire a semaphore and before release The semaphore is automatically released
                sleep 3
                out2=2
            }
        },
        semaphore2: {
            sleep 1
            acquireSemaphore (semaphore){
                echo "out2 ${out2}"
            }
        }
)
```
# [Condition](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/Condition.html)
```
def condition = createCondition()
def out = false
parallel(
        wait1: {
            awaitCondition condition
            echo "out=${out}"
        },
        wait2: {
            awaitCondition condition
            echo "out=${out}"
        },
        signalAll: {
            signalAll (condition:condition) {
                sleep 3 //simulate a long time execution.
                out = true
            }
        }
)
```
# *Release* Steps with closure body
It usually requires `try/finally` handling in release branch.
To simplify pipeline code, all *release* steps has an optional closure parameter.
The lock will be released immediately after closure code is executed even an exception is thrown in the block.  
Because of its semantic, Semaphore block closure is a parameter of acquireSemaphore and should not release the lock manually again.
     
# Samples
See more samples at [src/test/resources]
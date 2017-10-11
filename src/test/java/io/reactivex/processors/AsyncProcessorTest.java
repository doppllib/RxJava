/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.processors;

import com.google.j2objc.annotations.AutoreleasePool;

import co.touchlab.doppl.testing.DopplHacks;
import io.reactivex.TestHelper;
import io.reactivex.exceptions.TestException;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.fuseable.QueueSubscription;
import io.reactivex.internal.subscriptions.BooleanSubscription;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.SubscriberFusion;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

import static org.mockito.Mockito.*;

public class AsyncProcessorTest extends DelayedFlowableProcessorTest<Object> {

    private final Throwable testException = new Throwable();

    @Override
    protected FlowableProcessor<Object> create() {
        return AsyncProcessor.create();
    }

    @Test
    public void testNeverCompleted() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();
        subject.subscribe(observer);

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");

        verify(observer, Mockito.never()).onNext(anyString());
        verify(observer, Mockito.never()).onError(testException);
        verify(observer, Mockito.never()).onComplete();
    }

    @Test
    public void testCompleted() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();
        subject.subscribe(observer);

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onComplete();

        verify(observer, times(1)).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    @Ignore("Null values not allowed")
    public void testNull() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();
        subject.subscribe(observer);

        subject.onNext(null);
        subject.onComplete();

        verify(observer, times(1)).onNext(null);
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void testSubscribeAfterCompleted() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onComplete();

        subject.subscribe(observer);

        verify(observer, times(1)).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void testSubscribeAfterError() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");

        RuntimeException re = new RuntimeException("failed");
        subject.onError(re);

        subject.subscribe(observer);

        verify(observer, times(1)).onError(re);
        verify(observer, Mockito.never()).onNext(any(String.class));
        verify(observer, Mockito.never()).onComplete();
    }

    @Test
    public void testError() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();
        subject.subscribe(observer);

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onError(testException);
        subject.onNext("four");
        subject.onError(new Throwable());
        subject.onComplete();

        verify(observer, Mockito.never()).onNext(anyString());
        verify(observer, times(1)).onError(testException);
        verify(observer, Mockito.never()).onComplete();
    }

    @Test
    public void testUnsubscribeBeforeCompleted() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<String>(observer);
        subject.subscribe(ts);

        subject.onNext("one");
        subject.onNext("two");

        ts.dispose();

        verify(observer, Mockito.never()).onNext(anyString());
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, Mockito.never()).onComplete();

        subject.onNext("three");
        subject.onComplete();

        verify(observer, Mockito.never()).onNext(anyString());
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, Mockito.never()).onComplete();
    }

    @Test
    public void testEmptySubjectCompleted() {
        AsyncProcessor<String> subject = AsyncProcessor.create();

        Subscriber<String> observer = TestHelper.mockSubscriber();
        subject.subscribe(observer);

        subject.onComplete();

        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, never()).onNext(null);
        inOrder.verify(observer, never()).onNext(any(String.class));
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Can receive timeout if subscribe never receives an onError/onComplete ... which reveals a race condition.
     */
    @Test(timeout = 10000)
    public void testSubscribeCompletionRaceCondition() {
        /*
         * With non-threadsafe code this fails most of the time on my dev laptop and is non-deterministic enough
         * to act as a unit test to the race conditions.
         *
         * With the synchronization code in place I can not get this to fail on my laptop.
         */
        for (int i = 0; i < 30; i++) {
            final AsyncProcessor<String> subject = AsyncProcessor.create();
            final AtomicReference<String> value1 = new AtomicReference<String>();

            subject.subscribe(new Consumer<String>() {

                @Override
                public void accept(String t1) {
                    try {
                        // simulate a slow observer
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    value1.set(t1);
                }

            });

            Thread t1 = new Thread(new Runnable() {

                @Override
                public void run() {
                    subject.onNext("value");
                    subject.onComplete();
                }
            });

            SubjectSubscriberThread t2 = new SubjectSubscriberThread(subject);
            SubjectSubscriberThread t3 = new SubjectSubscriberThread(subject);
            SubjectSubscriberThread t4 = new SubjectSubscriberThread(subject);
            SubjectSubscriberThread t5 = new SubjectSubscriberThread(subject);

            t2.start();
            t3.start();
            t1.start();
            t4.start();
            t5.start();
            try {
                t1.join();
                t2.join();
                t3.join();
                t4.join();
                t5.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            assertEquals("value", value1.get());
            assertEquals("value", t2.value.get());
            assertEquals("value", t3.value.get());
            assertEquals("value", t4.value.get());
            assertEquals("value", t5.value.get());
        }

    }

    private static class SubjectSubscriberThread extends Thread {

        private final AsyncProcessor<String> subject;
        private final AtomicReference<String> value = new AtomicReference<String>();

        SubjectSubscriberThread(AsyncProcessor<String> subject) {
            this.subject = subject;
        }

        @Override
        public void run() {
            try {
                // a timeout exception will happen if we don't get a terminal state
                String v = subject.timeout(2000, TimeUnit.MILLISECONDS).blockingSingle();
                value.set(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // FIXME subscriber methods are not allowed to throw
//    @Test
//    public void testOnErrorThrowsDoesntPreventDelivery() {
//        AsyncSubject<String> ps = AsyncSubject.create();
//
//        ps.subscribe();
//        TestSubscriber<String> ts = new TestSubscriber<String>();
//        ps.subscribe(ts);
//
//        try {
//            ps.onError(new RuntimeException("an exception"));
//            fail("expect OnErrorNotImplementedException");
//        } catch (OnErrorNotImplementedException e) {
//            // ignore
//        }
//        // even though the onError above throws we should still receive it on the other subscriber
//        assertEquals(1, ts.getOnErrorEvents().size());
//    }


    // FIXME subscriber methods are not allowed to throw
//    /**
//     * This one has multiple failures so should get a CompositeException
//     */
//    @Test
//    public void testOnErrorThrowsDoesntPreventDelivery2() {
//        AsyncSubject<String> ps = AsyncSubject.create();
//
//        ps.subscribe();
//        ps.subscribe();
//        TestSubscriber<String> ts = new TestSubscriber<String>();
//        ps.subscribe(ts);
//        ps.subscribe();
//        ps.subscribe();
//        ps.subscribe();
//
//        try {
//            ps.onError(new RuntimeException("an exception"));
//            fail("expect OnErrorNotImplementedException");
//        } catch (CompositeException e) {
//            // we should have 5 of them
//            assertEquals(5, e.getExceptions().size());
//        }
//        // even though the onError above throws we should still receive it on the other subscriber
//        assertEquals(1, ts.getOnErrorEvents().size());
//    }

    @Test
    public void testCurrentStateMethodsNormal() {
        AsyncProcessor<Object> as = AsyncProcessor.create();

        assertFalse(as.hasValue());
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getValue());
        assertNull(as.getThrowable());

        as.onNext(1);

        assertFalse(as.hasValue()); // AP no longer reports it has a value until it is terminated
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getValue()); // AP no longer reports it has a value until it is terminated
        assertNull(as.getThrowable());

        as.onComplete();
        assertTrue(as.hasValue());
        assertFalse(as.hasThrowable());
        assertTrue(as.hasComplete());
        assertEquals(1, as.getValue());
        assertNull(as.getThrowable());
    }

    @Test
    public void testCurrentStateMethodsEmpty() {
        AsyncProcessor<Object> as = AsyncProcessor.create();

        assertFalse(as.hasValue());
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getValue());
        assertNull(as.getThrowable());

        as.onComplete();

        assertFalse(as.hasValue());
        assertFalse(as.hasThrowable());
        assertTrue(as.hasComplete());
        assertNull(as.getValue());
        assertNull(as.getThrowable());
    }
    @Test
    public void testCurrentStateMethodsError() {
        AsyncProcessor<Object> as = AsyncProcessor.create();

        assertFalse(as.hasValue());
        assertFalse(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getValue());
        assertNull(as.getThrowable());

        as.onError(new TestException());

        assertFalse(as.hasValue());
        assertTrue(as.hasThrowable());
        assertFalse(as.hasComplete());
        assertNull(as.getValue());
        assertTrue(as.getThrowable() instanceof TestException);
    }

    @Test
    public void fusionLive() {
        AsyncProcessor<Integer> ap = new AsyncProcessor<Integer>();

        TestSubscriber<Integer> ts = SubscriberFusion.newTest(QueueSubscription.ANY);

        ap.subscribe(ts);

        ts
        .assertOf(SubscriberFusion.<Integer>assertFuseable())
        .assertOf(SubscriberFusion.<Integer>assertFusionMode(QueueSubscription.ASYNC));

        ts.assertNoValues().assertNoErrors().assertNotComplete();

        ap.onNext(1);

        ts.assertNoValues().assertNoErrors().assertNotComplete();

        ap.onComplete();

        ts.assertResult(1);
    }

    @Test
    public void fusionOfflie() {
        AsyncProcessor<Integer> ap = new AsyncProcessor<Integer>();
        ap.onNext(1);
        ap.onComplete();

        TestSubscriber<Integer> ts = SubscriberFusion.newTest(QueueSubscription.ANY);

        ap.subscribe(ts);

        ts
        .assertOf(SubscriberFusion.<Integer>assertFuseable())
        .assertOf(SubscriberFusion.<Integer>assertFusionMode(QueueSubscription.ASYNC))
        .assertResult(1);
    }

    @Test
    public void onSubscribeAfterDone() {
        AsyncProcessor<Object> p = AsyncProcessor.create();

        BooleanSubscription bs = new BooleanSubscription();
        p.onSubscribe(bs);

        assertFalse(bs.isCancelled());

        p.onComplete();

        bs = new BooleanSubscription();
        p.onSubscribe(bs);

        assertTrue(bs.isCancelled());

        p.test().assertResult();
    }

    @Test
    public void cancelUpfront() {
        AsyncProcessor<Object> p = AsyncProcessor.create();

        assertFalse(p.hasSubscribers());

        p.test().assertEmpty();
        p.test().assertEmpty();

        p.test(0L, true).assertEmpty();

        assertTrue(p.hasSubscribers());
    }

    @Test
    public void cancelRace() {
        AsyncProcessor<Object> p = AsyncProcessor.create();

        for (int i = 0; i < 500; i++) {
            final TestSubscriber<Object> ts1 = p.test();
            final TestSubscriber<Object> ts2 = p.test();

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    ts1.cancel();
                }
            };

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    ts2.cancel();
                }
            };

            TestHelper.race(r1, r2, Schedulers.single());
        }
    }

    @Test
    public void onErrorCancelRace() {

        for (int i = 0; i < 500; i++) {
            final AsyncProcessor<Object> p = AsyncProcessor.create();

            final TestSubscriber<Object> ts1 = p.test();

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    ts1.cancel();
                }
            };

            final TestException ex = new TestException();

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    p.onError(ex);
                }
            };

            TestHelper.race(r1, r2, Schedulers.single());

            if (ts1.errorCount() != 0) {
                ts1.assertFailure(TestException.class);
            } else {
                ts1.assertEmpty();
            }
        }
    }

    @Test
    public void onNextCrossCancel() {
        AsyncProcessor<Object> p = AsyncProcessor.create();

        final TestSubscriber<Object> ts2 = new TestSubscriber<Object>();
        TestSubscriber<Object> ts1 = new TestSubscriber<Object>() {
            @Override
            public void onNext(Object t) {
                ts2.cancel();
                super.onNext(t);
            }
        };

        p.subscribe(ts1);
        p.subscribe(ts2);

        p.onNext(1);
        p.onComplete();

        ts1.assertResult(1);
        ts2.assertEmpty();
    }

    @Test
    public void onErrorCrossCancel() {
        AsyncProcessor<Object> p = AsyncProcessor.create();

        final TestSubscriber<Object> ts2 = new TestSubscriber<Object>();
        TestSubscriber<Object> ts1 = new TestSubscriber<Object>() {
            @Override
            public void onError(Throwable t) {
                ts2.cancel();
                super.onError(t);
            }
        };

        p.subscribe(ts1);
        p.subscribe(ts2);

        p.onError(new TestException());

        ts1.assertFailure(TestException.class);
        ts2.assertEmpty();
    }

    @Test
    public void onCompleteCrossCancel() {
        AsyncProcessor<Object> p = AsyncProcessor.create();

        final TestSubscriber<Object> ts2 = new TestSubscriber<Object>();
        TestSubscriber<Object> ts1 = new TestSubscriber<Object>() {
            @Override
            public void onComplete() {
                ts2.cancel();
                super.onComplete();
            }
        };

        p.subscribe(ts1);
        p.subscribe(ts2);

        p.onComplete();

        ts1.assertResult();
        ts2.assertEmpty();
    }
}

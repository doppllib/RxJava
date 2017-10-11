//
//  AppDelegate.m
//  iostest
//
//  Created by Kevin Galligan on 1/18/17.
//  Copyright © 2017 Kevin Galligan. All rights reserved.
//

#import "AppDelegate.h"
#import "CoTouchlabDopplTestingDopplJunitTestHelper.h"
#import "RxTestRunner.h"

@interface AppDelegate ()

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Override point for customization after application launch.
    
//    [RxTestRunner runResourceWithNSString:@"basetests.txt"];
    [RxTestRunner runSpecificWithNSString:@"io.reactivex.subscribers.TestSubscriberTest"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.subjects.ReplaySubjectBoundedConcurrencyTest#testConcurrentSizeAndHasAnyValueTimeBounded"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.internal.operators.flowable.FlowableReplayTest#testSynchronousDisconnect"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.internal.operators.flowable.FlowableReplayTest#testIssue2191_UnsubscribeSource"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.internal.operators.flowable.FlowableReplayTest#testIssue2191_SchedulerUnsubscribe"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.internal.operators.flowable.FlowableReplayTest"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.internal.operators.flowable.FlowableReplayTest#testBoundedReplayBuffer"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.internal.operators.flowable.FlowableReplayTest#testTimedAndSizedTruncation"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.internal.operators.flowable.FlowableReplayTest#testBackpressure"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.subjects.ReplaySubjectBoundedConcurrencyTest#testConcurrentSizeAndHasAnyValue"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.subjects.ReplaySubjectBoundedConcurrencyTest#testReplaySubjectEmissionSubscriptionRace"];
    
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.schedulers.ExecutorSchedulerTest#testRecursionAndOuterUnsubscribe"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.schedulers.ExecutorSchedulerTest#testSubscribeWithScheduler"];
//    [RxTestRunner runSpecificWithNSString:@"io.reactivex.schedulers.ExecutorSchedulerTest#runnableDisposedAsync"];
    
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}


@end

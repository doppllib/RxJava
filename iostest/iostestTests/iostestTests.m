//
//  iostestTests.m
//  iostestTests
//
//  Created by Kevin Galligan on 1/18/17.
//  Copyright © 2017 Kevin Galligan. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "OneTest.h"

@interface iostestTests : XCTestCase

@end

@implementation iostestTests

- (void)testExample {
    XCTAssertEqual([OneTest runDopplWithInt:100 withInt:0], 0, "Junit tests failed");
}

@end

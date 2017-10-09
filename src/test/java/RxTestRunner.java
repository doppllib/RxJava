import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import co.touchlab.doppl.testing.DopplJunitTestHelper;

/*-[
#import <mach/mach.h>
]-*/

/**
 * Created by kgalligan on 10/9/17.
 */

public class RxTestRunner
{
    public static int runSpecific(String cla)
    {
        return DopplJunitTestHelper.run(new String[]{cla}, new BigMemRunListener(), new BigMemDopplRunListener());
    }

    public static int runResource(String name)
    {
        return DopplJunitTestHelper.runResource(name, new BigMemRunListener(), new BigMemDopplRunListener());
    }

    static class BigMemDopplRunListener implements DopplJunitTestHelper.DopplJunitListener
    {
        long memSize;

        @Override
        public void startRun(String s)
        {
            memSize = printMem();
            System.out.println("\n\nTRACE RUN START **************** "+ s +" **************** \n\n" );
        }

        @Override
        public void endRun(String s)
        {
            long endSize = printMem();

            long megs = (long)Math.floor((double)(endSize-memSize)/(double)(1024*1024));
            if(Math.abs(megs) > 0)
            {
                System.out.println("\n\nTRACE RUN END (big: "+ megs +"m) **************** "+ s +" **************** \n\n" );
            }
            else
            {
                System.out.println("\n\nTRACE RUN END **************** "+ s +" **************** \n\n" );
            }
        }
    }

    static class BigMemRunListener extends RunListener
    {
        long memSize;

        @Override
        public void testStarted(Description description) throws Exception
        {
            super.testStarted(description);
            System.out.println("TRACE Starting "+ description.getClassName() + "-" + description.getMethodName() );
            memSize = printMem();
        }

        @Override
        public void testFinished(Description description) throws Exception
        {
            super.testFinished(description);
            System.out.println("TRACE Finished "+ description.getClassName() + "-" + description.getMethodName() );
            long endSize = printMem();

            long megs = (long)Math.floor((double)(endSize-memSize)/(double)(1024*1024));
            if(Math.abs(megs) > 0)
            {
                System.out.println(
                        "ZZZZ: " + description.getClassName() + "-" + description.getMethodName() + " diff: " +
                                megs +"m");
            }
        }
    }

    private static native long printMem()/*-[
    struct task_basic_info info;
  mach_msg_type_number_t size = sizeof(info);
  kern_return_t kerr = task_info(mach_task_self(),
                                 TASK_BASIC_INFO,
                                 (task_info_t)&info,
                                 &size);
  if( kerr == KERN_SUCCESS ) {
    return info.resident_size;
  } else {
    return 0;
  }
    ]-*/;
}

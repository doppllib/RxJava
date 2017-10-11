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

    private static native long moreMem()/*-[

    mach_port_t host_port;
    mach_msg_type_number_t host_size;
    vm_size_t pagesize;

    host_port = mach_host_self();
    host_size = sizeof(vm_statistics_data_t) / sizeof(integer_t);
    host_page_size(host_port, &pagesize);

    vm_statistics_data_t vm_stat;

    if (host_statistics(host_port, HOST_VM_INFO, (host_info_t)&vm_stat, &host_size) != KERN_SUCCESS) {
        NSLog(@"Failed to fetch vm statistics");
        return 0;
    }

    natural_t mem_used = (vm_stat.active_count +
            vm_stat.inactive_count +
            vm_stat.wire_count) * pagesize;

    return mem_used;
    ]-*/;

}


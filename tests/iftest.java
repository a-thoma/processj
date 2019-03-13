import java.util.*;
import processj.runtime.*;
import std.io;


/**
 * File generated by the ProcessJ JVM Compiler.
 * Package name 'tests'.
 * Code generation for 'iftest.pj'.
 * Target class 'iftest'.
 * Java code version '1.8.0_66'.
 *
 * @author ProcessJ Group - University of Nevada, Las Vegas
 * @since 1.2
 *
 */
public class iftest {
    public static class _proc$main$arT extends PJProcess {
        protected String[] _pd$args1;

        protected int _ld$a1;
        protected int _ld$i2;

        public _proc$main$arT(String[] _pd$args1) {
            this._pd$args1 = _pd$args1;
        }

        @Override
        public synchronized void run() {
            _ld$a1 = 1;
            if (_ld$a1 > 1) {
                if (_ld$a1 == 2) {
                    if ((_ld$a1 + 1) > 4) {
                        io.println("here");
                    } else {
                        if ((_ld$a1 + 1) >= 3) {
                            io.println("wtf");
                        }
                    }
                }
            } else {
                for (_ld$i2 = 0;
                     _ld$i2++ < 5;
                     /* empty */) {

                     io.println("Hello World!");
                }
            }
            terminate();
        }
    }

    public static void main(String[] _pd$args1) {
    	Scheduler scheduler = new Scheduler();
        PJProcess.scheduler = scheduler;
        (new iftest._proc$main$arT(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
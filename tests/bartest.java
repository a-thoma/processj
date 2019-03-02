import java.util.*;
import processj.runtime.*;
import std.*;


/**
 * File generated by the ProcessJ JVM Compiler.
 * Package name 'tests'.
 * Code generation for 'bartest.pj'.
 * Target class 'bartest'.
 * Java code version '1.8.0_66'.
 *
 * @author ProcessJ Group - University of Nevada, Las Vegas
 * @since 1.2
 *
 */
public class bartest {
    public static void _method$foo() {
        io.println("foo");
    }

    public static void _method$bar() {
        io.println("bar");
    }

    public static class _proc$main$arT extends PJProcess {
        protected String[] _pd$args1;

        protected PJBarrier _ld$b1;

        public _proc$main$arT(String[] _pd$args1) {
            this._pd$args1 = _pd$args1;
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                default: break;
            }

            _ld$b1 = new PJBarrier();
            final PJPar _ld$par1 = new PJPar(2, this);
            _ld$b1.enroll(2);

            new PJProcess() {
                @Override
                public synchronized void run() {
                    bartest._method$foo();
                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par1.decrement();
                    _ld$b1.resign();
                }
            }.schedule();

            new PJProcess() {
                @Override
                public synchronized void run() {
                    bartest._method$bar();
                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par1.decrement();
                    _ld$b1.resign();
                }
            }.schedule();

            setNotReady();
            this.runLabel = 1;
            yield();
            label(1);

            terminate();
        }
    }

    public static void main(String[] _pd$args1) {
    	Scheduler scheduler = new Scheduler();
        PJProcess.scheduler = scheduler;
        (new bartest._proc$main$arT(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
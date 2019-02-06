import java.util.*;
import processj.runtime.*;
import std.io;

/**
 * File generated by the ProcessJ JVM Compiler.
 * Package name 'tests'.
 * Code generation for 'Demo'.
 * Target class 'Demo'.
 * Java code version '1.8.0_66'.
 *
 * @author ProcessJ Group
 * @since 1.2
 *
 */
public class Demo {
    static void _method$say() {
        io.println("Hello from say");
    }

    public static class _proc$main extends PJProcess {
        private String[] _pd$args1;

        public _proc$main(String[] _pd$args1) {
            this._pd$args1 = _pd$args1;
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                case 2: resume(2); break;
                default: break;
            }

            final PJPar _ld$par1 = new PJPar(2, this);

            new PJProcess() {
                @Override
                public synchronized void run() {
                    io.println("from first par");
                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }.schedule();

            new PJProcess() {
                @Override
                public synchronized void run() {
                    io.println("par from first");
                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }.schedule();

            setNotReady();
            this.runLabel = 1;
            yield();
            label(1);
            final PJPar _ld$par2 = new PJPar(2, this);

            new PJProcess() {
                @Override
                public synchronized void run() {
                    io.println("from second par");
                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par2.decrement();
                }
            }.schedule();

            new PJProcess() {
                @Override
                public synchronized void run() {
                    io.println("par from second");
                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par2.decrement();
                }
            }.schedule();

            setNotReady();
            this.runLabel = 2;
            yield();
            label(2);
            terminate();
        }
    }

    public static void main(String[] _pd$args1) {
    	Scheduler scheduler = new Scheduler();
        PJProcess.scheduler = scheduler;
        (new Demo._proc$main(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
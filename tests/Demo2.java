import java.util.*;
import processj.runtime.*;
import std.io;

/**
 * File generated by the ProcessJ JVM Compiler.
 * Package name 'tests'.
 * Code generation for 'Demo2'.
 * Target class 'Demo2'.
 * Java code version '1.8.0_66'.
 *
 * @author ProcessJ Group
 * @since 1.2
 *
 */
public class Demo2 {
    public static class _proc$say extends PJProcess {
        public _proc$say() {
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                default: break;
            }

            final PJPar _ld$par1 = new PJPar(2, this);

            new PJProcess() {
                @Override
                public synchronized void run() {
                    io.println("from say");
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
                    io.println("say from");
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
            terminate();
        }
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
                default: break;
            }

            final PJPar _ld$par1 = new PJPar(3, this);

            new PJProcess() {
                @Override
                public synchronized void run() {
                    io.println("Hello");
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
                    io.println("World");
                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }.schedule();

            (new Demo2._proc$say() {
                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }).schedule();

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
        (new Demo2._proc$main(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
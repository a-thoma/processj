import java.util.*;
import processj.runtime.*;
import std.*;


/**
 * File generated by the ProcessJ JVM Compiler.
 * Package name 'tests'.
 * Code generation for 'Matt.pj'.
 * Target class 'Matt'.
 * Java code version '1.8.0_66'.
 *
 * @author ProcessJ Group - University of Nevada, Las Vegas
 * @since 1.2
 *
 */
public class Matt {
    // Temporary dirty fix for unreachable code due to infinite loop
    public static boolean isTrue() { return true; }

    public static void _method$foo1() {
        io.println("foo1 from Matt.pj");
    }

    public static class _proc$main$arT extends PJProcess {
        protected String[] _pd$args1;

        protected PJOne2OneChannel<Integer> _ld$c1;
        protected PJOne2OneChannel<Integer> _ld$d2;
        protected int _ld$x3;
        protected int _ld$temp04;
        protected int _ld$temp15;

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

            _ld$c1 = new PJOne2OneChannel<Integer>();
            _ld$d2 = new PJOne2OneChannel<Integer>();

            final PJPar _ld$par1 = new PJPar(3, this);

            new PJProcess() {
                @Override
                public synchronized void run() {
                    switch (this.runLabel) {
                        case 0: break;
                        case 1: resume(1); break;
                        case 2: resume(2); break;
                        case 3: resume(3); break;
                        case 4: resume(4); break;
                        default: break;
                    }

                    if (!_ld$d2.isReadyToRead(this)) {
                        this.runLabel = 1;
                        yield();
                    }

                    label(1);
                    _ld$temp04 = _ld$d2.read(this);
                    this.runLabel = 2;
                    yield();

                    label(2);

                    if (!_ld$c1.isReadyToRead(this)) {
                        this.runLabel = 3;
                        yield();
                    }

                    label(3);
                    _ld$temp15 = _ld$c1.read(this);
                    this.runLabel = 4;
                    yield();

                    label(4);
                    _ld$x3 = _ld$temp04 + _ld$temp15 + 3;
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
                    switch (this.runLabel) {
                        case 0: break;
                        case 1: resume(1); break;
                        default: break;
                    }

                    _ld$d2.write(this, 4);
                    this.runLabel = 1;
                    yield();
                    label(1);

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
                    switch (this.runLabel) {
                        case 0: break;
                        case 1: resume(1); break;
                        default: break;
                    }

                    _ld$c1.write(this, 7);
                    this.runLabel = 1;
                    yield();
                    label(1);

                    terminate();
                }

                @Override
                public void finalize() {
                    _ld$par1.decrement();
                } 
            }.schedule();

            if (_ld$par1.shouldYield()) {
                this.runLabel = 1;
                yield();
                label(1);
            }

            io.println(_ld$x3);
            Matt._method$foo1();
            terminate();
        }
    }

    public static void main(String[] _pd$args1) {
    	Scheduler scheduler = new Scheduler();
        PJProcess.scheduler = scheduler;
        (new Matt._proc$main$arT(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
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
    public static class _proc$writer$chanwriteI extends PJProcess {
        protected PJOne2OneChannel<Integer> _pd$out1;

        public _proc$writer$chanwriteI(PJOne2OneChannel<Integer> _pd$out1) {
            this._pd$out1 = _pd$out1;
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                default: break;
            }

            _pd$out1.write(this, 42);
            this.runLabel = 1;
            yield();
            label(1);

            terminate();
        }
    }


    public static class _proc$reader$chanreadI extends PJProcess {
        protected PJOne2OneChannel<Integer> _pd$in1;

        protected int _ld$value1;

        public _proc$reader$chanreadI(PJOne2OneChannel<Integer> _pd$in1) {
            this._pd$in1 = _pd$in1;
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                case 2: resume(2); break;
                default: break;
            }

            if (!_pd$in1.isReadyToRead(this)) {
                this.runLabel = 1;
                yield();
            }

            label(1);
            _ld$value1 = _pd$in1.read(this);
            this.runLabel = 2;
            yield();

            label(2);

            io.println("The value is " + _ld$value1);
            terminate();
        }
    }


    public static class _proc$main$arrT extends PJProcess {
        protected String[] _pd$args1;

        protected PJOne2OneChannel<Integer> _ld$c1;
        protected int _ld$a2;

        public _proc$main$arrT(String[] _pd$args1) {
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
            _ld$a2 = 2;
            final PJPar _ld$par1 = new PJPar(2, this);

            (new Demo._proc$writer$chanwriteI(_ld$c1) {
                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }).schedule();

            (new Demo._proc$reader$chanreadI(_ld$c1) {
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
        (new Demo._proc$main$arrT(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
import java.util.*;
import processj.runtime.*;

/**
 * File generated by the ProcessJ JVM Compiler.
 * Package name 'tests'.
 * Code generation for 'simple.pj'.
 * Target class 'simple'.
 * Java code version '1.8.0_66'.
 *
 * @author ProcessJ Group - University of Nevada, Las Vegas
 * @since 1.2
 *
 */
public class simple {
    // Temporary dirty fix for unreachable code due to infinite loop
    public static boolean isTrue() { return true; }

    public static class _proc$foo$crI extends PJProcess {
        protected PJChannel<Integer> _pd$r1;

        protected String _ld$str1;
        protected int _ld$a2;

        public _proc$foo$crI(PJChannel<Integer> _pd$r1) {
            this._pd$r1 = _pd$r1;
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                case 2: resume(2); break;
                default: break;
            }

            _ld$str1 = "...";

            if (!_pd$r1.isReadyToRead(this)) {
                this.runLabel = 1;
                yield();
            }

            label(1);
            _ld$a2 = _pd$r1.read(this);
            this.runLabel = 2;
            yield();

            label(2);
            terminate();
        }
    }

    public static class _proc$main$arT extends PJProcess {
        protected String[] _pd$args1;

        protected PJChannel<Integer> _ld$c1;

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
            final PJPar _ld$par1 = new PJPar(1, this);

            (new simple._proc$foo$crI(_ld$c1) {
                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }).schedule();

            if (_ld$par1.shouldYield()) {
                this.runLabel = 1;
                yield();
                label(1);
            }

            terminate();
        }
    }

    public static void main(String[] _pd$args1) {
    	Scheduler scheduler = new Scheduler();
        PJProcess.scheduler = scheduler;
        (new simple._proc$main$arT(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
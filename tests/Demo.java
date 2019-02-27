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
    public static class PP {
        protected static class request extends PJProtocolCase {
            public int number;
            public double amount;

            public request(int number, double amount) {
                this.number = number;
                this.amount = amount;
                this.tag = "request";
            }
        }

        protected static class reply extends PJProtocolCase {
            public boolean status;

            public reply(boolean status) {
                this.status = status;
                this.tag = "reply";
            }
        }
    }

    public static class P1 {
        protected static class deny extends PJProtocolCase {
            public int code;

            public deny(int code) {
                this.code = code;
                this.tag = "deny";
            }
        }
    }

    public static class XX {
        protected static class accept extends PJProtocolCase {
            public int code;

            public accept(int code) {
                this.code = code;
                this.tag = "accept";
            }
        }
    }

    public static class _proc$readXX$crLXX extends PJProcess {
        protected PJOne2OneChannel<PJProtocolCase> _pd$in1;

        protected PJProtocolCase _ld$value1;

        public _proc$readXX$crLXX(PJOne2OneChannel<PJProtocolCase> _pd$in1) {
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

            io.println("Done reading xx!");
            terminate();
        }
    }


    public static class _proc$writeXX$cwLXX extends PJProcess {
        protected PJOne2OneChannel<PJProtocolCase> _pd$out1;

        protected PJProtocolCase _ld$xx1;

        public _proc$writeXX$cwLXX(PJOne2OneChannel<PJProtocolCase> _pd$out1) {
            this._pd$out1 = _pd$out1;
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                default: break;
            }

            _ld$xx1 = new XX.accept(35);
            _pd$out1.write(this, ((PJProtocolCase) (_ld$xx1)));
            this.runLabel = 1;
            yield();
            label(1);

            terminate();
        }
    }


    public static class _proc$main$arT extends PJProcess {
        protected String[] _pd$args1;

        protected PJOne2OneChannel<PJProtocolCase> _ld$x1;
        protected int _ld$a2;

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

            _ld$x1 = new PJOne2OneChannel<PJProtocolCase>();
            _ld$a2 = 2;
            final PJPar _ld$par1 = new PJPar(2, this);

            (new Demo._proc$writeXX$cwLXX(_ld$x1) {
                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }).schedule();

            (new Demo._proc$readXX$crLXX(_ld$x1) {
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
        (new Demo._proc$main$arT(_pd$args1)).schedule();
        PJProcess.scheduler.start();
    }
}
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
    public static class T implements PJRecord {
        public int a;

        public T(int a) {
            this.a = a;
        }
    }

    public static class K implements PJRecord {
        public int z;
        public T t;

        public K(int z, T t) {
            this.z = z;
            this.t = t;
        }
    }

    protected static class X implements PJRecord {
        public int a;
        public int p;
        public String b;

        public X(int a, int p, String b) {
            this.a = a;
            this.p = p;
            this.b = b;
        }
    }

    protected static class P implements PJRecord {
        public int z;
        public T t;
        public int a;
        public int p;
        public String b;
        public int x;
        public int y;

        public P(int z, T t, int a, int p, String b, int x, int y) {
            this.z = z;
            this.t = t;
            this.a = a;
            this.p = p;
            this.b = b;
            this.x = x;
            this.y = y;
        }
    }

    private static class L implements PJRecord {
        public K k;
        public String str;

        public L(K k, String str) {
            this.k = k;
            this.str = str;
        }
    }

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

    public static class _proc$writer$cwLL extends PJProcess {
        protected PJOne2OneChannel<L> _pd$out1;

        protected K _ld$k1;
        protected X _ld$x2;
        protected L _ld$l3;

        public _proc$writer$cwLL(PJOne2OneChannel<L> _pd$out1) {
            this._pd$out1 = _pd$out1;
        }

        @Override
        public synchronized void run() {
            switch (this.runLabel) {
                case 0: break;
                case 1: resume(1); break;
                default: break;
            }

            _ld$k1 = new K(3, new T(45));
            _ld$x2 = new X(20, 300, "Ben");
            _ld$l3 = new L(_ld$k1, "Benjamin");
            _pd$out1.write(this, ((L) (_ld$l3)));
            this.runLabel = 1;
            yield();
            label(1);

            terminate();
        }
    }


    public static class _proc$reader$crLL extends PJProcess {
        protected PJOne2OneChannel<L> _pd$in1;

        protected L _ld$value1;

        public _proc$reader$crLL(PJOne2OneChannel<L> _pd$in1) {
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

            switch(_ld$value1.k.t.a) {
            case 4:
                io.println("case 4");
                break;
            case 5:
                io.println("case 5");
                break;
            case 45:
                io.println("case 45");
                break;
            case 6:
                io.println("case 6");
                break;
            default:
                io.println("some case!");
                break;
            }
            io.println("The value is " + _ld$value1.k.t.a);
            terminate();
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

        protected PJOne2OneChannel<L> _ld$c1;
        protected PJOne2OneChannel<PJProtocolCase> _ld$x2;
        protected int _ld$a3;

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

            _ld$c1 = new PJOne2OneChannel<L>();
            _ld$x2 = new PJOne2OneChannel<PJProtocolCase>();
            _ld$a3 = 2;
            final PJPar _ld$par1 = new PJPar(4, this);

            (new Demo._proc$writer$cwLL(_ld$c1) {
                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }).schedule();

            (new Demo._proc$reader$crLL(_ld$c1) {
                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }).schedule();

            (new Demo._proc$writeXX$cwLXX(_ld$x2) {
                @Override
                public void finalize() {
                    _ld$par1.decrement();
                }
            }).schedule();

            (new Demo._proc$readXX$crLXX(_ld$x2) {
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
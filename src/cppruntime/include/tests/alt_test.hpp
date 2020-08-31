#ifndef ALT_TEST_HPP
#define ALT_TEST_HPP

namespace pj_tests
{
    template <typename T>
    class alt_writer : public pj_runtime::pj_process
    {
    public:
        alt_writer() = delete;

        alt_writer(int32_t                              id,
                   pj_runtime::pj_scheduler*         sched,
                   pj_runtime::pj_one2one_channel<T>* chan,
                   T                                  data)
        : id(id), data(data)
        {
            this->sched = sched;
            this->chan = chan;
        }

        virtual ~alt_writer() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto LEND; break;
            }
        LEND:
            std::cout << "process " << this->id
                      << " writing data " << data << "...\n";
            this->chan->write(this, this->data);
            std::cout << "process " << this->id << " wrote data "
                      << data << std::endl;
            std::cout << "END (proc " << id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t                              id;
        pj_runtime::pj_scheduler*         sched;
        T                                  data;
        pj_runtime::pj_one2one_channel<T>* chan;
    };

    template <typename T>
    class alt_process : public pj_runtime::pj_process
    {
    public:
        alt_process() = delete;

        alt_process(uint32_t                             id,
                    pj_runtime::pj_scheduler*         sched,
                    pj_runtime::pj_one2one_channel<T>* chan)
        : id(id)
        {
            this->sched = sched;
            this->chan = chan;
        }

        virtual ~alt_process() = default;

        /* TODO: actually finish the alt -- switch case with read that will set
         * the writer ready instead of deadlocking :^)
         * ---
         * also move the statics in this test to protected instead, and give them
         * automatic duration -- see if this is better/easier (it's what Hello.pj
         * does)
         */
        void run()
        {
            static std::vector<pj_runtime::pj_alt_guard_type> guards;
            static std::vector<bool> b_guards;
            static int32_t enable_result;
            static int32_t disable_result;
            static bool ready;
            static pj_runtime::pj_alt alt(2, this);
            switch(this->get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "Hello from L0! (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";
            guards.push_back(chan);
            guards.push_back("skip");
            b_guards.push_back(true);
            b_guards.push_back(true);
            ready = alt.set_guards(b_guards, guards);

            if(!ready) {
                std::cout << "RuntimeError: One of the boolean pre-guards must be true!" << std::endl;
                abort();
            }

            this->set_not_ready();
            enable_result = alt.enable();
            std::cout << "process " << this->id << ": enable_result is " 
                      << enable_result << std::endl;
            /* NOTE: this condition optimizes the alt's performance
             * ---
             * if we simply yield on every enable we lose time because if we
             * end up yielding when we actually _have_ a guard that's ready
             * according to the enable phase, then the process with the alt
             * takes a completely unnecessary trip through the run queue
             */
            if(enable_result == -1)
            {
                set_label(1);
                return;
            }
        LEND:
            std::cout << "process " << this->id << ": again, enable_result is "
                      << enable_result << std::endl;
            disable_result = alt.disable(enable_result);
            std::cout << "process " << this->id << ": disable_result is "
                      << disable_result << std::endl;
            switch(disable_result)
            {
                case 0:
                {
                    /* generated code because choice 0 is a channel */
                    T x = chan->read(this);
                    /* then we do something afterwards (the code after the guard) */
                    std::cout << "process " << this->id << ": doing something with variable "
                              << x << std::endl;
                    break;
                    /* NOTE: below is probably just debug -- maybe leave in in
                     * and fix it when a proper log system is implemented
                     */
                }
                default:
                {
                    // std::cout << "process " << this->id 
                    //           << ": FATAL ERROR IN ALT: BAD disable_result FROM DISABLE()\n";
                    // abort();
                }
            }
            std::cout << "END (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, alt_process& p)
        {
            return o << p.id;
        }

    private:
        uint32_t id;
        pj_runtime::pj_scheduler*         sched;
        pj_runtime::pj_one2one_channel<T>* chan;
    };

    class alt_timeout_process : public pj_runtime::pj_process
    {
    public:
        alt_timeout_process() = delete;

        alt_timeout_process(uint32_t id, pj_runtime::pj_scheduler* sched)
        : id(id),
          sched(sched)
        {

        }

        virtual ~alt_timeout_process() = default;

        void run()
        {
            // static pj_runtime::pj_alt alt(2, this);
            static pj_runtime::pj_alt alt(1, this);
            static std::vector<pj_runtime::pj_alt_guard_type> guards;
            static std::vector<bool> b_guards;
            static int32_t enable_result;
            static int32_t disable_result;
            static pj_runtime::pj_timer* timer = new pj_runtime::pj_timer(this, 0);
            switch(this->get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "Hello from L0! (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";
            /* TODO: alt constructed here, rest of code to follow */
            timer->timeout(3000);
            timer->start();
            sched->insert(timer);
            guards.push_back(timer);
            // guards.push_back("skip");
            b_guards.push_back(true);
            // b_guards.push_back(true);
            this->set_not_ready();
            alt.set_guards(b_guards, guards);
            enable_result = alt.enable();
            std::cout << "process " << this->id << ": enable_result is "
                      << enable_result << std::endl;
            /* NOTE: this condition optimizes the alt's performance
             * ---
             * if we simply yield on every enable we lose time because if we
             * end up yielding when we actually _have_ a guard that's ready
             * according to the enable phase, then the process with the alt
             * takes a completely unnecessary trip through the run queue
             */
            if(enable_result == -1)
            {
                set_label(1);
                return;
            }
        LEND:
            std::cout << "process " << this->id << ": again, enable_result is "
                      << enable_result << std::endl;
            disable_result = alt.disable(enable_result);
            std::cout << "process " << this->id << ": disable_result is " 
                      << disable_result << std::endl;
            switch(disable_result)
            {
                case 0:
                {
                    /* TODO: handling for a timeout -- read? */
                    std::cout << "process " << this->id << ": alt choice was a timer\n";
                    /* NOTE: guarded code would be below */
                    break;
                }
                /* NOTE: below is probably just debug -- maybe leave in in
                 * and fix it when a proper log system is implemented
                 */
                default:
                {
                    // std::cout << "process " << this->id
                    //           << ": FATAL ERROR IN ALT: BAD disable_result FROM DISABLE()\n";
                    // abort();
                }
            }
            std::cout << "END (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";

            delete timer;
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, alt_timeout_process& p)
        {
            return o << p.id;
        }

    private:
        uint32_t id;
        pj_runtime::pj_scheduler* sched;
    };

    class alt_skip_process : public pj_runtime::pj_process
    {
    public:
        alt_skip_process() = delete;

        alt_skip_process(uint32_t                     id,
                         pj_runtime::pj_scheduler* sched)
        : id(id)
        {
            this->sched = sched;
        }

        virtual ~alt_skip_process() = default;

        void run()
        {
            static pj_runtime::pj_alt alt(1, this);
            static std::vector<pj_runtime::pj_alt_guard_type> guards;
            static std::vector<bool> b_guards;
            static int32_t enable_result;
            static int32_t disable_result;
            static bool ready;
            switch(this->get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "Hello from L0! (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";
            /* TODO: alt constructed here, rest of code to follow */
            guards.push_back("skip");
            b_guards.push_back(true);
            this->set_not_ready();
            ready = alt.set_guards(b_guards, guards);

            if(!ready)
            {
                std::cout << "RuntimeError: One of the boolean pre-guards must be true!" << std::endl;
                abort();
            }

            this->set_not_ready();
            enable_result = alt.enable();
            std::cout << "process " << this->id << ": enable_result is "
                      << enable_result << std::endl;
            /* NOTE: this condition optimizes the alt's performance
             * ---
             * if we simply yield on every enable we lose time because if we
             * end up yielding when we actually _have_ a guard that's ready
             * according to the enable phase, then the process with the alt
             * takes a completely unnecessary trip through the run queue
             */
            if(enable_result == -1)
            {
                set_label(1);
                return;
            }
        LEND:
            std::cout << "process " << this->id << ": again, enable_result is "
                      << enable_result << std::endl;
            disable_result = alt.disable(enable_result);
            std::cout << "process " << this->id << ": disable_result is " 
                      << disable_result << std::endl;
            switch(disable_result)
            {
                case 0:
                {
                    /* TODO: handling for a skip */
                    std::cout << "process " << this->id << ": alt choice was a skip\n";
                    /* NOTE: guarded code would be below */
                    break;
                }
                /* NOTE: below is probably just debug -- maybe leave in in
                 * and fix it when a proper log system is implemented
                 */
                default:
                {
                    // std::cout << "process " << this->id
                    //           << ": FATAL ERROR IN ALT: BAD disable_result FROM DISABLE()\n";
                    // abort();
                }
            }
            std::cout << "END (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, alt_skip_process& p)
        {
            return o << p.id;
        }

    private:
        uint32_t id;
        pj_runtime::pj_scheduler* sched;
    };

    class alt_test
    {
    public:
        alt_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sched;

            std::cout << "\n *** CREATING CHANNELS FOR ALT GUARDS *** \n\n";
            pj_runtime::pj_one2one_channel<uint32_t>* chan1 =
                new pj_runtime::pj_one2one_channel<uint32_t>();
            pj_runtime::pj_one2one_channel<uint32_t>* chan2 =
                new pj_runtime::pj_one2one_channel<uint32_t>();

            std::cout << "\n *** CREATING PROCESS FOR ALT *** \n\n";
            alt_process<uint32_t>* a_process = new alt_process<uint32_t>(0, &sched, chan1);

            std::cout << "\n *** CREATING PROCESS FOR ALT ON CHANNEL READ *** \n\n";
            alt_writer<uint32_t>* process = new alt_writer<uint32_t>(1, &sched, chan2, 1);

            std::cout << "\n *** CREATING PROCESS FOR ALT ON TIMEOUT *** \n\n";
            // alt_timeout_process* at_process = new alt_timeout_process(2, timer);
            alt_timeout_process* at_process = new alt_timeout_process(2, &sched);

            std::cout << "\n *** CREATING PROCESS FOR ALT ON SKIP *** \n\n";
            alt_skip_process* as_process = new alt_skip_process(3, &sched);

            std::cout << "\n *** SCHEDULING PROCESSES *** \n\n";
            // sched.insert(a_process);
            // sched.insert(process);
            sched.insert(at_process);
            // sched.insert(as_process);

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sched.start();

            std::cout << "\n *** FREEING MEMORY *** \n\n";
            delete chan2;
            delete chan1;
        }
    };
}

#endif
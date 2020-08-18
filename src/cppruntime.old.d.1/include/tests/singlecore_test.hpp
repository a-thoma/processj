#ifndef SINGLECORE_TEST_HPP
#define SINGLECORE_TEST_HPP

namespace pj_tests
{
    class sc_process : public pj_runtime::pj_process
    {
    public:
        sc_process() = delete;
        sc_process(int32_t id, pj_runtime::pj_scheduler* sched)
        : id(id)
        {
            this->sched = sched;
        }
        virtual ~sc_process() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            {
                std::cout << "Hello from L0! (proc " << id << " on cpu "
                          << sched_getcpu() << ")\n";
                /* this is the code that should be generated by a timeout
                 */
                std::chrono::system_clock::time_point tp =
                    std::chrono::system_clock::now() + std::chrono::seconds(3);
                pj_runtime::pj_timer* timer = new pj_runtime::pj_timer(this, tp);
                this->sched->insert(timer);
                this->set_not_ready();
                set_label(1);
                return;
            }
        LEND:
            {
                std::cout << "END (proc " << id << " on cpu "
                          << sched_getcpu() << ")\n";
                terminate();
                return;
            }
        }

        friend std::ostream& operator<<(std::ostream& o, sc_process& p)
        {
            o << p.id;
            return o;
        }
    private:
        int32_t id;
        pj_runtime::pj_scheduler* sched;
    };

    class singlecore_test
    {
    public:
        singlecore_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sched;

            std::cout << "\n *** CREATING PROCESSES *** \n\n";
            pj_tests::sc_process* processes[4];
            uint32_t i;
            for(i = 0; i < 4; ++i)
            {
                processes[i] = new pj_tests::sc_process(i, &sched);
            }

            std::cout << "\n *** SCHEDULING PROCESSES *** \n\n";
            for(i = 0; i < 4; ++i)
            {
                sched.insert(processes[i]);
            }

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sched.start();
        }
    };
}

#endif
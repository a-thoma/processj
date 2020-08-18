#ifndef PJ_ANONPROC_TEST_HPP
#define PJ_ANONPROC_TEST_HPP

namespace pj_tests
{
    class anonproc_process : public pj_runtime::pj_process
    {
    public:
        anonproc_process() = delete;
        anonproc_process(int32_t id, pj_runtime::pj_scheduler* sched)
        : id(id)
        {
            this->sched = sched;
        }

        virtual ~anonproc_process() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto ANONPROCL0;   break;
                case 1: goto ANONPROCLEND; break;
            }
        ANONPROCL0:
            std::cout << "Hello from L0! (proc " << id << " on cpu "
                      << sched_getcpu() << ")\n";
            std::cout << "Inserting our anon process into the scheduler...\n";
            /* this would be the code generated for an anonymous process
             * in the main process
             */
            class anon : public pj_runtime::pj_process
            {
            public:
                anon() = delete;
                anon(int32_t id, pj_runtime::pj_scheduler* sched)
                : id(id)
                {
                    this->sched = sched;
                }

                virtual ~anon() = default;

                void run()
                {
                    switch(get_label())
                    {
                        case 0: goto ANONL0;   break;
                        case 1: goto ANONLEND; break;
                    }
                ANONL0:
                    std::cout << "Hello from L0! (proc " << id << " on cpu "
                              << sched_getcpu() << ")\n";
                    set_label(1);
                    return;

                ANONLEND:
                    std::cout << "END (proc " << id << " on cpu "
                              << sched_getcpu() << ")\n";
                    terminate();
                    return;
                }

                /* NOTE: local classes cannot have friend functions defined */

            private:
                int32_t id;
                pj_runtime::pj_scheduler* sched;
            };

            this->sched->insert(new anon(this->id + 1, this->sched));
            std::cout << "anon process inserted!\n";
            set_label(1);
            return;

        ANONPROCLEND:
            std::cout << "END (proc " << id << " on cpu "
                      << sched_getcpu() << ")\n";
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, anonproc_process& p)
        {
            o << p.id;
            return o;
        }

    private:
        int32_t id;
        pj_runtime::pj_scheduler* sched;
    };

    class anonproc_test
    {
    public:
        anonproc_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sched;

            std::cout << "\n *** CREATING PROCESS *** \n\n";
            pj_tests::anonproc_process* ap = new pj_tests::anonproc_process(0, &sched);

            std::cout << "\n *** SCHEDULING PROCESS *** \n\n";
            sched.insert(ap);

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sched.start();
        }
    };
}

#endif
#ifndef PJ_PAR_TEST_HPP
#define PJ_PAR_TEST_HPP

namespace pj_tests
{
    class par_process : public pj_runtime::pj_process
    {
    public:
        par_process() = delete;

        par_process(int32_t id, pj_runtime::pj_scheduler* sched)
        : id(id)
        {
            this->sched = sched;
        }

        virtual ~par_process() = default;

        void run()
        {
            static pj_runtime::pj_par* par = new pj_runtime::pj_par(2, this);

            switch(get_label())
            {
                case 0: goto PAR_PROCESSL0; break;
                case 1: goto PAR_PROCESSL1; break;
            }

        PAR_PROCESSL0:
            std::cout << "Hello from L0! (process " << this->id << " on cpu "
                      << sched_getcpu() << ")\n";

            /* now we inline-define our other processes */
            class anon : public pj_runtime::pj_process
            {
            public:
                anon() = delete;
                anon(int32_t id,
                     pj_runtime::pj_scheduler* sched,
                     pj_runtime::pj_par* par)
                : id(id)
                {
                    this->sched = sched;
                    this->par   = par;
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
                    /* NOTE: this might be in a method called finalize() that calls
                     * a lambda we pass to this function that captures the par from
                     * the process that "owns" the par, just so that it's easier
                     * to generalize these anonymous functions within the pars.
                     * or maybe it'll be easier... not sure yet.
                     */
                    this->par->decrement();
                    terminate();
                    return;
                }

                /* NOTE: local classes cannot have friend functions defined */

            private:
                int32_t id;
                pj_runtime::pj_scheduler* sched;
                pj_runtime::pj_par* par;
            };

            /* plunk 2 new anon procs into the scheduler */
            this->sched->insert(new anon(this->id + 1, this->sched, par));
            this->sched->insert(new anon(this->id + 2, this->sched, par));

            /* more code generated for our par block below */
            if(par->should_yield())
            {
                this->set_label(1);
                return;
            }

        PAR_PROCESSL1:
            std::cout << "END (proc " << this->id << " on cpu "
                      << sched_getcpu() << ")\n";
            delete par;
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, par_process& p)
        {
            o << p.id;
            return o;
        }

    private:
        int32_t id;
        pj_runtime::pj_scheduler* sched;
    };

    class par_test
    {
    public:
        par_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sched;

            std::cout << "\n *** CREATING PROCESS *** \n\n";
            pj_tests::par_process* pp = new pj_tests::par_process(0, &sched);

            std::cout << "\n *** SCHEDULING PROCESS *** \n\n";
            sched.insert(pp);

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sched.start();
        }
    };
}

#endif
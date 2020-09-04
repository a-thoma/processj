#ifndef BARRIER_TEST_HPP
#define BARRIER_TEST_HPP

namespace pj_tests
{
    class b_process : public pj_runtime::pj_process
    {
    public:
        b_process() = delete;

        b_process(uint32_t id, std::shared_ptr<pj_runtime::pj_barrier> b)
        : id(id)
        {
            this->barrier = b;
        }

        virtual ~b_process() = default;

        void run()
        {
            switch(this->get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "Hello from L0! (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";
            /* NOTE: this is the code generated for a barrier sync */
            barrier->sync(this);
            set_label(1);
            return;
        LEND:
            std::cout << "END (process " << this->id
                      << " on cpu " << sched_getcpu() << ")\n";
            terminate();
            return;
        }

        friend std::ostream& operator<<(std::ostream& o, b_process& p)
        {
            return o << p.id;
        }

    private:
        uint32_t id;
        std::shared_ptr<pj_runtime::pj_barrier> barrier;
    };

    class barrier_test
    {
    public:
        barrier_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sched;

            std::cout << "\n *** CREATING BARRIER *** \n\n";
            std::shared_ptr<pj_runtime::pj_barrier> bar =
                std::make_shared<pj_runtime::pj_barrier>();
            bar->enroll(4);

            std::cout << "\n *** CREATING PROCESSES *** \n\n";
            b_process* processes[4];
            uint32_t i;
            for(i = 0; i < 4; ++i)
            {
                processes[i] = new b_process(i, bar);
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
#ifndef MULTICORE_TEST_HPP
#define MULTICORE_TEST_HPP

/* TODO:
 * ---
 * this test doesn't quite work, as we need to isolate the threads earlier
 * in the multicore scheduler. see pj_process_tests.cpp for more details
 */

namespace pj_tests
{
	class mc_process : public pj_runtime::pj_process
    {
    public:
        mc_process() = delete;
        mc_process(int32_t id, pj_runtime::pj_scheduler* sched)
        : id(id)
        {
            this->sched = sched;
        }
        virtual ~mc_process() = default;

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
                // std::chrono::system_clock::time_point tp =
                //     std::chrono::system_clock::now() + std::chrono::seconds(3);
                pj_runtime::pj_timer* timer = new pj_runtime::pj_timer(this, 3);
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

        friend std::ostream& operator<<(std::ostream& o, mc_process& p)
        {
            o << p.id;
            return o;
        }
    private:
        int32_t id;
        pj_runtime::pj_scheduler* sched;
    };

	class multicore_test
	{
	public:
		multicore_test()
		{
			std::cout << "instantiating test...\n";
		}

		void run()
		{
			std::cout << "\n *** CREATING SCHEDULER *** \n\n";
	        std::vector<pj_runtime::pj_scheduler> schedulers(4);

	        std::cout << "\n *** CREATING PROCESSES *** \n\n";
	        pj_tests::mc_process* processes[4];
	        int32_t i;
	        for(i = 0; i < 4; ++i)
	        {
	            processes[i] = new pj_tests::mc_process(i, &schedulers[i]);
	        }

	        std::cout << "\n *** SCHEDULING PROCESS *** \n\n";
	        for(i = 0; i < 4; ++i)
	        {
	            schedulers[i].insert(processes[i]);
	        }

	        std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            for(i = 0; i < 4; ++i)
	        {
                schedulers[i].start();
            }
		}
	};
}

#endif
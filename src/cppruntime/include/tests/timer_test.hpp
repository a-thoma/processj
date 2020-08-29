#ifndef TIMER_TEST_HPP
#define TIMER_TEST_HPP

namespace pj_tests
{
	class timer_process : public pj_runtime::pj_process
	{
	public:
		timer_process(pj_runtime::pj_scheduler* sched)
		: m_sched(sched)
		{ }

		~timer_process() = default;

		void run()
		{
			std::cout << "timer_process run: new timer\n";
	        static pj_runtime::pj_timer* t = new pj_runtime::pj_timer(this); // TODO: change runtime to add THIS to arg list
	        // t->timeout(3);                                                // to the non-null initializer for a timer

	        switch(get_label())
	        {
	        	case 0: goto L0; break;
	        	case 1: goto L1; break;
	        }

	        L0:
	        std::cout << "timer_process run: start timer\n";
	        t->start();
	        m_sched->insert(t);
	        this->set_not_ready();
	        this->set_label(1);
	        return;

	        L1:
	        std::cout << "timer_process run: delete timer\n";
	        delete t;
	        terminate();
	        return;
		}

	private:
		pj_runtime::pj_scheduler* m_sched;
	};

	class timer_test
	{
	public:
		timer_test()
		{
			std::cout << "instantiating test...\n";
		}

		void run()
		{
			pj_runtime::pj_scheduler sched;
	        sched.insert(new timer_process(&sched));
	        sched.start();
	    }
	};
}

#endif
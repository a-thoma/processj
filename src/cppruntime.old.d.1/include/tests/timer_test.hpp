#ifndef TIMER_TEST_HPP
#define TIMER_TEST_HPP

namespace pj_tests
{
	class timer_test
	{
	public:
		timer_test()
		{
			std::cout << "instantiating test...\n";
		}

		void run()
		{
	        std::cout << "\n *** CREATING ONE2ONE CHANNEL<int32_t> *** \n\n";
	        pj_runtime::pj_one2one_channel<int32_t> oto_ch;

	        std::cout << "\n *** CREATING PROCESSES FOR R/W *** \n\n";
	        one2one_reader<int32_t>* oto_r = new one2one_reader<int32_t>(0, &oto_ch);
	        one2one_writer<int32_t>* oto_w = new one2one_writer<int32_t>(1,
	                                                                     &oto_ch,
	                                                                     69);

	        std::cout << "\n *** CREATING SCHEDULER *** \n\n";
	        pj_runtime::pj_scheduler sch;

	        std::cout << "\n *** SETTING PROCESSES NOT READY *** \n\n";
	        oto_w->set_not_ready();

	        std::cout << "\n *** PLACING TIMER IN TIMER QUEUE *** \n\n";
	        std::chrono::system_clock::time_point tp =
	            std::chrono::system_clock::now() + std::chrono::seconds(3);
	        pj_runtime::pj_timer* t = new pj_runtime::pj_timer(oto_w, tp);
	        sch.insert(t);

	        std::cout << "\n *** SCHEDULING PROCESSES *** \n\n";
	        sch.insert(oto_w);
	        sch.insert(oto_r);

	        std::cout << "\n *** STARTING SCHEDULER *** \n\n";
	        sch.start();

	        std::cout << "\n *** END OF RUNTIME *** \n\n";
	    }
	};
}

#endif
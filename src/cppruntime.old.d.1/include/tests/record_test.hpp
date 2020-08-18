#ifndef RECORD_TEST_HPP
#define RECORD_TEST_HPP

namespace pj_tests
{

    struct some_record : public pj_runtime::pj_record
    {
        int   a;
        bool  b;
        float y;
    };

    class record_process : public pj_runtime::pj_process
    {
    public:

        record_process() = delete;

        record_process(int32_t id)
        : id(id)
        {

        }

        virtual ~record_process() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id
                      << " manipulating a record...\n";
            rt.a = 13;
            rt.b = true;
            rt.y = 5.3;
            std::cout << std::boolalpha
                      << "contents of record:"
                      << "\nrt.a = " << rt.a
                      << "\nrt.b = " << rt.b
                      << "\nrt.y = " << rt.y
                      << std::endl;
        LEND:
            std::cout << "END (proc " << this->id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t id;
        struct some_record rt;
    };

    class record_test
    {
    public:
        record_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sched;

            std::cout << "\n *** CREATING PROCESS *** \n\n";
            auto* r_proc = new record_process(0);

            std::cout << "\n *** SCHEDULING PROCESS *** \n\n";
            sched.insert(r_proc);

            std::cout << "\n *** RUNNING SCHEDULER *** \n\n";
            sched.start();
        }
    };
}

#endif
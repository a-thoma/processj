#ifndef ONE2ONE_TEST_HPP
#define ONE2ONE_TEST_HPP

namespace pj_tests
{
	template <typename T>
    class one2one_writer : public pj_runtime::pj_process
    {
    public:
        one2one_writer() = delete;

        one2one_writer(int32_t                              id,
                       pj_runtime::pj_one2one_channel<T>* chan,
                       T                                  data)
        : id(id), data(data)
        {
            this->chan = chan;
        }

        virtual ~one2one_writer() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id
                      << " writing data " << data << "...\n";
            this->chan->write(this, this->data);
            std::cout << "process " << this->id << " wrote data "
                      << data << std::endl;
            set_label(1);
            return;
        LEND:
            std::cout << "END (proc " << id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t                              id;
        T                                  data;
        pj_runtime::pj_one2one_channel<T>* chan;
    };

    template <typename T>
    class one2one_reader : public pj_runtime::pj_process
    {
    public:
        one2one_reader() = delete;

        one2one_reader(int32_t                              id,
                       pj_runtime::pj_one2one_channel<T>* chan)
        : id(id)
        {
            this->chan = chan;
        }

        virtual ~one2one_reader() = default;

        void run()
        {
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto L1;   break;
                case 2: goto LEND; break;
            }
        L0:
            std::cout << "process " << this->id << " reading from channel\n";
            if(!this->chan->is_ready_to_read(this))
            {
                std::cout << "process " << this->id << "'s channel is not ready\n";
                set_label(1);
                return;
            }
            else
            {
                std::cout << "process " << this->id << "'s channel is ready\n";
            }
        L1:
            std::cout << "reading data...\n";
            data = this->chan->read(this);
            std::cout << "process " << this->id << " read data "
                      << this->data << std::endl;
            set_label(2);
            return; 
        LEND:
            std::cout << "END (proc " << id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t                              id;
        T                                  data;
        pj_runtime::pj_one2one_channel<T>* chan;
    };

    class one2one_test
    {
    public:
        one2one_test()
        {
            std::cout << "instantiating test...\n";
        }

        void run()
        {
            std::cout << "\n *** CREATING ONE2ONE CHANNEL<INT> *** \n\n";
            pj_runtime::pj_one2one_channel<int32_t> oto_ch;

            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sch;

            std::cout << "\n *** CREATING TWO PROCESSES FOR R/W *** \n\n";
            one2one_reader<int32_t>* oto_r = new one2one_reader<int32_t>(0, &oto_ch);

            one2one_writer<int32_t>* oto_w = new one2one_writer<int32_t>(1,
                                                                         &oto_ch,
                                                                         69);

            std::cout << "\n *** RANDOMIZING PROCESS ORDER *** \n\n";
            std::vector<pj_runtime::pj_process*> processes(2);

            processes[0] = oto_r;
            processes[1] = oto_w;
            std::random_shuffle(processes.begin(), processes.end()); 

            std::cout << "\n *** SCHEDULING PROCESSES *** \n\n";
            int32_t i;
            for(i = 0; i < 2; ++i)
            {
                sch.insert(processes[i]);
            }

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sch.start();
        }
    };
}

#endif
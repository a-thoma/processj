#ifndef PROTOCOL_TEST_HPP
#define PROTOCOL_TEST_HPP


/* NO LONGER VALID */

namespace pj_tests
{
    class protocol_p
    {
    public:
        protocol_p() = delete;

        struct request
        {
        public:

            request() = default;

            request(int number, double amount)
            : number(number), amount(amount)
            {

            }

            ~request() = default;

            int number;
            double amount;
        };

        struct reply
        {
        public:

            reply() = default;

            reply(bool status)
            : status(status)
            {

            }

            ~reply() = default;

            bool status;
        };
    };

    template <typename T>
    class protocol_writer : public pj_runtime::pj_process
    {
    public:
        protocol_writer() = delete;

        protocol_writer(int32_t                              id,
                        pj_runtime::pj_one2one_channel<T>* chan)
        : id(id)
        {
            this->chan = chan;
        }

        virtual ~protocol_writer() = default;

        void run()
        {
            static protocol_p::request req{.number = 0, .amount = 420.69};
            data = req;
            switch(get_label())
            {
                case 0: goto L0;   break;
                case 1: goto LEND; break;
            }
        L0:
            /* TODO: note how stupid this is. can we just allocate the protocol
             * as the request in initializers? do we need a ctor for a request
             * to make that happen?
             * ---
             * tl;dr find a way to not have to make a protocol and a request on
             * the stack
             */
            std::cout << "process " << this->id
                      << " writing data " << req.number << ", " << req.amount
                      << "...\n";
            this->chan->write(this, this->data);
            std::cout << "process " << this->id
                      << " wrote data " << req.number << ", " << req.amount
                      << std::endl;
            set_label(1);
            return;
        LEND:
            std::cout << "END (proc " << this->id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t                              id;
        T                                  data;
        pj_runtime::pj_one2one_channel<T>* chan;
    };

    template <typename T>
    class protocol_reader : public pj_runtime::pj_process
    {
    public:
        protocol_reader() = delete;

        protocol_reader(int32_t                              id,
                        pj_runtime::pj_one2one_channel<T>* chan)
        : id(id)
        {
            this->chan = chan;
        }

        virtual ~protocol_reader() = default;

        void run()
        {
            static protocol_p::request req;
            static protocol_p::reply   rep;
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
            if(std::holds_alternative<protocol_p::request>(data))
            {
                std::cout << "reader received a request\n";
                req = std::get<protocol_p::request>(data);
                std::cout << req.number << ", " << req.amount << std::endl;
            }
            else if(std::holds_alternative<protocol_p::reply>(data))
            {
                std::cout << "reader received a reply\n";
                rep = std::get<protocol_p::reply>(data);
                std::cout << std::boolalpha << rep.status << std::endl;
            }
            set_label(2);
            return;
        LEND:
            std::cout << "END (proc " << this->id << ")\n";
            terminate();
            return;
        }

    private:
        int32_t                              id;
        T                                  data;
        pj_runtime::pj_one2one_channel<T>* chan;
    };

    class protocol_test
    {
    public:
        protocol_test()
        {
            std::cout << "instantiating test...\n";
        }

        /* NOTE: just for basics, see below. this should be the idea for
         * generated code
         */
        // void run()
        // {
        //     std::cout << "\n *** CREATING PROTOCOL *** \n\n";
        //     pj_runtime::pj_protocol<struct request, struct reply> p;

        //     std::cout << "\n *** WRITING TO PROTOCOL *** \n\n";
        //     struct request r{.number = 0, .amount = 420.69};
        //     p = r;

        //     std::cout << "\n *** READING FROM PROTOCOL *** \n\n";
        //     if(std::holds_alternative<struct request>(p))
        //     {
        //         std::cout << "request\n";
        //         struct request from_p = std::get<struct request>(p);
        //         std::cout << from_p.number << ", " << from_p.amount << std::endl;
        //     }
        //     else if(std::holds_alternative<struct reply>(p))
        //     {
        //         std::cout << "reply\n";
        //         struct reply from_p = std::get<struct reply>(p);
        //         std::cout << from_p.number << ", " << from_p.amount << std::endl;
        //     }
        // }

        void run()
        {
            std::cout << "\n *** CREATING SCHEDULER *** \n\n";
            pj_runtime::pj_scheduler sched;

            std::cout << "\n *** CREATING CHANNEL FOR PROTOCOL *** \n\n";
            pj_runtime::pj_one2one_channel<pj_runtime::pj_protocol<protocol_p::request, protocol_p::reply>> oto_ch;

            std::cout << "\n *** CREATING READER AND WRITER PROCESSES *** \n\n";
            auto* oto_w =
                new protocol_writer<pj_runtime::pj_protocol<protocol_p::request, protocol_p::reply>>(0, &oto_ch);
            auto* oto_r =
                new protocol_reader<pj_runtime::pj_protocol<protocol_p::request, protocol_p::reply>>(1, &oto_ch);

            std::cout << "\n *** SCHEDULING PROCESSES *** \n\n";
            sched.insert(oto_w);
            sched.insert(oto_r);

            std::cout << "\n *** STARTING SCHEDULER *** \n\n";
            sched.start();
        }
    };
}

#endif
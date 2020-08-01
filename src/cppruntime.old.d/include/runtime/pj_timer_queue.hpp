#ifndef PJ_TIMER_QUEUE_HPP
#define PJ_TIMER_QUEUE_HPP

#include <runtime/pj_timer.hpp>
#include <utilities/delay_queue.hpp>

#include <thread>
#include <atomic>

namespace pj_runtime
{
    class pj_timer_queue
    {
    public:
        pj_timer_queue()
        : exit_value(1)
        {
            std::cout << "timerqueue constructor called\n";
        }

        ~pj_timer_queue()
        {
            std::cout << "timerqueue destructor called\n";
            if(timer_thread.joinable())
            {
                std::cout << "attempting to join timer_thread\n";
                timer_thread.join();
            }

            /* make sure we delete our kill_timer sanely */
            if(kill_timer)
            {
                std::cout << "kill_timer populated, deleting\n";
                delete kill_timer;
            }

            std::cout << "timer_thread joined, end of timerqueue constructor\n";
        }

        void insert(pj_timer* timer)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            std::cout << "inserting timer " << *timer << " into timerqueue\n";
            dq.enqueue(timer, timer->timeout);
            std::cout << "done inserting\n";
        }

        void start()
        {
            std::cout << "starting timer_thread\n";
            /* kick off a thread running our timer algorithm
             * ---
             * should use the overloaded operator()
             */
            timer_thread = std::thread([this]()
            {
                while(1)
                {
                    pj_timer* timer = dq.dequeue();

                    std::cout << "grabbed a timer: " << *timer << std::endl;

                    timer->expire();

                    pj_process* p = timer->get_process();

                    std::cout << "DEBUG: process of timer is: " << timer->get_process() << std::endl;

                    if(!p)
                    {
                        std::cout << "this timer has no process associated with it\n";
                    }
                    else
                    {
                        std::cout << "this timer should not cause the thread to exit\n";
                    }

                    /* check if we can safely exit as a thread */
                    if(!p && exit_value)
                    {
                        std::cout << "timer_thread exiting\n";
                        return;
                    }

                    if(p)
                    {
                        p->set_ready();
                        delete timer;
                    }
                }
            });

            std::cout << "timer_thread started\n";
        }

        void kill()
        {
            std::cout << "timer_thread, it's time to die\n";

            /* immediate death */
            std::chrono::system_clock::time_point tp = std::chrono::system_clock::now();

            /* make our kill_timer and place it in the queue */
            kill_timer = new pj_timer(nullptr, tp);
            this->insert(kill_timer);

            /* we're ready to die */
            kill_flag.exchange(true);

            std::cout << "timer_thread told to die\n";
        }

        size_t size()
        {
            std::cout << "size() called on timerqueue\n";
            return dq.size();
        }

    private:
        /* the pj_timer_queue essentially needs to grab a timer, and wait for
         * it to timeout before setting it's _expired flag to true, and
         * handling the the process that had that timer appropriately
         * ---
         * allow a "fake timer" to be placed in the timer queue, which will
         * both disallow permanent waiting for a timer from the delayqueue,
         * and make it so that when that is placed in the queue, it is
         * taken out immediately, the flag is then checked, and if 
         * it is set to true the timerqueue thread dies, else we have
         * not received a fake timer, and we should handle that timer
         * however we are supposed to
         */
        pj_utilities::delay_queue<pj_timer*> dq;
        std::thread                timer_thread;
        std::mutex                          mtx;

        /* TODO: need to implement exit_value counter
         * ---
         * exit_value must be 1 initially (first process does not
         * increment), and must be 0 for the TimerQueue to be
         * allowed to exit properly
         * ---
         * incrememted on fork, decremented on process end 
         */
        std::atomic<int32_t> exit_value;
        std::atomic<bool>    kill_flag;

        pj_timer* kill_timer = nullptr;
    };
}

#endif
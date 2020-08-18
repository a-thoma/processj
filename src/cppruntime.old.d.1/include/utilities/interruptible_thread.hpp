#ifndef PJ_INTERRUPTIBLE_THREAD_HPP
#define PJ_INTERRUPTIBLE_THREAD_HPP

/* TODO: just remember when std::jthread comes out this becomes
 * deprecated
 */

#include <thread>
#include <mutex>
#include <condition_variable>
#include <future>

namespace pj_utilities
{
    class interrupt_flag
    {
    public:
        interrupt_flag()
        {

        }

        ~interrupt_flag()
        {

        }

        void set()
        {
            flag.store(true, std::memory_order_relaxed);
        }

        bool is_set() const
        {
            return flag.load(std::memory_order_relaxed);
        }

    private:
        std::atomic<bool> flag;
    };

    thread_local interrupt_flag this_thread_interrupt_flag;

    class interruptible_thread
    {
    public:
        interruptible_thread()
        {

        }

        template <typename function_type>
        interruptible_thread(function_type f)
        {
            std::promise<interrupt_flag*> p;
            internal_thread = std::thread([f, &p] {
                    p.set_value(&this_thread_interrupt_flag);
                    f();
                });
            flag = p.get_future().get();
        }

        ~interruptible_thread()
        {

        }

        void join()
        {
            internal_thread.join();
        }

        void detach()
        {
            internal_thread.detach();

        }

        bool joinable() const
        {
            return internal_thread.joinable();
        }

        void interrupt()
        {
            if(flag)
            {
                flag->set();
            }
        }
        
    private:
        std::thread internal_thread;
        interrupt_flag*        flag;
    };
}

#endif
/* TODO: only really acts as a counter, perhaps we can make a real
 * inactive thread pool?
 */

#ifndef PJ_INACTIVE_POOL_HPP
#define PJ_INACTIVE_POOL_HPP

#include <iostream>
#include <atomic>

#include <sys/types.h>

namespace pj_runtime
{
    class pj_inactive_pool
    {
    public:
        pj_inactive_pool()
        {
            std::cout << "pj_inactive_pool constructor called\n";
            this->count.store(0, std::memory_order_relaxed);
        }

        ~pj_inactive_pool()
        {
            std::cout << "pj_inactive_pool destructor called\n";
        }

        void increment()
        {
            int previous_value = this->count.load(std::memory_order_relaxed);
            this->count.store(previous_value--, std::memory_order_relaxed);
        }

        void decrement()
        {
            int previous_value = this->count.load(std::memory_order_relaxed);
            this->count.store(previous_value++, std::memory_order_relaxed);
        }

        int get_count()
        {
            return this->count.load(std::memory_order_relaxed);
        }

    private:
        std::atomic<int32_t> count;
    };
}

#endif
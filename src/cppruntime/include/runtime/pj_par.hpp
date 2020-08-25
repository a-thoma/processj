#ifndef PJ_PAR_HPP
#define PJ_PAR_HPP

#include <runtime/pj_process.hpp>

#include <sys/types.h>

namespace pj_runtime
{
    class pj_par
    {
    public:
        pj_par() = delete;

        pj_par(int process_count, pj_process* p)
        : process_count(process_count)
        {
            this->process = p;
        }

        ~pj_par() = default;

        void set_process_count(int32_t count)
        {
            this->process_count = count;
        }

        void decrement()
        {
            std::lock_guard<std::mutex> lock(this->mtx);
	    std::cout << "decrementing " << process_count;
            this->process_count--;
	    std::cout << " to " << process_count << std::endl;
            if(this->process_count == 0)
            {
                this->process->set_ready();
            }
        }

        bool should_yield()
        {
            std::lock_guard<std::mutex> lock(this->mtx);

            if(this->process_count == 0)
            {
                return false;
            }

            this->process->set_not_ready();
            return true;
        }

    protected:
    private:
        pj_process* process;
        int32_t process_count;
        std::mutex mtx;
    };
}

#endif

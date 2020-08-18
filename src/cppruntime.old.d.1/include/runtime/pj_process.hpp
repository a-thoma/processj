#ifndef PJ_PROCESS_HPP
#define PJ_PROCESS_HPP

#include <iostream>
#include <ostream>
#include <mutex>

#include <sys/types.h>

namespace pj_runtime
{
    class pj_process
    {
    public:
        pj_process()
        {
            std::cout << "pj_process constructor called\n";
        }

        virtual ~pj_process()
        {
            std::cout << "pj_process destructor called\n";
        }

        bool is_ready()
        {
            return ready;
        }

        /* TODO: this should also call scheduler's inactivepool() to be decr'd */
        void set_ready()
        {
            std::unique_lock<std::mutex> lock(this->mtx);
            if(!ready)
            {
                ready = true;
                // (PJScheduler*)scheduler)->ip.decrement();
            }
        }

        /* TODO: this should also call scheduler's inactivepool() to be incr'd */
        void set_not_ready()
        {
            std::unique_lock<std::mutex> lock(this->mtx);
            if(ready)
            {
                ready = false;
                // ((PJScheduler*)scheduler)->ip.increment();
            }
        }

        void terminate()
        {
            terminated = true;
        }

        bool is_terminated()
        {
            return terminated;
        }

        /* method to be overwritten by specialization */
        virtual void run()
        {
            std::cout << "pj_process base method (nothing overwritten)\n";
        }

        /* put process into scheduler's run queue */
        /* TODO: not possible w/o circular ref'ing PJScheduler.hpp */
        // void schedule();

        /* method called at end of a process' lifetime */
        virtual void finalize()
        {

        }

        /* instrumenter method to be replaced
         * ---
         * TODO: might not need this, we can insert ASM manually
         */
        virtual void yield()
        {

        }

        /* instrumenter method to be replaced
         * ---
         * TODO: might not need this, we can insert ASM manually
         */
        virtual void set_label(uint32_t label)
        {
            run_label = label;
        }

        /* TODO: in the future, we could potentially move this stuff
         * back to the protected section of pj_process and remove
         * the necessity of set/get_label(), as well as terminate()
         */
        virtual uint32_t get_label()
        {
            return run_label;
        }

        /* instrumenter method to be replaced
         * ---
         * TODO: might not need this, we can insert ASM manually
         */        
        virtual void resume(uint32_t label)
        {

        }

        friend std::ostream& operator<<(std::ostream& o, pj_process& p)
        {
            return o << "base process operator<< called (nothing overwritten)";
        }

    protected:
        /* NOTE: in the future this scheduler will be removed
         * instead of commented out, because for now, when
         * we extend pj_process we will add a reference to the
         * scheduler from the runtime itself. this is a
         * workaround, and hopefully isn't the _last_ way this
         * will be handled, but it will work one way or another
         */
        // static pjscheduler_pjprocess_internal_t* scheduler;
        std::mutex mtx;

    private:
        uint32_t run_label      = 0;
        bool     ready       = true;
        bool     terminated = false;

    // friend class pj_timer;
    };
}

#endif
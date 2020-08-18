/* TODO: inherits from concurrent libs in java, find out equivalents
 * ---
 * more specifically, find an equivalent use for:
 *     java.util.concurrent.Delayed;
 *     java.util.concurrent.TimeUnit;
 * ---
 */

#ifndef PJ_TIMER_HPP
#define PJ_TIMER_HPP

#include <runtime/pj_process.hpp>

#include <chrono>
#include <stdexcept>
#include <iostream>
#include <ostream>

namespace pj_runtime
{
    class pj_timer
    {
        friend class pj_timer_queue;
        
    public:    
        bool started = false;

        pj_timer()
        {
            std::cout << "pj_timer constructor called\n";
            set_timeout(std::chrono::system_clock::time_point());
        }

        pj_timer(pj_process* p, std::chrono::system_clock::time_point t)
        {
            std::cout << "pj_timer constructor called with arguments\n";
            process = p;
            set_timeout(t);

            /* TODO: this is debugging and will be removed later */
            std::cout << "after assignment, process has address "
                      << process << std::endl;
        }

        ~pj_timer()
        {

        }

        void set_timeout(std::chrono::system_clock::time_point value)
        {
            if(!timeout_set)
            {
                timeout = value;
                timeout_set = true;
            }
            else
            {
                throw std::runtime_error("timeout_set already true");
            }
        }

        void start()
        {
            /* TODO: create a new timer and place it in the scheduler's timer queue
             * ---
             * this is done to allow for timeout on a thread for being interrupted
             * or some other unwanted behavior, need to make this work somehow,
             * whether or not the method stays here or gets moved elsewhere
             */
            timeout = std::chrono::system_clock::now() + delay;
            // ((PJScheduler*)PJProcess::scheduler)->insertTimer(this);
        }

        void expire()
        {
            expired = true;
        }

        void kill()
        {
            killed = true;
        }

        pj_process* get_process()
        {
            return (this->killed) ? (pj_process*)nullptr : (process);
        }

        friend std::ostream& operator<<(std::ostream& s, const pj_timer& t)
        {
            return s << "Process: " << t.process;
        }

        std::chrono::system_clock::time_point read()
        {
            return timeout;
        }

    protected:
        std::chrono::system_clock::time_point timeout;
        bool expired     = false;

    private:
        pj_process* process;

        // std::chrono::system_clock::time_point delay;
        std::chrono::system_clock::duration delay;
        bool killed = false;

        /* final in java */
        bool timeout_set = false;
    };
}

#endif
#ifndef PJ_RUN_QUEUE_HPP
#define PJ_RUN_QUEUE_HPP

#include <runtime/pj_process.hpp>

#include <queue>
#include <thread>
#include <mutex>
#include <iostream>

#include <sys/types.h>

namespace pj_runtime
{
    class pj_run_queue
    {
    public:
        
        pj_run_queue()
        {
            std::cout << "pj_run_queue constructor called\n";
        }

        ~pj_run_queue()
        {
            std::cout << "pj_run_queue destructor called\n";
        }

        /* synchronized in java */
        void insert(pj_process* p)
        {
            std::lock_guard<std::mutex> lk(rq_mutex);
            queue.push(p);
        }

        /* synchronized in java */
        pj_process* next()
        {
            std::lock_guard<std::mutex> lk(rq_mutex);
            pj_process* next = queue.front();
            queue.pop();
            return next;
        }

        /* synchronized in java */
        size_t size()
        {
            std::lock_guard<std::mutex> lk(rq_mutex);
            size_t size = queue.size();
            return size;
        }  

    protected:
        std::queue<pj_process*> queue;

    private:
        std::mutex rq_mutex;    
    };
}

#endif
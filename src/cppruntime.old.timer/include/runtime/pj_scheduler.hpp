#ifndef PJ_SCHEDULER_HPP
#define PJ_SCHEDULER_HPP

#include <runtime/pj_inactive_pool.hpp>
#include <runtime/pj_timer_queue.hpp>
#include <runtime/pj_run_queue.hpp>
#include <runtime/pj_process.hpp>
#include <runtime/pj_timer.hpp>

#include <mutex>
#include <string>

#include <sched.h>
#include <pthread.h>
#include <sys/types.h>

namespace pj_runtime
{
    class pj_scheduler
    {   

    public:        
        pj_inactive_pool ip;

        pj_scheduler()
        : cpu(0), cpus(std::thread::hardware_concurrency())
        {
            std::cout << "pj_scheduler constructor called\n";
        }

        pj_scheduler(uint32_t cpu)
        : cpu(cpu), cpus(std::thread::hardware_concurrency())
        {
            std::cout << "pj_scheduler constructor called with arguments\n";
        }

        ~pj_scheduler()
        {
            std::cout << "pj_scheduler destructor called\n";
            if(this->sched_thread.joinable())
            {
                this->sched_thread.join();
            }

            /* timer queue should only be killed once */
            tq.kill();

            std::cerr << "[Scheduler " << cpu << "] Total Context Switches: "
                      << context_switches
                      << "\n[Scheduler " << cpu << "] Max RunQueue Size: "
                      << max_rq_size
                      << std::endl;
        }

        void insert(pj_process* p)
        {
            std::lock_guard<std::mutex> lk(mutex);
            std::cout << "inserting process " << p
                      << " into scheduler run queue\n";
            rq.insert(p);
            std::cout << "process " << p << " inserted\n";
        }

        void insert(pj_timer* t)
        {
            std::lock_guard<std::mutex> lk(mutex);
            tq.insert(t);
        }

        void start()
        {
            /* only need to start the timer queue once */
            tq.start();
            this->sched_thread = std::thread(&pj_scheduler::run, this);

            /* this allows us to use heap-allocated variables
             * in the main driver for the code (i.e. run() in alt_test).
             * without this, we would have to nondeterministically choose
             * if the alt_writer process or the alt_process process
             * "owns" the channel and thus has to free it -- it's owned
             * by the runtime, and should be deleted there.
             * ---
             * later on, shared_ptrs will be used probably exclusively,
             * so no raw pointers will be allowed (and thus, no random
             * pointers that don't at least have a deletor for them
             * when they really go out of scope)
             */
            if(this->sched_thread.joinable())
            {
                this->sched_thread.join();
            }
        }
      
        void run(void)
        {
            this->isolate_thread();

            while(rq.size() > 0)
            {
                if(static_cast<size_t>(rq.size()) > max_rq_size)
                {
                    max_rq_size = rq.size();
                }

                pj_process* p = rq.next();

                if(p->is_ready())
                {
                    p->run();
                    context_switches++;
                    if(!p->is_terminated())
                    {
                        rq.insert(p);
                    }
                    else
                    {
                        p->finalize();
                        delete p;
                    }
                }
                else
                {
                    rq.insert(p);
                }

                if(ip.get_count() == static_cast<int>(rq.size()) &&
                   static_cast<size_t>(rq.size()) > 0            &&
                   tq.size() == 0)
                {
                    std::cerr << "No processes ready to run. System is deadlocked.\n";
                    tq.kill();

                    log_execution_time();
                    exit(1);
                }
            }
        }

        friend std::ostream& operator<<(std::ostream& s, const pj_scheduler& t)
        {
            return s << "pj_scheduler operator<< not implemented yet";            
        }

    protected:
        int size()
        {
            std::lock_guard<std::mutex> lk(mutex);
            return rq.size();
        }

        void inc_context_switches()
        {
            std::lock_guard<std::mutex> lk(mutex);
            context_switches++;
        }

        void inc_max_rq_size(size_t size)
        {
            std::lock_guard<std::mutex> lk(mutex);
            if(size > max_rq_size)
            {
                max_rq_size = size;
            }
        }

    private:
        pj_timer_queue tq;
        pj_run_queue   rq;
        
        std::mutex mutex;
        std::mutex iomutex;

        uint64_t start_time       = 0;
        int32_t  context_switches = 0;
        size_t   max_rq_size      = 0;

        /* NOTE: added for multicore scheduler impl. */
        // std::vector<std::thread> sched_threads;
        std::thread sched_thread;
        /* TODO: each scheduler should know about the others,
         * but _should_ we do that...? do we _need_ to?
         */
        // std::vector<pj_scheduler*> schedulers;
        uint32_t             cpu;
        uint32_t            cpus;

        void isolate_thread(void)
        {
            std::unique_lock<std::mutex> lock(this->iomutex, std::defer_lock);
            std::thread::id th_id = this->sched_thread.get_id();

            lock.lock();
            std::cout << "isolating thread " << th_id << " to cpu "
                      << cpu << std::endl;
            lock.unlock();

            cpu_set_t cur_set;
            cpu_set_t new_set;
            pthread_t    p_th;
            uint32_t        i;

            CPU_ZERO(&new_set);

            if(!this->cpus)
            {
                lock.lock();
                std::cerr << "error: hardware_concurrency not set/determinable\n";
                lock.unlock();
                abort();
            }

            uint8_t arr_cur_set[this->cpus];
            uint8_t arr_new_set[this->cpus];

            for(i = 0; i < this->cpus; ++i)
            {
                arr_cur_set[i] = 0;
                arr_new_set[i] = 0;
            }

            p_th = this->sched_thread.native_handle();
            lock.lock();
            std::cout << "the native_handle is " << p_th << std::endl;
            lock.unlock();

            if(!p_th)
            {
                lock.lock();
                std::cerr << "error: native_handle() returned null\n";
                lock.unlock();
                abort();
            }

            lock.lock();
            std::cout << "getting thread cpu_set...\n";
            lock.unlock();

            CPU_ZERO(&cur_set);

            if(pthread_getaffinity_np(p_th,
                                      sizeof(cpu_set_t),
                                      &cur_set))
            {
                lock.lock();
                perror("pthread_getaffinity_np");
                lock.unlock();
                abort();
            }

            lock.lock();
            for(i = 0; i < this->cpus; ++i)
            {
                if(CPU_ISSET(i, &cur_set))
                {
                    std::cout << "cpu " << i 
                              << " is in thread " << th_id
                              << "'s current cpu set\n";
                }
            }

            std::cout << "now setting thread " << th_id << "'s cpu_set to "
                      << cpu << std::endl;
            lock.unlock();

            CPU_SET(cpu, &new_set);
            arr_new_set[cpu] = 1;

            lock.lock();
            std::cout << "new cpu_set is:\n";
            for(i = 0; i < cpus; ++i)
            {
                std::cout << static_cast<uint32_t>(arr_new_set[i]) << " ";
            }
            std::cout << "\nwhich implies:\n";
            for(i = 0; i < cpus; ++i)
            {
                if(CPU_ISSET(i, &new_set))
                {
                    std::cout << i << " ";
                }
            }
            std::cout << std::endl;
            lock.unlock();

            if(pthread_setaffinity_np(p_th,
                                      sizeof(cpu_set_t),
                                      &new_set))
            {
                lock.lock();
                perror("pthread_setaffinity_np");
                lock.unlock();
                abort();
            }

            lock.lock();
            std::cout << "verifying thread " << th_id << "'s cpu_set...\n";
            lock.unlock();

            CPU_ZERO(&cur_set);
            if(pthread_getaffinity_np(p_th,
                                      sizeof(cpu_set_t),
                                      &cur_set))
            {
                lock.lock();
                perror("pthread_getaffinity_np");
                lock.unlock();
                abort();
            }

            lock.lock();
            for(i = 0; i < cpus; ++i)
            {
                if(CPU_ISSET(i, &cur_set))
                {
                    std::cout << "cpu " << i << " is in new current cpu set\n";
                    arr_cur_set[i] = 1;
                }
            }

            for(i = 0; i < cpus; ++i)
            {
                if(arr_cur_set[i] != arr_new_set[i])
                {
                    std::cerr << "error: cpu " << i << " is in thread " << th_id
                              << "'s cpu_set\n";
                    lock.unlock();
                    abort();
                }
            }
            lock.unlock();

            lock.lock();
            std::cout << "thread " << th_id
                      << "'s cpu_set successfully modified\n\n";
            lock.unlock();
        }

        void log_execution_time()
        {
            /* TODO: add this logging function */
        }
    };
}

#endif
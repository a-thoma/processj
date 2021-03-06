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

#include <iostream>
#include <chrono>
#include <ostream>

namespace pj_runtime
{
    
    class pj_timer
    {
        friend class pj_timer_queue;
        friend class pj_alt;

    public:
        bool m_started;
        bool m_expired;

        pj_timer()
        : m_started(false),
          m_expired(false),
          m_delay(0),
          m_real_delay(std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::time_point::min())),
          m_killed(false),
          m_process(static_cast<pj_process*>(0))
        {
            std::cout << "pj_timer constructor called with immediate timeout\n";
        }

        pj_timer(long timeout)
        : m_started(false),
          m_expired(false),
          m_delay(timeout),
          m_real_delay(std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::time_point::min())),
          m_killed(false),
          m_process(static_cast<pj_process*>(0))
        {
            std::cout << "pj_timer constructor called with timeout of " << m_delay << std::endl;
        }

        pj_timer(pj_process* process, long timeout)
        : m_started(false),
          m_expired(false),
          m_delay(timeout),
          m_real_delay(std::chrono::time_point_cast<std::chrono::milliseconds>(std::chrono::system_clock::time_point::min())),
          m_killed(false),
          m_process(process)
        {
            std::cout << "pj_timer constructor called with process ptr and timeout of " << m_delay << std::endl;
        }

        ~pj_timer() = default;

        void start()
        {
            m_real_delay = std::chrono::system_clock::time_point(std::chrono::milliseconds(pj_timer::read() + this->get_delay()));
            std::cout << "pj_timer started\n";
            m_started = true;
        }

        void timeout(long timeout)
        {
            m_delay = timeout;
        }

        static long read()
        {
            auto now = std::chrono::system_clock::now();
            auto now_ms = std::chrono::time_point_cast<std::chrono::milliseconds>(now);
            auto now_epoch = now_ms.time_since_epoch();
            auto value = std::chrono::duration_cast<std::chrono::milliseconds>(now_epoch);
            return static_cast<long>(value.count());
        }

        void kill()
        {
            std::cout << "pj_timer killed\n";
            m_killed = true;
        }

        void expire()
        {
            std::cout << "pj_timer expired\n";
            m_expired = true;
        }

        long get_delay()
        {
            std::cout << "getting delay of " << m_delay << std::endl;
            return m_delay;
        }

        void set_process(pj_process* p)
        {
            m_process = p;
        }

        pj_process* get_process()
        {
            return (m_killed) ? static_cast<pj_process*>(0) : m_process;
        }

        friend std::ostream& operator<<(std::ostream& o, pj_timer& t)
        {
            return o << "Process: " << t.m_process;
        }

protected:
        std::chrono::system_clock::time_point get_real_delay()
        {
            return std::chrono::time_point_cast<std::chrono::milliseconds>(m_real_delay);
        }

    private:
        long m_delay;
        std::chrono::system_clock::time_point m_real_delay;
        long m_timeout;
        bool m_killed;
        pj_process* m_process;
    };
}

#endif